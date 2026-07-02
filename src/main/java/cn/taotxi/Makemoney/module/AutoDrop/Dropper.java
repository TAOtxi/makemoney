package cn.taotxi.Makemoney.module.AutoDrop;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.gson.JsonElement;

import cn.taotxi.Makemoney.gui.ConfigScreen;
import cn.taotxi.Makemoney.util.CommonUtil;
import cn.taotxi.Makemoney.util.game.EnchantmentHelper;
import cn.taotxi.Makemoney.util.game.InventoryUtil;
import cn.taotxi.Makemoney.util.game.ItemStackUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.InventoryMenu;


public class Dropper {
    private static final AutoDropConfig CONFIG = AutoDropConfig.getInstance();
    private static final List<Integer> ignoreSlots = new ArrayList<>();
    private static final List<Item> matchItemList = new ArrayList<>();
    private static final Minecraft client = Minecraft.getInstance();

    public static void initialize() {
        CONFIG.ignoreSlots.onChange(
            (oldValue, newValue) -> {
                ignoreSlots.clear();
                for (JsonElement slot : newValue) {
                    ignoreSlots.add(slot.getAsInt());
                }
            }
        );
        CONFIG.ignoreSlots.triggerConfigChange();

        CONFIG.matchItemLists.onChange(
            (oldValue, newValue) -> {
                matchItemList.clear();
                for (int i = 0; i < newValue.size(); i++) {
                    JsonElement item = newValue.get(i);
                    if (item.getAsJsonObject().get("enabled").getAsBoolean()) {
                        Item matchItem = CONFIG.getStdMatchItem(i);
                        if (matchItem == null) continue;

                        matchItemList.add(matchItem);
                    }
                }
            }
        );
        CONFIG.matchItemLists.triggerConfigChange();
    }

    private static boolean canDrop() {
        if (!AutoDrop.enabled) return false;
        if (client.player == null) return false;

        if (CONFIG.stopWhenCrouch.getValue() && client.player.isCrouching()) return false;
        if (CONFIG.stopWhenOpenConfigGui.getValue() && ConfigScreen.isOpenYaclScreen()) return false;

        if (CONFIG.stopWhenNotHoldingItem.getValue()) {
            ItemStack heldItem = client.player.getMainHandItem();
            String itemName = CONFIG.stopWhenNotHoldingItemName.getValue();
            String itemId = CONFIG.stopWhenNotHoldingItemId.getValue();
            if (itemName.isEmpty() || itemId.isEmpty()) return false;

            if (
                !itemName.equals("*") &&
                !ItemStackUtil.equalName(heldItem, itemName)
            )
                return false;

            if (
                !itemId.equals("*") &&
                !ItemStackUtil.equalIdWithDefaultNamespace(heldItem, itemId)
            )
                return false;
        };

        return true;
    }

    public static void tryToDropItems() {
        if (!canDrop()) return;

        if (client.player.hasContainerOpen()) return;
        if (CONFIG.triggerMinCount.getValue() > 0 && CONFIG.triggerMinCount.getValue() > notEmptySlotCount()) return;

        drop();
    }

    public static void drop() {
        final List<Integer> dropSlots = new ArrayList<>();
        InventoryMenu inventoryMenu = client.player.inventoryMenu;
        boolean isWhiteListMode = CONFIG.whiteListMode.getValue();

        for (int i = InventoryMenu.INV_SLOT_START; i < InventoryMenu.USE_ROW_SLOT_END; i++) {
            if (ignoreSlots.contains(i)) continue;

            ItemStack item = inventoryMenu.getSlot(i).getItem();
            if (item.isEmpty()) continue;

            if (
                (isWhiteListMode && !isEqualItem(item, matchItemList)) ||
                (!isWhiteListMode && isEqualItem(item, matchItemList))
            ) {
                dropSlots.add(i);
            };
        }
        if (dropSlots.isEmpty()) return;

        startToDropItems(dropSlots);
    }

    public static void startToDropItems(List<Integer> dropSlots) {
        ThrowWay throwWay = ThrowWay.valueOf(CONFIG.throwWay.getValue());
        if (throwWay.equals(ThrowWay.DIRECTION)) {
            dropItemAnywhere(dropSlots, Direction.valueOf(CONFIG.throwDirection.getValue()));
        } else if (throwWay.equals(ThrowWay.ROTATION)) {
            dropItemAnywhere(dropSlots, CONFIG.throwYaw.getValue(), CONFIG.throwPitch.getValue());
        } else {
            throw new IllegalArgumentException("Unknown throwWay: " + throwWay +". Please use DIRECTION or ROTATION."); 
        }
    }

    public static int notEmptySlotCount() {
        return InventoryUtil.getInventoryNotEmptySlots().size();
    }

    public static boolean isEqualItem(ItemStack item) {
        return isEqualItem(item, matchItemList);
    }

    public static boolean isEqualItem(ItemStack item, List<Item> matchLists) {
        for (Item check: matchLists){
            // if (!check.enabled) continue;
            // AutoDrop.LOGGER.info("Checker: {}", check.description);
            // AutoDrop.LOGGER.info("Name: {} --- {}", ItemStackUtil.getName(item), check.name);
            // AutoDrop.LOGGER.info("Id: {} --- {}", ItemStackUtil.getId(item), check.id);
            // AutoDrop.LOGGER.info("");


            // check name
            if (!check.name.equals("*") && !ItemStackUtil.equalName(item, check.name)) {
                continue;
            }
            // check id
            if (!check.id.equals("*") && !ItemStackUtil.equalId(item, check.id)) {
                continue;
            }

            // check durability
            if (item.isDamageableItem()) {
                if (check.durability == -1 && item.getDamageValue() > 0) {
                    continue;
                }
                if (check.durability == -2 && item.getDamageValue() == 0) {
                    continue;
                }

                if (item.getMaxDamage() - item.getDamageValue() < check.durability) {
                    continue;
                }
            }

            if (!check.tags.isEmpty()) {
                List<String> itemTags = ItemStackUtil.getTags(item);
                // check tag
                if (!CommonUtil.hasIntersection_hash(itemTags, check.tags)) {
                    continue;
                };
            }

            if (check.enchantments.size() == 0 ||
                check.minEnchantRequir == 0) {    // 匹配通过
                return true;
            }

            if (check.enchantments.size() < check.minEnchantRequir) {
                continue;   // 不可能通过匹配
            }

            int minEnchantRequir = check.minEnchantRequir == -1 ? check.enchantments.size() : check.minEnchantRequir;
            int hasEnchantCount = calEnchantCounts(item, check.enchantments);
            if (hasEnchantCount >= minEnchantRequir) {
                return true;   // ✔
            }
        }
        return false;
    }

    private static int calEnchantCounts(ItemStack item, Map<String, Integer> enchantments) {
        int counter = 0;
        Map<String, Integer> itemEnchantments = EnchantmentHelper.getEnchantments(item);
        for (Map.Entry<String, Integer> entry: itemEnchantments.entrySet()) {
            String ID = entry.getKey();
            int level = entry.getValue();
            if (level >= enchantments.getOrDefault(ID, 6666)) {
                counter++;
            }
        }
        return counter;
    }

    private static void dropItems(List<Integer> slots) {
        LocalPlayer player = client.player;
        InventoryMenu inventoryMenu = player.inventoryMenu;
        for (int slot: slots) {
            client.gameMode.handleInventoryMouseClick(inventoryMenu.containerId, slot, 1, ClickType.THROW, player);
        }
    }

    private static void dropItemAnywhere(int slot, Direction direction) {
        dropItemAnywhere(List.of(slot), direction);
    }

    private static void dropItemAnywhere(List<Integer> slots, float yaw, float pitch) {
        float xRot = client.player.getXRot();
        float yRot = client.player.getYRot();

        // 时间间隔太短了，以至于客户端上看不见转向
        setPlayerRotation(yaw, pitch);
        dropItems(slots);
        setPlayerRotation(yRot, xRot);
    }

    // TODO: Bug: 创造模式会丢弃两个物品，但背包实际减少的是一个
    private static void dropItemAnywhere(List<Integer> slots, Direction direction) {
        if (direction == Direction.LOOKING) {
          dropItems(slots);
          return;
        }

        float xRot = client.player.getXRot();
        float yRot = client.player.getYRot();
        setPlayerRotation(direction);
        dropItems(slots);
        setPlayerRotation(yRot, xRot);
    }

    private static void setPlayerRotation(Direction direction) {
        switch (direction) {
            case LOOKING:
                break;
            case UP:
                setPlayerRotation(0, -90);
                break;
            case DOWN:
                setPlayerRotation(0, 90);
                break;
            case EAST:
                setPlayerRotation(-90, 0);
                break;
            case WEST:
                setPlayerRotation(90, 0);
                break;
            case NORTH:
                setPlayerRotation(-180, 0);
                break;
            case SOUTH:
                setPlayerRotation(0, 0);
                break;
            default:
                // should never happen
                throw new IllegalArgumentException("Unknown direction: " + direction);
        }
    }

    private static void setPlayerRotation(float yaw, float pitch) {
        LocalPlayer player = client.player;
        player.setYRot(yaw);
        player.setXRot(pitch);

        player.connection.send(
            new ServerboundMovePlayerPacket.Rot(
                yaw, pitch,
                player.onGround(),
                false
            )
        );
    }

    public static void onOpenContainerDrop() {
        if (!canDrop()) return;

        if (!CONFIG.dropWhenOpenContainer.getValue()) return;

        
        AbstractContainerMenu containerMenu = client.player.containerMenu;
        Map.Entry<Integer, Integer> slotRange = InventoryUtil.getContainerSlotRange(containerMenu);
        if (slotRange == null) return;
        
        int startSlot = slotRange.getKey();
        int endSlot = slotRange.getValue();
        List<Integer> dropSlots = new ArrayList<>(endSlot - startSlot + 1);

        boolean isWhiteListMode = CONFIG.whiteListMode.getValue();
        for (int i = startSlot; i <= endSlot; i++) {
            ItemStack item = containerMenu.getSlot(i).getItem();
            
            if (item.isEmpty()) continue;
            
            if (
                (isWhiteListMode && !isEqualItem(item, matchItemList)) ||
                (!isWhiteListMode && isEqualItem(item, matchItemList))
            ) {
                dropSlots.add(i);
            }
        }
        
        boolean putItemInInventory = CONFIG.putItemInInventoryWhenOpenContainer.getValue();
        for (int slot: dropSlots) {
            if (!putItemInInventory) {
                client.gameMode.handleInventoryMouseClick(
                    containerMenu.containerId, slot, 1, ClickType.THROW, client.player);
            }
            else {
                client.gameMode.handleInventoryMouseClick(
                    containerMenu.containerId, slot, 0, ClickType.QUICK_MOVE, client.player);
            }
        }

        client.player.closeContainer();
    }
}

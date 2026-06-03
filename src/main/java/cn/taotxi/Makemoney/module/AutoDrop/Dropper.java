package cn.taotxi.Makemoney.module.AutoDrop;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.gson.JsonElement;

import cn.taotxi.Makemoney.gui.ConfigScreen;
import cn.taotxi.Makemoney.util.CommonUtil;
import cn.taotxi.Makemoney.util.game.InventoryUtil;
import cn.taotxi.Makemoney.util.game.ItemStackUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.InventoryMenu;


public class Dropper {
    private static final AutoDropConfig CONFIG = AutoDropConfig.getInstance();
    private static final List<Integer> ignoreSlots = CONFIG.ignoreSlots.getValueAsIntList();
    private static final List<Item> matchItemList = CONFIG.getMatchItemLists();
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

        CONFIG.matchItemLists.onChange(
            (oldValue, newValue) -> {
                matchItemList.clear();
                for (JsonElement item : newValue) {
                    if (item.getAsJsonObject().get("enabled").getAsBoolean()) {
                        matchItemList.add(AutoDropConfig.getGson().fromJson(item, Item.class));
                    }
                }
            }
        );

    }

    // TODO: 在捡到物品的事件中，调用该方法
    public static void tryToDropItems() {
        LocalPlayer player = client.player;
        if (player == null) return;

        if (CONFIG.stopWhenCrouch.getValue() && player.isCrouching()) return;
        if (CONFIG.stopWhenOpenContainer.getValue() && player.hasContainerOpen()) return; // TODO: BUG: 无法检测是否打开背包
        if (CONFIG.stopWhenOpenConfigGui.getValue() && ConfigScreen.isOpenYaclScreen()) return;
        if (CONFIG.triggerMinCount.getValue() > 0 && CONFIG.triggerMinCount.getValue() > notEmptySlotCount()) return;
        
        if (CONFIG.stopWhenNotHoldingItem.getValue()) {
            ItemStack heldItem = player.getMainHandItem();
            String itemName = CONFIG.stopWhenNotHoldingItemName.getValue();
            String itemId = CONFIG.stopWhenNotHoldingItemId.getValue();
            if (itemName.isEmpty() || itemId.isEmpty()) return;

            if (
                !itemName.equals("*") &&
                !ItemStackUtil.equalName(heldItem, itemName)
            )
                return;

            if (
                !itemId.equals("*") &&
                !ItemStackUtil.equalId(heldItem, itemId)
            )
                return;
        };

        drop();
    }

    public static void drop() {
        final List<Integer> dropSlots = new ArrayList<>();
        InventoryMenu inventoryMenu = client.player.inventoryMenu;

        for (int i = InventoryMenu.INV_SLOT_START; i < InventoryMenu.USE_ROW_SLOT_END; i++) {
            if (ignoreSlots.contains(i)) continue;

            ItemStack item = inventoryMenu.getSlot(i).getItem();

            // AutoDrop.LOGGER.info("Check item {} in slot {}", item.getItemName(), i);
            if (isEqualItem(item, matchItemList)) continue;
            // AutoDrop.LOGGER.info("Dropping item {} in slot {}", ItemStackUtil.getName(item), i);
            dropSlots.add(i);
        }
        if (dropSlots.isEmpty()) return;

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
        if (item.isEmpty()) return true;

        for (Item check: matchLists){
            // if (!check.enabled) continue;

            // TODO: 兼容其它命名空间
            // check name
            if (!check.name.equals("*") && !ItemStackUtil.equalName(item, check.name)) {
                // String name = ItemStackUtil.getName(item);
                // AutoDrop.LOGGER.info("name not match: " + name + " " + check.name);
                continue;
            }
            // check id
            if (!check.id.equals("*") && !ItemStackUtil.equalId(item, check.id)) {
                // AutoDrop.LOGGER.info("id not match: " + item + " " + ItemStackUtil.withDefaultNamespace(check.id));
                continue;
            }

            /**
             * 有点绕，梳理下思路
             * 1. 如果check.tags包含`*`，匹配直接通过
             * 2. 如果check.tags为空，匹配通过
             * 3. 如果itemTags和check.tags有交集，则通过
             * 下面第一个if不赘述
             * 第二个if：没有包含`*`，且itemTags和check.tags没有交集，表示匹配不通过
             */
            if (!check.tags.isEmpty()) {
                List<String> itemTags = ItemStackUtil.getTags(item);
                // check tag
                List<String> withNamespaceCheckTags = check.tags.stream().map(tag -> ItemStackUtil.withDefaultNamespace(tag)).toList();
                if (!check.tags.contains("*") && !CommonUtil.hasIntersection_regMatch(itemTags, withNamespaceCheckTags)) {
                    // AutoDrop.LOGGER.info("tags not match: " + itemTags + " " + withNamespaceCheckTags);
                    continue;
                };
            }

            if (check.enchantments.size() == 0 ||
                check.minEnchantRequir == 0) {    // 匹配通过
                return true;
            }

            if (check.enchantments.size() < check.minEnchantRequir) {
                continue;       // 不可能通过匹配
            }

            int minEnchantRequir = check.minEnchantRequir == -1 ? check.enchantments.size() : check.minEnchantRequir;
            int hasEnchantCount = calEnchantCounts(item, check.enchantments);
            if (hasEnchantCount >= minEnchantRequir) {
                return true;   // ✔
            }
            // AutoDrop.LOGGER.info("enchantments not match.");
        }
        return false;
    }

    private static int calEnchantCounts(ItemStack item, Map<String, Integer> enchantments) {
        int counter = 0;
        ItemEnchantments it = 
            item.is(Items.ENCHANTED_BOOK) ?
            item.getOrDefault(DataComponents.STORED_ENCHANTMENTS, ItemEnchantments.EMPTY) :
            item.getEnchantments();
        for (Holder<Enchantment> enchant: it.keySet()) {
            String ID = enchant.getRegisteredName();
            int level = it.getLevel(enchant);
            if (level >= enchantments.getOrDefault(ID, 666) || 
                level >= enchantments.getOrDefault(ItemStackUtil.withoutDefaultNamespace(ID), 666)) {
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
}

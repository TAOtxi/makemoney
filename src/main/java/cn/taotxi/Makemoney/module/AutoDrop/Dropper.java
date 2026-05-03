package cn.taotxi.Makemoney.module.AutoDrop;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import cn.taotxi.Makemoney.gui.ConfigScreen;
import cn.taotxi.Makemoney.util.CommonUtil;
import cn.taotxi.Makemoney.util.InventoryUtil;
import cn.taotxi.Makemoney.util.ItemStackUtil;
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
    // TODO: 在捡到物品的事件中，调用该方法
    public static void tryToDropItems() {
        Minecraft client = Minecraft.getInstance();
        LocalPlayer player = client.player;

        if (AutoDrop.config.stopWhenCrouch && player.isCrouching()) return;
        if (AutoDrop.config.stopWhenOpenContainer && player.hasContainerOpen()) return; // TODO: BUG: 无法检测是否打开背包
        if (AutoDrop.config.stopWhenOpenConfig && ConfigScreen.isOpenYaclScreen()) return;
        if (AutoDrop.config.triggerMinCount != 0 && AutoDrop.config.triggerMinCount > notEmptySlotCount()) return;

        if (AutoDrop.config.triggerWithItem) {
            ItemStack heldItem = player.getMainHandItem();
            if (!AutoDrop.config.triggerItemName.equals("*") &&
                !ItemStackUtil.equalName(heldItem, AutoDrop.config.triggerItemName)
            )
                return;

            if (!AutoDrop.config.triggerItemId.equals("*") &&
                !ItemStackUtil.equalId(heldItem, AutoDrop.config.triggerItemId)
            )
                return;
        };

        final List<Integer> dropSlots = new ArrayList<>();
        InventoryMenu inventoryMenu = player.inventoryMenu;
        for (int i = InventoryMenu.INV_SLOT_START; i < InventoryMenu.USE_ROW_SLOT_END; i++) {
            if (AutoDrop.config.ingnoreSlots.contains(i)) continue;

            ItemStack item = inventoryMenu.getSlot(i).getItem();

            // AutoDrop.LOGGER.info("Check item {} in slot {}", item.getItemName(), i);
            if (isEqualItem(item)) continue;
            // AutoDrop.LOGGER.info("Dropping item {} in slot {}", ItemStackUtil.getName(item), i);
            dropSlots.add(i);
        }
        if (dropSlots.isEmpty()) return;

        if (AutoDrop.config.isDirectionThrow) {
            dropItemAnywhere(dropSlots, AutoDrop.config.throwDirection);
        } else {
            dropItemAnywhere(dropSlots, AutoDrop.config.throwYaw, AutoDrop.config.throwPitch);
        }
    }

    public static int notEmptySlotCount() {
        return InventoryUtil.getInventoryNotEmptySlots().size();
    }

    public static boolean isEqualItem(ItemStack item) {
        if (item.isEmpty()) return true;

        for (AutoDropConfig.Item check: AutoDrop.config.items){
            if (!check.enabled) continue;

            // TODO: 适配多颜色文本
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

    public static int calEnchantCounts(ItemStack item, Map<String, Integer> enchantments) {
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

    public static void dropItems(List<Integer> slots) {
        Minecraft client = Minecraft.getInstance();
        LocalPlayer player = client.player;
        InventoryMenu inventoryMenu = player.inventoryMenu;
        for (int slot: slots) {
            client.gameMode.handleInventoryMouseClick(inventoryMenu.containerId, slot, 1, ClickType.THROW, player);
        }
    }

    public static void dropItemAnywhere(int slot, String direction) {
        dropItemAnywhere(List.of(slot), direction);
    }

    public static void dropItemAnywhere(List<Integer> slots, float yaw, float pitch) {
        Minecraft client = Minecraft.getInstance();
        float xRot = client.player.getXRot();
        float yRot = client.player.getYRot();

        setPlayerRotation(yaw, pitch);
        dropItems(slots);
        setPlayerRotation(yRot, xRot);
    }

    // TODO: Bug: 创造模式会丢弃两个物品，但背包实际减少的是一个
    public static void dropItemAnywhere(List<Integer> slots, String direction) {
        if (!AutoDropConfig.getAllThrowDirections()
            .stream()
            .map(Enum::name)
            .collect(Collectors.toList())
            .contains(direction.toUpperCase())
        ) {
            AutoDrop.LOGGER.error("Error direction !!!");
            return;
        }

        Minecraft client = Minecraft.getInstance();
        LocalPlayer player = client.player;

        if (direction.toUpperCase().equals(AutoDropConfig.Direction.LOOKING.name())) {
          dropItems(slots);
          return;
        }

        float xRot = player.getXRot();
        float yRot = player.getYRot();
        setPlayerRotation(direction);
        dropItems(slots);
        setPlayerRotation(yRot, xRot);
    }

    public static void setPlayerRotation(String direction) {
        AutoDropConfig.Direction dir = AutoDropConfig.Direction.valueOf(direction.toUpperCase());
        switch (dir) {
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
            case LOOKING:
                break;
            default:
                // never happen
                break;
        }
    }

    public static void setPlayerRotation(float yaw, float pitch) {
        LocalPlayer player = Minecraft.getInstance().player;
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

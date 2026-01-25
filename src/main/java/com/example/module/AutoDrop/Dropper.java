package com.example.module.AutoDrop;


import java.util.List;
import java.util.Map;

import com.example.util.CommonUtil;
import com.example.util.ItemStackUtil;

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
    public static void tryToDropItems() {
        Minecraft client = Minecraft.getInstance();
        LocalPlayer player = client.player;

        // TODO: 待优化
        if (player.hasContainerOpen()) return;

        for (int i = InventoryMenu.INV_SLOT_START; i < InventoryMenu.USE_ROW_SLOT_END; i++) {
            if (AutoDrop.config.ingnoreSlots.contains(i)) continue;

            InventoryMenu inventoryMenu = player.inventoryMenu;
            ItemStack item = inventoryMenu.getSlot(i).getItem();

            // AutoDrop.LOGGER.info("Check item {} in slot {}", item.getItemName(), i);
            if (!shouldDrop(item)) continue;
            // AutoDrop.LOGGER.info("Dropping item {} in slot {}", ItemStackUtil.getName(item), i);
            dropItemAnywhere(i, AutoDrop.config.throwDirection);
        }
    }

    public static boolean shouldDrop(ItemStack item) {
        if (item.isEmpty()) return false;

        for (AutoDropConfig.Item check: AutoDrop.config.items){
            // TODO: 适配多颜色文本
            // check name
            String name = ItemStackUtil.getName(item);
            if (!check.name.equals("*") && !check.name.equals(name)) {
                continue; 
            }
            // check id
            String id = ItemStackUtil.getId(item);
            if (!check.id.equals("*") && !check.id.equals(id)) {
                continue;
            }

            /**
             * 有点绕，梳理下思路
             * 1. 如果check.tags包含`*`，匹配直接通过
             * 2. 如果check.tags为空，即便itemTags也为空，匹配也不通过    TODO: 有待斟酌是否合理
             * 3. 如果itemTags和check.tags有交集，则通过
             * 下面第一个if不赘述
             * 第二个if：没有包含`*`，且itemTags和check.tags没有交集，表示匹配不通过
             */
            List<String> itemTags = ItemStackUtil.getTags(item);
            // check tag
            if (check.tags.isEmpty()) continue;
            if (!check.tags.contains("*") && !CommonUtil.hasIntersection(itemTags, check.tags)) continue;

            int size = check.enchantments.size();
            int needToSatisfiedCounts = size;
            if (size == 0) {    // 匹配通过
                return false;
            }
            if (!check.isAllEnchantment && size > 2) {
                needToSatisfiedCounts = 2;
            }
            if (calEnchantCounts(item, check.enchantments) >= needToSatisfiedCounts) {
                return false;   // ✔
            }
        }
        return true;
    }

    // TODO: 验证附魔书是否适用此函数
    public static int calEnchantCounts(ItemStack item, Map<String, Integer> enchantments) {
        int counter = 0;
        ItemEnchantments it = 
            item.is(Items.ENCHANTED_BOOK) ?
            item.getOrDefault(DataComponents.STORED_ENCHANTMENTS, ItemEnchantments.EMPTY) :
            item.getEnchantments();
        for (Holder<Enchantment> enchant: it.keySet()) {
            String ID = enchant.getRegisteredName();
            int level = it.getLevel(enchant);
            if (enchantments.containsKey(ID) && enchantments.getOrDefault(ID, -666) == level) {
                counter++;
            }
        }
        return counter;
    }

    // TODO: Bug: 创造模式会丢弃两个物品，但背包实际减少的是一个
    public static void dropItemAnywhere(int slot, String direction) {
        // if (!AutoDropConfig.getAllThrowDirections().contains(direction)) {
        //     AutoDrop.LOGGER.error("Error direction !!!");
        // }

        Minecraft client = Minecraft.getInstance();
        LocalPlayer player = client.player;
        InventoryMenu inventoryMenu = player.inventoryMenu;

        if (direction.equals(AutoDropConfig.Direction.LOOKING)) {
            client.gameMode.handleInventoryMouseClick(inventoryMenu.containerId, slot, 1, ClickType.THROW, player);
            return;
        }

        float xRot = player.getXRot();
        float yRot = player.getYRot();
        setPlayerRotation(direction);
        client.gameMode.handleInventoryMouseClick(inventoryMenu.containerId, slot, 1, ClickType.THROW, player);
        setPlayerRotation(yRot, xRot);
    }

    public static void setPlayerRotation(String direction) {
        switch (direction) {
            case AutoDropConfig.Direction.UP:
                setPlayerRotation(0, -90);
                break;
            case AutoDropConfig.Direction.DOWN:
                setPlayerRotation(0, 90);
                break;
            case AutoDropConfig.Direction.EAST:
                setPlayerRotation(-90, 0);
                break;
            case AutoDropConfig.Direction.WEST:
                setPlayerRotation(90, 0);
                break;
            case AutoDropConfig.Direction.NORTH:
                setPlayerRotation(-180, 0);
                break;
            case AutoDropConfig.Direction.SOUTH:
                setPlayerRotation(0, 0);
                break;
            case AutoDropConfig.Direction.LOOKING:
                break;
            default:
                AutoDrop.LOGGER.error("Error direction: {} !!!", direction);
                break;
        }
    }

    // TODO: 测试在服务端的表现
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

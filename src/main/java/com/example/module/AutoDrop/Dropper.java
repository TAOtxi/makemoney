package com.example.module.AutoDrop;


import java.util.Map;

import com.example.Makemoney;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.InventoryMenu;


public class Dropper {
    public static void tryToDropItems() {
        for (int i = InventoryMenu.INV_SLOT_START; i < InventoryMenu.USE_ROW_SLOT_END - InventoryMenu.INV_SLOT_START; i++) {
            if (AutoDrop.config.ingnoreSlots.contains(i)) continue;

            InventoryMenu inventoryMenu = Minecraft.getInstance().player.inventoryMenu;
            ItemStack item = inventoryMenu.getSlot(i).getItem();

            if (!shouldDrop(item)) return;
            dropItemAnywhere(i, AutoDrop.config.throwDirection);
        }
    }

    public static boolean shouldDrop(ItemStack item) {
        if (item.isEmpty()) return false;

        for (AutoDropConfig.Item check: AutoDrop.config.items){
            // check name
            String name = item.getCustomName().getString();
            if (!check.name.equals("*") && !check.name.equals(name)) {
                continue; 
            }
            // TODO: 待测试
            // check id
            String id = BuiltInRegistries.ITEM.getKey(item.getItem()).toString();
            if (!check.id.equals("*") && !check.id.equals(id)) {
                continue;
            }
            // check tag
            boolean hasAnyTag = false;
            for (String tag : check.tags) {
                if (tag.equals("*") || hasTag(item, tag)) {
                    hasAnyTag = true;
                    break;
                }
            }
            if (!hasAnyTag) continue;

            int size = check.enchantments.size();
            int needToSatisfiedCounts = size;
            if (size == 0) {
                return false;
            }
            if (!check.isAllEnchantment && size > 2) {
                needToSatisfiedCounts = 2;
            }
            if (calEnchantCounts(item, check.enchantments) >= needToSatisfiedCounts) {
                return false;
            }
        }
        return true;
    }

    public static boolean hasTag(ItemStack item, String tag) {
        return false;
    }

    // TODO: 验证附魔书是否适用此函数
    public static int calEnchantCounts(ItemStack item, Map<String, Integer> enchantments) {
        int counter = 0;
        ItemEnchantments it = 
            item.is(Items.ENCHANTED_BOOK) ?
            item.getOrDefault(DataComponents.STORED_ENCHANTMENTS, ItemEnchantments.EMPTY) :
            item.getEnchantments();
        for (Holder<Enchantment> enchant: it.keySet()) {
            String ID = enchant.getRegisteredName();    // TODO: to be checked
            int level = it.getLevel(enchant);
            if (enchantments.containsKey(ID) && enchantments.getOrDefault(ID, -666) == level) {
                counter++;
            }
        }
        return counter;
    }

    public static void dropItemAnywhere(int slot, String direction) {
        // if (!AutoDropConfig.getAllThrowDirections().contains(direction)) {
        //     AutoDrop.LOGGER.error("Error direction !!!");
        // }

        Minecraft client = Minecraft.getInstance();
        LocalPlayer player = client.player;
        InventoryMenu inventoryMenu = player.inventoryMenu;
        if (direction.equals(AutoDropConfig.Direction.LOOKING)) {
            client.gameMode.handleInventoryMouseClick(inventoryMenu.containerId, slot, 0, ClickType.THROW, player);
            return;
        }

        float xRot = player.getXRot();
        float yRot = player.getYRot();
        setPlayerRotation(direction);
        client.gameMode.handleInventoryMouseClick(inventoryMenu.containerId, slot, 0, ClickType.THROW, player);
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

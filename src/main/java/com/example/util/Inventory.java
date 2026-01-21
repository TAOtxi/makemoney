package com.example.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.example.module.AutoDrop.AutoDrop;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;

public class Inventory {
    private static final Minecraft client = Minecraft.getInstance();

    // TODO: 待测试
    public static List<Integer> getInventoryNotEmptySlots() {
        LocalPlayer player = client.player;
        InventoryMenu inventoryMenu = player.inventoryMenu;
        List<Integer> result = new ArrayList<>();
        for (int i = 0; i < InventoryMenu.USE_ROW_SLOT_END - InventoryMenu.ARMOR_SLOT_END; i++) {
            ItemStack item = inventoryMenu.getSlot(i).getItem();
            
            if (!item.isEmpty()) continue;
            result.add(i);
        }
        return result;
    }
}

package com.example.util;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.ClickType;

public class SwapSlot {
    public static void swapSlot(Player player, int slot1, int slot2) {
        Minecraft client = Minecraft.getInstance();

        slot1 = translateSlot(slot1);
        slot2 = translateSlot(slot2);
        
        InventoryMenu screenHandler = player.inventoryMenu;
        client.gameMode.handleInventoryMouseClick(screenHandler.containerId, slot1, 0, ClickType.SWAP, player);
        client.gameMode.handleInventoryMouseClick(screenHandler.containerId, slot2, 0, ClickType.SWAP, player);
        client.gameMode.handleInventoryMouseClick(screenHandler.containerId, slot1, 0, ClickType.SWAP, player);
    }

    public static int translateSlot(int slot) {
        if (slot == Inventory.SLOT_OFFHAND) {
            return 45;
        } else if (slot < 9) {
            return slot + 36;
        }
        return slot;
    }
}
package cn.taotxi.Makemoney.util;


import cn.taotxi.Makemoney.module.AutoRepair.AutoRepair;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.ClickType;

public class SwapSlot {
    public static Minecraft client = Minecraft.getInstance();
    
    public static void swaper(int slot1, int slot2) {
        Player player = client.player;
        if (player.containerMenu instanceof InventoryMenu inventory) {
            inventorySwaper(inventory, slot1, slot2);
        } else if (player.containerMenu instanceof AnvilMenu anvil) {
            anvilSwaper(anvil, slot1, slot2);
        // } else if (player.containerMenu instanceof InventoryMenu inventory) {
        //     inventorySwaper(inventory, slot1, slot2);
        }
    }

    public static void inventorySwaper(InventoryMenu inventory, int slot1, int slot2) {
        Player player = client.player;
        // slot1 = inventoryTranslator(slot1);
        // slot2 = inventoryTranslator(slot2);

        client.gameMode.handleInventoryMouseClick(inventory.containerId, slot1, 0, ClickType.SWAP, player);
        client.gameMode.handleInventoryMouseClick(inventory.containerId, slot2, 0, ClickType.SWAP, player);
        client.gameMode.handleInventoryMouseClick(inventory.containerId, slot1, 0, ClickType.SWAP, player);
    }
    
    // public static int inventoryTranslator(int slot) {
    //     if (slot == Inventory.SLOT_OFFHAND) {
    //         return InventoryMenu.SHIELD_SLOT;
    //     } else if (slot < 9) {
    //         return slot + InventoryMenu.USE_ROW_SLOT_START;
    //     }
    //     return slot;
    // }

    public static void anvilSwaper(AnvilMenu anvil, int inventorySlot, int anvilSlot) {
        Player player = client.player;
        if (anvilSlot != AnvilMenu.INPUT_SLOT && anvilSlot != AnvilMenu.ADDITIONAL_SLOT) {
            throw new IllegalArgumentException("anvilSlot must be AnvilMenu.INPUT_SLOT or AnvilMenu.ADDITIONAL_SLOT");
        }
        AutoRepair.LOGGER.info("anvilSwaper: move item from slot {} to slot {}", inventorySlot, anvilSlot);

        client.gameMode.handleInventoryMouseClick(anvil.containerId, inventorySlot, AnvilMenu.RESULT_SLOT, ClickType.SWAP, player);
        client.gameMode.handleInventoryMouseClick(anvil.containerId, anvilSlot, AnvilMenu.RESULT_SLOT, ClickType.SWAP, player);
        client.gameMode.handleInventoryMouseClick(anvil.containerId, inventorySlot, AnvilMenu.RESULT_SLOT, ClickType.SWAP, player);
    }
}
package cn.taotxi.Makemoney.util.game;

import java.util.List;
import java.util.ArrayList;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.InventoryMenu;

public class InventoryUtil {
    public static List<Integer> getInventoryNotEmptySlots() {
        InventoryMenu inventoryMenu = Minecraft.getInstance().player.inventoryMenu;
        List<Integer> result = new ArrayList<>();
        for (int i = InventoryMenu.INV_SLOT_START; i < InventoryMenu.USE_ROW_SLOT_END; i++) {
            ItemStack item = inventoryMenu.getSlot(i).getItem();
            
            if (item.isEmpty()) continue;
            result.add(i);
        }
        return result;
    }

    public static void swapInventoryItem(int slot1, int slot2) {
        Minecraft client = Minecraft.getInstance();
        LocalPlayer player = client.player;
        InventoryMenu inventoryMenu = player.inventoryMenu;
        if (inventoryMenu != player.containerMenu) {
            throw new IllegalArgumentException("Inventory must be player container menu");
        };

        client.gameMode.handleInventoryMouseClick(inventoryMenu.containerId, slot1, 0, ClickType.SWAP, player);
        client.gameMode.handleInventoryMouseClick(inventoryMenu.containerId, slot2, 0, ClickType.SWAP, player);
        client.gameMode.handleInventoryMouseClick(inventoryMenu.containerId, slot1, 0, ClickType.SWAP, player);
    }
}

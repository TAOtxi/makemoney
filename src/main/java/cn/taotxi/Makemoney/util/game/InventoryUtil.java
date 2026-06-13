package cn.taotxi.Makemoney.util.game;

import java.util.List;
import java.util.ArrayList;

import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;

public class InventoryUtil {
    private static final Minecraft client = Minecraft.getInstance();

    public static List<Integer> getInventoryNotEmptySlots() {
        InventoryMenu inventoryMenu = client.player.inventoryMenu;
        List<Integer> result = new ArrayList<>();
        for (int i = InventoryMenu.INV_SLOT_START; i < InventoryMenu.USE_ROW_SLOT_END; i++) {
            ItemStack item = inventoryMenu.getSlot(i).getItem();
            
            if (item.isEmpty()) continue;
            result.add(i);
        }
        return result;
    }

    public static void swapInventoryItem(int slot1, int slot2) {
        if (client.player.hasContainerOpen()) {
            throw new IllegalArgumentException("Inventory must be player container menu");
        };
        int containerId = client.player.inventoryMenu.containerId;

        client.gameMode.handleInventoryMouseClick(containerId, slot1, 0, ClickType.SWAP, client.player);
        client.gameMode.handleInventoryMouseClick(containerId, slot2, 0, ClickType.SWAP, client.player);
        client.gameMode.handleInventoryMouseClick(containerId, slot1, 0, ClickType.SWAP, client.player);
    }

    public static void swapItemToHand(InteractionHand hand, int slotNum) {
        if (client.player.hasContainerOpen()) {
            throw new IllegalArgumentException("Inventory must be player container menu");
        };
        
        if (hand == InteractionHand.MAIN_HAND) {
            Inventory inventory = client.player.getInventory();
            if (slotNum >= InventoryMenu.USE_ROW_SLOT_START && slotNum < InventoryMenu.USE_ROW_SLOT_END) {
                inventory.setSelectedSlot(slotNum - InventoryMenu.USE_ROW_SLOT_START);
                client.getConnection().send(new ServerboundSetCarriedItemPacket(inventory.getSelectedSlot()));
            } else {
                client.gameMode.handleInventoryMouseClick(
                    client.player.inventoryMenu.containerId,
                    slotNum,
                    inventory.getSelectedSlot(),
                    ClickType.SWAP,
                    client.player
                );
            }
        } else {
            client.gameMode.handleInventoryMouseClick(
                client.player.inventoryMenu.containerId,
                slotNum,
                Inventory.SLOT_OFFHAND,
                ClickType.SWAP,
                client.player
            );
        }
    }
}

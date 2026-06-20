package cn.taotxi.Makemoney.util.game;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.ArrayList;

import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.CrafterMenu;
import net.minecraft.world.inventory.DispenserMenu;
import net.minecraft.world.inventory.HopperMenu;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.ShulkerBoxMenu;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.world.level.block.DoubleBlockCombiner.BlockType;
import net.minecraft.world.level.block.ShulkerBoxBlock;

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

    public static boolean inventoryHasEmptySlot() {
        for (int i = InventoryMenu.INV_SLOT_START; i < InventoryMenu.USE_ROW_SLOT_END; i++) {
            ItemStack item = client.player.inventoryMenu.getSlot(i).getItem();
            if (item.isEmpty()) return true;
        }
        return false;
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

    public static List<Integer> findSuitableSlots(Predicate<ItemStack> predicate) {
        List<Integer> result = new ArrayList<>();
        InventoryMenu inventoryMenu = client.player.inventoryMenu;
        for (int i = InventoryMenu.INV_SLOT_START; i < InventoryMenu.USE_ROW_SLOT_END; i++) {
            ItemStack item = inventoryMenu.getSlot(i).getItem();
            if (predicate.test(item)) {
                result.add(i);
            }
        }
        return result;
    }

    public static Map.Entry<Integer, Integer> getContainerSlotRange(BlockState blockState) {
        Block block = blockState.getBlock();

        if (block instanceof ChestBlock) {
            if (ChestBlock.getBlockType(blockState) == BlockType.SINGLE) {
                return Map.entry(0, 26);
            }
            return Map.entry(0, 53);
        }

        if (
            blockState.is(Blocks.ENDER_CHEST) ||
            blockState.is(Blocks.BARREL) ||
            block instanceof ShulkerBoxBlock
        ) {
            return Map.entry(0, 26);
        }

        if (blockState.is(Blocks.HOPPER)) {
            return Map.entry(0, 4);
        }

        if (
            blockState.is(Blocks.DISPENSER) ||
            blockState.is(Blocks.DROPPER)
        ) {
            return Map.entry(0, 8);
        }

        if (blockState.is(Blocks.CRAFTER)) {
            return Map.entry(1, 9);
        }

        return null;
    }

    public static Map.Entry<Integer, Integer> getContainerSlotRange(AbstractContainerMenu containerMenu) {
        int startSlot = 0;
        int endSlot = -1;

        if (containerMenu instanceof InventoryMenu) {
            startSlot = InventoryMenu.INV_SLOT_START;
            endSlot = InventoryMenu.USE_ROW_SLOT_END - 1;
        } else if (containerMenu instanceof ChestMenu chestMenu) {
            endSlot = chestMenu.getContainer().getContainerSize() - 1;
        } else if (containerMenu instanceof ShulkerBoxMenu) {
            endSlot = 27 - 1;
        } else if (containerMenu instanceof HopperMenu) {
            endSlot = 5 - 1;
        } else if (containerMenu instanceof DispenserMenu) {
            endSlot = 9 - 1;
        } else if (containerMenu instanceof CrafterMenu) {
            startSlot = 1;
            endSlot = startSlot + 9 - 1;
        }

        if (endSlot < startSlot) {
            return null;
        }
        
        return Map.entry(startSlot, endSlot);
    }
}

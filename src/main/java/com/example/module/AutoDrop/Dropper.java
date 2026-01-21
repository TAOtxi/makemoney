package com.example.module.AutoDrop;


import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.InventoryMenu;


public class Dropper {
    private static final Minecraft client = Minecraft.getInstance();
    public static void tryToDropItems() {
        LocalPlayer player = client.player;
        InventoryMenu inventoryMenu = player.inventoryMenu;

        for (int i = InventoryMenu.INV_SLOT_START; i < InventoryMenu.USE_ROW_SLOT_END - InventoryMenu.INV_SLOT_START; i++) {
            if (AutoDrop.config.ingnoreSlots.contains(i)) continue;
            ItemStack item = inventoryMenu.getSlot(i).getItem();
            if (!isValidItem(item)) {
                float x = player.getXRot();
                float y = player.getYRot();

                client.gameMode.handleInventoryMouseClick(inventoryMenu.containerId, i, 0, ClickType.THROW, player);
            };
        }
        
    }

    public static boolean isValidItem(ItemStack item) {
        if (item.isEmpty()) return false;
        if (item.getCount() <= 0) return false;
        return true;
    }

    public static void setPlayerDirection(String direction) {
        switch (direction) {
            case "up":
                setPlayerRotation(0, -90);
                break;
            case "down":
                setPlayerRotation(0, 90);
                break;
            case "east":
                setPlayerRotation(0, 0);
                break;
            case "west":
                setPlayerRotation(180, 0);
                break;
            case "north":
                setPlayerRotation(90, 0);
                break;
            case "south":
                setPlayerRotation(-90, 0);
                break;
            // case "looking":
            //     setPlayerRotation(player.getYRot(), player.getXRot());
            //     break;
            default:
                break;
        }
    }

    public static void setPlayerRotation(float yaw, float pitch) {
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

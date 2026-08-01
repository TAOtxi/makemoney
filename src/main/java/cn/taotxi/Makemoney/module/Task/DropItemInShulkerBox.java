package cn.taotxi.Makemoney.module.Task;

import java.util.Map;

import com.mojang.brigadier.context.CommandContext;

import cn.taotxi.Makemoney.util.Message;
import cn.taotxi.Makemoney.util.T;
import cn.taotxi.Makemoney.util.TaskUtil;
import cn.taotxi.Makemoney.util.game.InventoryUtil;
import cn.taotxi.Makemoney.util.game.ItemStackUtil;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.Minecraft;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerInputPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.ShulkerBoxMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;

public class DropItemInShulkerBox {
    private static final Minecraft client = Minecraft.getInstance();
    public static final String TASK_NAME = "dropAllItemFromShulkerBox";
    private static boolean enabled = false;

    public static int enable(CommandContext<FabricClientCommandSource> context) {
        if (!TaskUtil.hasTimeTask(TASK_NAME)) {
            TaskUtil.createTimeTask(TASK_NAME, DropItemInShulkerBox::run, 2);
        }
        enabled = true;
        Message.actionBarMsg(T.tl("task.dropItemInShulkerBox.on"));
        return 1;
    }

    public static int disable(CommandContext<FabricClientCommandSource> context) {
        cancel();
        Message.actionBarMsg(T.tl("task.dropItemInShulkerBox.off"));
        return 1;
    }

    private static void cancel() {
        enabled = false;
        TaskUtil.removeTimeTask(TASK_NAME);
    }

    public static void onOpenContainer() {
        if (!enabled) return;

        if (!isOpenShulkerBox()) return;
        dropAllItemInShulkerBox();
        client.player.closeContainer();
        ItemStack handItem = client.player.getMainHandItem();
        if (ItemStackUtil.getId(handItem).endsWith("shulker_box")) {
            dropHandItem();
        }
    }

    private static void run() {
        if (client.player == null) {
            cancel();
            return;
        }
        if (client.player.hasContainerOpen() && !isOpenShulkerBox()) {
            client.player.closeContainer();
            return;
        }

        int slot = findNotEmptyShulkerBox();
        if (slot == -1) {
            cancel();
            Message.actionBarMsg(T.tl("task.dropItemInShulkerBox.off"));
            return;
        }

        int handSlot = client.player.getInventory().getSelectedSlot();
        client.gameMode.handleInventoryMouseClick(
            client.player.containerMenu.containerId, slot, handSlot, ClickType.SWAP, client.player
        );

        Input shiftInput = new Input(
            client.player.input.keyPresses.forward(),
            client.player.input.keyPresses.backward(),
            client.player.input.keyPresses.left(),
            client.player.input.keyPresses.right(),
            client.player.input.keyPresses.jump(),
            true,
            client.player.input.keyPresses.sprint()
        );
        client.player.connection.send(new ServerboundPlayerInputPacket(shiftInput));

        client.gameMode.useItem(client.player, InteractionHand.MAIN_HAND);
        Input noShiftInput = new Input(
            client.player.input.keyPresses.forward(),
            client.player.input.keyPresses.backward(),
            client.player.input.keyPresses.left(),
            client.player.input.keyPresses.right(),
            client.player.input.keyPresses.jump(),
            false,
            client.player.input.keyPresses.sprint()
        );
        client.player.connection.send(new ServerboundPlayerInputPacket(noShiftInput));
    }

    private static int findNotEmptyShulkerBox() {
        AbstractContainerMenu menu = client.player.containerMenu;
        Map.Entry<Integer, Integer> slotRange = InventoryUtil.getContainerSlotRange(menu);
        if (slotRange == null) {
            throw new IllegalArgumentException("Not support menu");
        }
        int startSlot = slotRange.getKey();
        int endSlot = slotRange.getValue();
        for (int i = startSlot; i <= endSlot; i++) {
            ItemStack item = menu.getSlot(i).getItem();
            if (!ItemStackUtil.equalId(item, "/^minecraft:.*?shulker_box$/")) {
                continue;
            }
            ItemContainerContents container = item.getComponents().get(DataComponents.CONTAINER);
            if (container != null) {
                return i;
            }
        }
        return -1;
    }



    private static void dropHandItem() {
        float originYaw = client.player.getYRot();
        float originPitch = client.player.getXRot();

        client.player.connection.send(
            new ServerboundMovePlayerPacket.Rot(
                0, 0,
                client.player.onGround(),
                false
            )
        );

        int handSlot = client.player.getInventory().getSelectedSlot() + 36;
        client.gameMode.handleInventoryMouseClick(
            client.player.inventoryMenu.containerId, handSlot, 1, ClickType.THROW, client.player
        );

        client.player.connection.send(
            new ServerboundMovePlayerPacket.Rot(
                originYaw, originPitch,
                client.player.onGround(),
                false
            )
        );
    }

    private static boolean isOpenShulkerBox() {
        return client.player.containerMenu instanceof ShulkerBoxMenu;
    }

    private static void dropAllItemInShulkerBox() {
        AbstractContainerMenu menu = client.player.containerMenu;
        Map.Entry<Integer, Integer> slotRange = InventoryUtil.getContainerSlotRange(menu);
        if (slotRange == null) {
            throw new IllegalArgumentException("Not in Shulker Box");
        }
        int startSlot = slotRange.getKey();
        int endSlot = slotRange.getValue();
        for (int i = startSlot; i <= endSlot; i++) {
            if (!menu.getSlot(i).getItem().isEmpty()) {
                client.gameMode.handleInventoryMouseClick(
                    menu.containerId, i, 1, ClickType.THROW, client.player
                );
            }
        }
    }
}

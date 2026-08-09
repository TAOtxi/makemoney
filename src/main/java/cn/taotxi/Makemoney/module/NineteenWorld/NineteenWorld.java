package cn.taotxi.Makemoney.module.NineteenWorld;

import cn.taotxi.Makemoney.util.game.ItemStackUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.game.ServerboundPlayerInputPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.HitResult;

public class NineteenWorld {
    private static final Minecraft client = Minecraft.getInstance();
    private static final NineteenWorldConfig CONFIG = NineteenWorldConfig.getInstance();
    public static final String MODULE_NAME = "nineteenworld";

    public static void initialize() {
        NineteenWorldConfig.getInstance().loadConfig();
        AutoRide.initialize();
        IgnoreMessage.initialize();
    }

    public static void onUseItem(InteractionHand interactionHand) {
        if (
            !CONFIG.rightClickOpenShulkerBoxEnabled.getValue() ||
            client.hitResult == null ||
            client.hitResult.getType() == HitResult.Type.BLOCK
        ) {
            return;
        }
        ItemStack handItem = client.player.getItemInHand(interactionHand);
        if (handItem.isEmpty()) return;

        if (!ItemStackUtil.getId(handItem).endsWith("shulker_box")) {
            return;
        }
        if (client.player.isCrouching()) return;

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
    }
}

package cn.taotxi.Makemoney.module.StrangeFunction;

import java.util.List;

import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import cn.taotxi.Makemoney.util.Message;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.PlayerRideable;
import net.minecraft.world.entity.animal.happyghast.HappyGhast;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.VehicleEntity;
import net.minecraft.world.phys.EntityHitResult;

public class RightClickRide {
    // TODO: 右键一次，会调用此函数两次，待修复
    public static void handleInteract(
        Player player, 
        Entity entity, 
        EntityHitResult entityHitResult, 
        InteractionHand interactionHand, 
        CallbackInfoReturnable<InteractionResult> cir) 
    {
        if (!isEnabled(false)) return;

        Minecraft client = Minecraft.getInstance();
        Entity lookEntity = client.crosshairPickEntity;

        if (lookEntity == null) return;

        if (lookEntity instanceof HappyGhast ||
            lookEntity instanceof PlayerRideable ||
            lookEntity instanceof VehicleEntity ||
            lookEntity instanceof Player
        ) {
            return;
        }

        if (!player.getItemInHand(interactionHand).isEmpty()) return;

        Message.sendMessage("/ride");
    }

    public static boolean isEnabled(boolean forceDefault) {
        return StrangeConfig.getInstance().getBoolean("rightClickRide_enabled", forceDefault);
    }

    public static void setEnabled(boolean enabled) {
        StrangeConfig.getInstance().putBoolean("rightClickRide_enabled", enabled);
    }
}

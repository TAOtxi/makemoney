package cn.taotxi.Makemoney.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import cn.taotxi.Makemoney.module.StrangeFunction.RightClickRide;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.EntityHitResult;

@Mixin(MultiPlayerGameMode.class)
public class MultiPlayerGameModeMixin {

    @Inject(method = "interactAt", at = @At("HEAD"), cancellable = true)
    public void interactAtBefore(Player player, Entity entity, EntityHitResult entityHitResult, InteractionHand interactionHand, CallbackInfoReturnable<InteractionResult> cir) {
        RightClickRide.handleInteract(player, entity, entityHitResult, interactionHand, cir);
    }
}

package cn.taotxi.Makemoney.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import cn.taotxi.Makemoney.module.AutoFish.AutoFish;
import cn.taotxi.Makemoney.module.NineteenWorld.NineteenWorld;
import cn.taotxi.Makemoney.module.NineteenWorld.RightClickRide;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.EntityHitResult;

@Mixin(MultiPlayerGameMode.class)
public class MultiPlayerGameModeMixin {

    @Inject(method = "interact", at = @At("HEAD"))
    public void interactBefore(final Player player, final Entity entity, final EntityHitResult hitResult, final InteractionHand interactionHand, CallbackInfoReturnable<InteractionResult> cir) {
        RightClickRide.handleInteract(player, entity, interactionHand);
    }

    @Inject(method = "useItem", at = @At("HEAD"))
    public void useItemBefore(final Player player, final InteractionHand interactionHand, CallbackInfoReturnable<InteractionResult> cir) {
        AutoFish.initRotaion(player, interactionHand);
        NineteenWorld.onUseItem(interactionHand);
    }
}

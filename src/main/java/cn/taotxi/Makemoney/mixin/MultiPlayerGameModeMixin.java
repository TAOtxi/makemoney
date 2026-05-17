package cn.taotxi.Makemoney.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import cn.taotxi.Makemoney.module.AutoFish.AutoFish;
import cn.taotxi.Makemoney.module.StrangeFunction.RightClickRide;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

@Mixin(MultiPlayerGameMode.class)
public class MultiPlayerGameModeMixin {

    @Inject(method = "interact", at = @At("HEAD"))
    public void interactBefore(Player player, Entity entity, InteractionHand interactionHand, CallbackInfoReturnable<InteractionResult> cir) {
        RightClickRide.handleInteract(player, entity, interactionHand, cir);
    }

    @Inject(method = "useItem", at = @At("HEAD"))
    public void useItemBefore(Player player, InteractionHand interactionHand, CallbackInfoReturnable<InteractionResult> cir) {
        AutoFish.initRotaion(player, interactionHand, cir);
    }
}

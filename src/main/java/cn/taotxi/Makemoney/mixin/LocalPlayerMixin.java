package cn.taotxi.Makemoney.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;

import cn.taotxi.Makemoney.module.NineteenWorld.NineteenWorld;

@Mixin(LocalPlayer.class)
public class LocalPlayerMixin {
	@Inject(method = "closeContainer", at = @At("HEAD"))
	private void onLocalPlayerCloseContainer(CallbackInfo ci) {
		// NineteenWorld.onCloseContainer(Minecraft.getInstance().player.containerMenu.containerId);
	}
}
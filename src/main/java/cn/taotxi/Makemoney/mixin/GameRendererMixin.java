package cn.taotxi.Makemoney.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.renderer.GameRenderer;
import cn.taotxi.Makemoney.Makemoney;
import cn.taotxi.Makemoney.module.EntityHighlightBox.render.CustomRenderPipeline;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
	@Inject(method = "close", at = @At("RETURN"))
	private void onGameRendererClose(CallbackInfo ci) {
		CustomRenderPipeline.close();
		Makemoney.LOGGER.info("CustomRenderPipeline closed");
	}
}
package cn.taotxi.Makemoney.mixin;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import cn.taotxi.Makemoney.module.NineteenWorld.TabListSort;
import net.minecraft.client.gui.components.PlayerTabOverlay;
import net.minecraft.client.multiplayer.PlayerInfo;

@Mixin(PlayerTabOverlay.class)
public class PlayerTabOverlayMixin {

    @Inject(method = "getPlayerInfos", at = @At("HEAD"), cancellable = true)
    private void onGetPlayerInfos(CallbackInfoReturnable<List<PlayerInfo>> cir) {
        List<PlayerInfo> sorted = TabListSort.getSortedPlayerInfos();
        if (sorted != null) {
            cir.setReturnValue(sorted);
        }
    }
}

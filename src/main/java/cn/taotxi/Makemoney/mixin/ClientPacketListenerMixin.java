package cn.taotxi.Makemoney.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientCommonPacketListenerImpl;
import net.minecraft.client.multiplayer.CommonListenerCookie;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.TickablePacketListener;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import cn.taotxi.Makemoney.module.StrangeFunction.IgnoreMessage;
import cn.taotxi.Makemoney.module.AutoFish.AutoFish;


@Mixin(ClientPacketListener.class)
public abstract class ClientPacketListenerMixin extends ClientCommonPacketListenerImpl implements TickablePacketListener, ClientGamePacketListener {
    protected ClientPacketListenerMixin(Minecraft client, Connection connection, CommonListenerCookie connectionState) {
        super(client, connection, connectionState);
    }

    @Inject(method = "handleSystemChat", at = @At("HEAD"), cancellable = true)
    public void onChatMessage(ClientboundSystemChatPacket chatMessageS2CPacket_1, CallbackInfo ci) {
        if (minecraft.isSameThread()) {
            IgnoreMessage.handleChatMessage(chatMessageS2CPacket_1, ci);
        };
    }

    @Inject(method = "handleSetEntityData", at = @At("HEAD"))
    public void onSetEntityData(ClientboundSetEntityDataPacket clientboundSetEntityDataPacket, CallbackInfo ci) {
        if (minecraft.isSameThread()) {
            AutoFish.onEntitySetData(clientboundSetEntityDataPacket);
        };
    }

    @Inject(method = "handleAddEntity", at = @At("TAIL"))
    public void onAddEntity(ClientboundAddEntityPacket clientboundAddEntityPacket, CallbackInfo ci) {
        if (minecraft.isSameThread()) {
            AutoFish.onEntityAdd(clientboundAddEntityPacket);
        };
    }
}

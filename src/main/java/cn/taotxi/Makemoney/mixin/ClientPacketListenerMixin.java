package cn.taotxi.Makemoney.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientCommonPacketListenerImpl;
import net.minecraft.client.multiplayer.CommonListenerCookie;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.ClientboundContainerClosePacket;
import net.minecraft.network.protocol.game.ClientboundContainerSetContentPacket;
import net.minecraft.network.protocol.game.ClientboundOpenScreenPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.protocol.game.ClientboundSetPassengersPacket;
import net.minecraft.network.protocol.game.ClientboundSetTimePacket;
import net.minecraft.network.TickablePacketListener;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;
import net.minecraft.network.protocol.game.ClientboundTakeItemEntityPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.item.ItemEntity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import cn.taotxi.Makemoney.module.Task.DropItemInShulkerBox;
import cn.taotxi.Makemoney.module.AutoAFK.calcServerTps;
import cn.taotxi.Makemoney.module.AutoDrop.AutoDrop;
import cn.taotxi.Makemoney.module.AutoDrop.Dropper;
import cn.taotxi.Makemoney.module.AutoFish.AutoFish;
import cn.taotxi.Makemoney.module.MessageCommand.MessageCommand;
import cn.taotxi.Makemoney.module.NineteenWorld.AutoRide;
import cn.taotxi.Makemoney.module.NineteenWorld.IgnoreMessage;
// import cn.taotxi.Makemoney.module.NineteenWorld.NineteenWorld;
import cn.taotxi.Makemoney.module.MendingHelper.AutoEnchantMending;
import cn.taotxi.Makemoney.module.MendingHelper.AutoMendingReplace;
import cn.taotxi.Makemoney.module.MendingHelper.AutoRepair;


@Mixin(ClientPacketListener.class)
public abstract class ClientPacketListenerMixin extends ClientCommonPacketListenerImpl implements TickablePacketListener, ClientGamePacketListener {
    protected ClientPacketListenerMixin(Minecraft client, Connection connection, CommonListenerCookie connectionState) {
        super(client, connection, connectionState);
    }

    @Inject(method = "handleSystemChat", at = @At("HEAD"), cancellable = true)
    public void onChatMessage(ClientboundSystemChatPacket packet, CallbackInfo ci) {
        if (minecraft.isSameThread()) {
            String message = packet.content().getString();
            MessageCommand.onMessage(message);
            IgnoreMessage.handleChatMessage(message, ci);
        };
    }

    @Inject(method = "handleSetEntityData", at = @At("HEAD"))
    public void onSetEntityData(ClientboundSetEntityDataPacket packet, CallbackInfo ci) {
        if (minecraft.isSameThread()) {
            AutoFish.onEntitySetData(packet);
        };
    }

    @Inject(method = "handleAddEntity", at = @At("TAIL"))
    public void onAddEntity(ClientboundAddEntityPacket packet, CallbackInfo ci) {
        if (minecraft.isSameThread()) {
            AutoFish.onEntityAdd(packet);
        };
    }

    @Inject(method = "handleTakeItemEntity", at = @At("HEAD"))
    public void onTakeItemEntity(ClientboundTakeItemEntityPacket packet, CallbackInfo ci) {
        if (minecraft.isSameThread() && packet.getPlayerId() == minecraft.player.getId()) {
            Entity entity = minecraft.level.getEntity(packet.getItemId());
            if (entity == null) return;
            if (entity instanceof ItemEntity itemEntity) {
                AutoDrop.onTakeItemEntity(itemEntity);
            } else if (entity instanceof ExperienceOrb) {
                AutoMendingReplace.tryToReplaceOffHand();
            }
        };
    }

    @Inject(method = "handleSetEntityPassengersPacket", at = @At("HEAD"))
    public void onSetEntityPassengersPacket(ClientboundSetPassengersPacket packet, CallbackInfo ci) {
        if (minecraft.isSameThread()) {
            AutoRide.onEntityRidePlayer(packet);
        };
    }

    @Inject(method = "handleOpenScreen", at = @At("TAIL"))
    public void onOpenScreen(ClientboundOpenScreenPacket packet, CallbackInfo ci) {
        if (minecraft.isSameThread()) {
            AutoEnchantMending.onOpenAnvil();
        };
    }
    
    // @Inject(method = "handleContainerClose", at = @At("TAIL"))
    // public void onCloseContainer(ClientboundContainerClosePacket packet, CallbackInfo ci) {
    //     if (minecraft.isSameThread()) {
    //     };
    // }

    @Inject(method = "handleContainerContent", at = @At("TAIL"))
    public void onContainerContent(ClientboundContainerSetContentPacket packet, CallbackInfo ci) {
        if (minecraft.isSameThread() && minecraft.player.hasContainerOpen()) {
            if (packet.containerId() == minecraft.player.containerMenu.containerId) {
                Dropper.onOpenContainerDrop();
                AutoRepair.onOpenContainer();
                DropItemInShulkerBox.onOpenContainer();
            }
        };
    }

    @Inject(method = "handleSetTime", at = @At("HEAD"))
    public void onSetTime(ClientboundSetTimePacket packet, CallbackInfo ci) {
        if (minecraft.isSameThread()) {
            calcServerTps.onSetTime(packet.gameTime());
        };
    }
}

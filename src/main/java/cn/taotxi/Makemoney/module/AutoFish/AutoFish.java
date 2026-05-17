package cn.taotxi.Makemoney.module.AutoFish;

import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import cn.taotxi.Makemoney.util.MLogger;
import cn.taotxi.Makemoney.util.TaskUtil;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.syncher.SynchedEntityData.DataValue;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.FluidState;

public class AutoFish {
    public static final MLogger logger = new MLogger("AutoFish");
    private static final Minecraft client = Minecraft.getInstance();
    private static int outOfWaterTime = 0;
    private static float lastYaw = -1.0F;
    private static float lastPitch = -1.0F;

    public static void initialize() {
        if (isAutoFishing(false)) {
            TaskUtil.createTimeTask("fishingStatusCheck", AutoFish::fishingStatusCheck, 40);
        }
        registerCommand();
    }

    public static boolean isAutoFishing(boolean isDefault) {
        return AutoFishConfig.getInstance().getBoolean("enabled", isDefault);
    }

    public static boolean isRotationEnabled(boolean isDefault) {
        return AutoFishConfig.getInstance().getBoolean("rotation", isDefault);
    }

    public static int enableFishing() {
        if (!isAutoFishing(false)) {
            TaskUtil.createTimeTask("fishingStatusCheck", AutoFish::fishingStatusCheck, 40);
            AutoFishConfig.getInstance().setBoolean("enabled", true);
        }
        return 1;
    }

    public static int disableFishing() {
        AutoFishConfig.getInstance().setBoolean("enabled", false);
        TaskUtil.removeTimeTask("fishingStatusCheck");
        TaskUtil.removeTimeTask("throwFishingRod");
        return 1;
    }

    public static int setRotationEnabled(boolean enabled) {
        AutoFishConfig.getInstance().setBoolean("rotation", enabled);
        return 1;
    }

    // TODO: 优雅地保存配置文件
    private static void registerCommand() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(ClientCommandManager.literal("autofish")
                .then(ClientCommandManager.literal("on")
                    .executes(context -> AutoFish.enableFishing()))
                .then(ClientCommandManager.literal("off")
                    .executes(context -> AutoFish.disableFishing()))
                .then(ClientCommandManager.literal("rotation")
                    .then(ClientCommandManager.literal("on")
                        .executes(context -> AutoFish.setRotationEnabled(true)))
                    .then(ClientCommandManager.literal("off")
                        .executes(context -> AutoFish.setRotationEnabled(false))))
            );
        });
    }

    private static void fishingStatusCheck() {
        if (client.player == null) return;
        InteractionHand hand = getFishingHand();
        if (hand == null) {
            lastYaw = -1.0F;
            lastPitch = -1.0F;
            return;
        }

        FishingHook bobber = client.player.fishing;
        if (bobber == null) {
            throwRodAfterDelay(false);
            return;
        }

        // 计算鱼漂不在水中的时间，超过 2*检查周期 tick则重新抛竿，这里是 2 * 40 tick，也就是4秒
        BlockPos blockPos = bobber.blockPosition();
        FluidState fluidState = client.level.getFluidState(blockPos);
        if (!fluidState.is(FluidTags.WATER)) {
            outOfWaterTime++;
        } else {
            outOfWaterTime = 0;
        }

        if (outOfWaterTime >= 2) {
            outOfWaterTime = 0;
            client.gameMode.useItem(client.player, hand);
            throwRodAfterDelay(false);
        }

    }

    public static void initRotaion(Player player, InteractionHand interactionHand, CallbackInfoReturnable<InteractionResult> cir) {
        if (lastYaw == -1.0F && isRotationEnabled(false) && getFishingHand() == interactionHand) {
            lastYaw = player.getYRot();
            lastPitch = player.getXRot();
        }
    }

    public static void onEntitySetData(ClientboundSetEntityDataPacket clientboundSetEntityDataPacket, CallbackInfo ci) {
        if (isAutoFishing(false) &&
            client.player != null &&
            client.player.fishing != null &&
            clientboundSetEntityDataPacket.id() == client.player.fishing.getId()
        ) {
            for (DataValue<?> dataValue : clientboundSetEntityDataPacket.packedItems()) {
                // See https://minecraft.wiki/w/Java_Edition_protocol/Entity_metadata#Fishing_Bobber
                if ((dataValue.id() == 9 && (Boolean) dataValue.value()) ||
                    (dataValue.id() == 8 && (Integer) dataValue.value() != 0)
                ) {
                    InteractionHand hand = getFishingHand();
                    if (hand != null) {
                        client.gameMode.useItem(client.player, hand);
                        throwRodAfterDelay(dataValue.id() == 9);
                    }
                }
            }
        }
    }

    private static InteractionHand getFishingHand() {
        Item mainHandItem = client.player.getMainHandItem().getItem();
        if (mainHandItem == Items.FISHING_ROD) {
            return InteractionHand.MAIN_HAND;
        }
        Item offHandItem = client.player.getOffhandItem().getItem();
        if (offHandItem == Items.FISHING_ROD) {
            return InteractionHand.OFF_HAND;
        }
        return null;
    }

    private static void throwRodAfterDelay(boolean rotation) {
        TaskUtil.removeTimeTask("throwFishingRod");
        TaskUtil.createOnceTimeTask("throwFishingRod", () -> {
            InteractionHand hand = getFishingHand();
            if (hand == null) return;
            ItemStack fishingRod = client.player.getItemInHand(hand);
            if (fishingRod.nextDamageWillBreak()) return;

            client.player.swing(hand);
            client.gameMode.useItem(client.player, hand);

            // TODO: Bug: 转向有点问题
            if (!isRotationEnabled(false) || !rotation) {
                return;
            }

            if (lastYaw == -1.0F) {
                lastYaw = client.player.getYRot();
                lastPitch = client.player.getXRot();
                return;
            }
            
            float yaw = client.player.getYRot();
            float pitch = client.player.getXRot();
            client.player.setYRot(lastYaw);
            client.player.setXRot(lastPitch);
            lastYaw = yaw;
            lastPitch = pitch;
        }, 5);
    }
}

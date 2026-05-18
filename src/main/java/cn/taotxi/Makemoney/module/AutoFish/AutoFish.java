package cn.taotxi.Makemoney.module.AutoFish;

import java.util.Map;

import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;

import cn.taotxi.Makemoney.util.EventBus;
import cn.taotxi.Makemoney.util.MLogger;
import cn.taotxi.Makemoney.util.T;
import cn.taotxi.Makemoney.util.TaskUtil;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.syncher.SynchedEntityData.DataValue;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
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
    private static final int checkInterval = 40;
    private static int bobberId = -1;

    public static void initialize() {
        if (isAutoFishing(false)) {
            TaskUtil.createTimeTask("fishingStatusCheck", AutoFish::fishingStatusCheck, checkInterval);
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
            TaskUtil.createTimeTask("fishingStatusCheck", AutoFish::fishingStatusCheck, checkInterval);
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

    public static boolean isRandomDelayEnabled(boolean isDefault) {
        return AutoFishConfig.getInstance().getBoolean("randomDelay", isDefault);
    }

    public static int setRandomDelayEnabled(boolean enabled) {
        AutoFishConfig.getInstance().setBoolean("randomDelay", enabled);
        return 1;
    }

    public static int getThrowDelay(boolean isDefault) {
        return AutoFishConfig.getInstance().getInt("throwDelay", isDefault);
    }

    public static int setThrowDelay(int delay) {
        AutoFishConfig.getInstance().setInt("throwDelay", delay);
        return 1;
    }

    // TODO: 优雅地保存配置文件
    private static void registerCommand() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            var cmd = dispatcher.register(ClientCommandManager.literal("autofish")
                .executes(AutoFish::showHelp)
                .then(ClientCommandManager.literal("help").executes(AutoFish::showHelp))
                .then(ClientCommandManager.literal("on")
                    .executes(context -> {
                        enableFishing();
                        AutoFishConfig.getInstance().saveConfig();
                        context.getSource().sendFeedback(T.tl("autofish.enabled.message"));
                        return 1;
                    }))
                .then(ClientCommandManager.literal("off")
                    .executes(context -> {
                        disableFishing();
                        AutoFishConfig.getInstance().saveConfig();
                        context.getSource().sendFeedback(T.tl("autofish.disabled.message"));
                        return 1;
                    }))
                .then(ClientCommandManager.literal("rotation")
                    .then(ClientCommandManager.literal("on")
                        .executes(context -> {
                            setRotationEnabled(true);
                            AutoFishConfig.getInstance().saveConfig();
                            context.getSource().sendFeedback(T.tl("autofish.rotation.enabled.message"));
                            return 1;
                        }))
                    .then(ClientCommandManager.literal("off")
                        .executes(context -> {
                            setRotationEnabled(false);
                            AutoFishConfig.getInstance().saveConfig();
                            context.getSource().sendFeedback(T.tl("autofish.rotation.disabled.message"));
                            return 1;
                        })))
                .then(ClientCommandManager.literal("randomDelay")
                    .then(ClientCommandManager.literal("on")
                        .executes(context -> {
                            setRandomDelayEnabled(true);
                            AutoFishConfig.getInstance().saveConfig();
                            context.getSource().sendFeedback(T.tl("autofish.randomDelay.enabled.message"));
                            return 1;
                        }))
                    .then(ClientCommandManager.literal("off")
                        .executes(context -> {
                            setRandomDelayEnabled(false);
                            AutoFishConfig.getInstance().saveConfig();
                            context.getSource().sendFeedback(T.tl("autofish.randomDelay.disabled.message"));
                            return 1;
                        })))
                .then(ClientCommandManager.literal("debug")
                    .then(ClientCommandManager.literal("on")
                        .executes(context -> {
                            logger.setDebug(true);
                            context.getSource().sendFeedback(T.l("[AutoFish] Debug mode enabled"));
                            return 1;
                        }))
                    .then(ClientCommandManager.literal("off")
                        .executes(context -> {
                            logger.setDebug(false);
                            context.getSource().sendFeedback(T.l("[AutoFish] Debug mode disabled"));
                            return 1;
                        })))
                .then(ClientCommandManager.literal("throwDelay")
                    .then(ClientCommandManager.argument("delay", IntegerArgumentType.integer(0))
                    .executes(context -> {
                        int delay = context.getArgument("delay", Integer.class);
                        setThrowDelay(delay);
                        AutoFishConfig.getInstance().saveConfig();
                        context.getSource().sendFeedback(T.tl("autofish.throwDelay.message", delay));

                        if (TaskUtil.hasTimeTask("throwFishingRod")) {
                            throwRodAfterDelay(true);
                        }
                        return 1;
                    })))
                .then(ClientCommandManager.literal("config")
                    .then(ClientCommandManager.literal("reload")
                        .executes(context -> {
                            // AutoFishConfig.getInstance().reloadConfig();
                            context.getSource().sendFeedback(T.tl("autofish.config.reload.message"));
                            return 1;
                        }))
                    .then(ClientCommandManager.literal("open")
                        .executes(context -> {
                            EventBus.post("openMainConfigGui", Map.of("tab", 0));
                            return 1;
                        })))
            );

            dispatcher.register(ClientCommandManager.literal("af")
                .executes(AutoFish::showHelp)
                .redirect(cmd)
            );

            dispatcher.register(ClientCommandManager.literal("fish")
                .executes(AutoFish::showHelp)
                .redirect(cmd)
            );
        });
    }

    private static int showHelp(CommandContext<FabricClientCommandSource> context) {
        context.getSource().sendFeedback(T.tl("autofish.help.message"));
        return 1;
    }

    private static void fishingStatusCheck() {
        if (client.player == null) return;
        InteractionHand hand = getFishingHand();
        if (hand == null) {
            lastYaw = -1.0F;
            lastPitch = -1.0F;
            return;
        }

        if (TaskUtil.hasTimeTask("throwFishingRod")) return;
        FishingHook bobber = client.player.fishing;

        if (bobber == null) {
            Entity entity = client.level.getEntity(bobberId);
            if (entity != null && !entity.isRemoved()) {
                bobber = (FishingHook) entity;
                client.player.fishing = bobber;
            } else {
                client.player.fishing = null;
                logger.info("Fishing bobber is null, throw rod");
                throwRod(hand);
                return;
            }
        }

        // 计算鱼钩不在水中的时间，超过 2*检查周期 tick则重新抛竿，这里是 2 * 40 tick，也就是4秒
        BlockPos blockPos = bobber.blockPosition();
        FluidState fluidState = client.level.getFluidState(blockPos);
        if (!fluidState.is(FluidTags.WATER)) {
            outOfWaterTime++;
        } else {
            outOfWaterTime = 0;
        }

        if (outOfWaterTime >= 2) {
            logger.info("Out of water for 2 ticks, throw rod");
            client.gameMode.useItem(client.player, hand);
            throwRod(hand);
        }
    }

    public static boolean throwRod(InteractionHand hand) {
        outOfWaterTime = 0;
        ItemStack fishingRod = client.player.getItemInHand(hand);
        if (fishingRod.nextDamageWillBreak()) {
            logger.info("Fishing rod is broken, stop fishing");
            return false;
        };

        client.gameMode.useItem(client.player, hand);
        client.player.swing(hand);
        return true;
    }

    public static void initRotaion(Player player, InteractionHand interactionHand, CallbackInfoReturnable<InteractionResult> cir) {
        if (lastYaw == -1.0F && isRotationEnabled(false) && getFishingHand() == interactionHand) {
            lastYaw = player.getYRot();
            lastPitch = player.getXRot();
        }
    }

    public static void onEntityAdd(ClientboundAddEntityPacket clientboundAddEntityPacket) {
        if (clientboundAddEntityPacket.getType() != EntityType.FISHING_BOBBER) return;
        FishingHook bobber = (FishingHook) client.level.getEntity(clientboundAddEntityPacket.getId());
        if (bobber.getPlayerOwner() == client.player) {
            bobberId = bobber.getId();
        }
    }

    public static void onEntitySetData(ClientboundSetEntityDataPacket clientboundSetEntityDataPacket) {
        // if (!isAutoFishing(false) || client.player == null) return;

        // Entity entity = client.level.getEntity(clientboundSetEntityDataPacket.id());
        // if (entity == null || entity.getType() != EntityType.FISHING_BOBBER) return;
        // FishingHook bobber = (FishingHook) entity;
        // if (bobber.getPlayerOwner() != client.player) return;
        
        // for (DataValue<?> dataValue : clientboundSetEntityDataPacket.packedItems()) {
        //     // See https://minecraft.wiki/w/Java_Edition_protocol/Entity_metadata#Fishing_Bobber
        //     if ((dataValue.id() == 9 && (Boolean) dataValue.value()) ||
        //         (dataValue.id() == 8 && (Integer) dataValue.value() != 0)
        //     ) {
        //         InteractionHand hand = getFishingHand();
        //         if (hand != null) {
        //             logger.info("Catch a fish or hook in entity");
        //             client.gameMode.useItem(client.player, hand);
        //             throwRodAfterDelay(dataValue.id() == 9);
        //         }
        //     }
        // }
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
                        logger.info("Catch a fish or hook in entity");
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

        int throwDelay = getThrowDelay(false);
        if (isRandomDelayEnabled(false)) {
            throwDelay += (int) (Math.random() * 20) + 1;
        }

        TaskUtil.createOnceTimeTask("throwFishingRod", () -> {
            InteractionHand hand = getFishingHand();
            if (hand == null) return;

            logger.info("Throw rod after delay");

            boolean success = throwRod(hand);
            if (!success) return;

            TaskUtil.resetNextRunTick("fishingStatusCheck");

            // TODO: Bug: 转向有点问题
            if (!isRotationEnabled(false) || !rotation) {
                return;
            }

            if (lastYaw == -1.0F) {
                logger.info("Last yaw is -1.0F, use current yaw");
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
        }, throwDelay);
    }
}

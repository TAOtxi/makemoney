package cn.taotxi.Makemoney.module.StrangeFunction;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import cn.taotxi.Makemoney.Makemoney;
import cn.taotxi.Makemoney.gui.GuiUtil;
import cn.taotxi.Makemoney.util.T;
import cn.taotxi.Makemoney.util.TaskUtil;
import cn.taotxi.Makemoney.util.game.GameUtil;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.game.ClientboundSetPassengersPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerInputPacket;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.EntityHitResult;

public class AutoRide {
    public static final String MODULE_NAME = "autoride";
    private static boolean enabled = false;
    private static final Minecraft client = Minecraft.getInstance();
    private static final StrangeConfig CONFIG = StrangeConfig.getInstance();
    private static final String AUTORIDE_RUN_TASK = "autoRideRunTask";
    private static final String AUTORIDE_SHAKE_OFF_PLAYER_TASK = "autoRideShakeOffPlayerTask";
    private static final int SHAKE_OFF_PLAYER_INTERVAL = 5;
    private static String targetPlayer = "";

    public static void initialize() {
        registerCommand();

        CONFIG.autoRideRunInterval.onChange(
            (oldValue, newValue) -> {
                if (TaskUtil.hasTimeTask(AUTORIDE_RUN_TASK)) {
                    TaskUtil.updateTimeTask(AUTORIDE_RUN_TASK, newValue);
                }
            }
        );

        CONFIG.autoRideTargetPlayer.onChange(
            (oldValue, newValue) -> {
                targetPlayer = newValue;
            }
        );
        CONFIG.autoRideTargetPlayer.triggerConfigChange();

        CONFIG.autoRideEnableShakeOffPlayer.onChange(
            (oldValue, newValue) -> {
                if (!newValue) {
                    TaskUtil.removeTimeTask(AUTORIDE_SHAKE_OFF_PLAYER_TASK);
                    return;
                };

                if (!TaskUtil.hasTimeTask(AUTORIDE_SHAKE_OFF_PLAYER_TASK)) {
                    TaskUtil.createTimeTask(AUTORIDE_SHAKE_OFF_PLAYER_TASK, AutoRide::shakeOffPlayer, SHAKE_OFF_PLAYER_INTERVAL);
                }

                if (client.player == null || client.player.getPassengers().isEmpty()) return;
                shakeOffPlayer();
            }
        );
        CONFIG.autoRideEnableShakeOffPlayer.triggerConfigChange();
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static void setEnabled(boolean enabled) {
        if (AutoRide.enabled == enabled) return;

        AutoRide.enabled = enabled;
        if (enabled) {
            TaskUtil.createTimeTask(AUTORIDE_RUN_TASK, AutoRide::tick, CONFIG.autoRideRunInterval.getValue());
        } else {
            TaskUtil.removeTimeTask(AUTORIDE_RUN_TASK);
        }
    }

    private static void tick() {
        LocalPlayer player = client.player;
        if (player == null) return;

        if (player.isCrouching()) return;
        if (player.getVehicle() != null) return;

        if (!player.getMainHandItem().isEmpty()) return;
        if (client.level.players().size() < 2) return;

        Player target = findTargetPlayer();
        if (target == null) return;

        rideTargetPlayer(target);
    }

    public static void shakeOffPlayer() {
        LocalPlayer player = client.player;
        if (player == null) return;
        
        if (player.isCrouching()) return;
        if (player.getVehicle() != null) return;
        
        // if (player.getPassengers().isEmpty()) return;
        if (player.getAbilities().flying) return;   // 飞行状态下无法潜行

        // TODO: 找到比较优雅让玩家潜行的方法
        Input shiftInput = new Input(
            player.input.keyPresses.forward(),
            player.input.keyPresses.backward(),
            player.input.keyPresses.left(),
            player.input.keyPresses.right(),
            player.input.keyPresses.jump(),
            true,
            player.input.keyPresses.sprint()
        );
        player.connection.send(new ServerboundPlayerInputPacket(shiftInput));

        Input cancelShiftInput = new Input(
            player.input.keyPresses.forward(),
            player.input.keyPresses.backward(),
            player.input.keyPresses.left(),
            player.input.keyPresses.right(),
            player.input.keyPresses.jump(),
            false,
            player.input.keyPresses.sprint()
        );
        player.connection.send(new ServerboundPlayerInputPacket(cancelShiftInput));
    }

    public static void onEntityRidePlayer(ClientboundSetPassengersPacket clientboundSetPassengersPacket) {
        if (clientboundSetPassengersPacket.getVehicle() != client.player.getId()) return;
        if (!CONFIG.autoRideEnableShakeOffPlayer.getValue()) return;
        shakeOffPlayer();
    }

    private static Player findTargetPlayer() {
        LocalPlayer player = client.player;
        List<String> onlinePlayers = GameUtil.getOnlinePlayerNames();
        return client.level.getNearestPlayer(
            player.getX(), player.getY(), player.getZ(), CONFIG.autoRideMinDistance.getValue(),
            cow -> {
                if (
                    targetPlayer.isEmpty() && 
                    cow != player && 
                    onlinePlayers.contains(cow.getName().getString())
                ) {
                    return true;
                }
                return cow != player && cow.getName().getString().equals(targetPlayer);
            });
    }

    private static void rideTargetPlayer(Player playerCow) {
        client.gameMode.interactAt(client.player, playerCow, 
                new EntityHitResult(playerCow), InteractionHand.MAIN_HAND);
    }

    private static int showHelp(CommandContext<FabricClientCommandSource> context) {
        context.getSource().sendFeedback(T.tl("autoride.help.message"));
        return 1;
    }

    public static void resetConfig() {
        CONFIG.autoRideTargetPlayer.resetValue();
        CONFIG.autoRideRunInterval.resetValue();
        CONFIG.autoRideMinDistance.resetValue();
        CONFIG.autoRideEnableShakeOffPlayer.resetValue();
        setEnabled(false);
        CONFIG.saveConfig();
    }

    // TODO: BUG: 插入的变量颜色是白色，即使设置为§e也无效。
    private static void registerCommand() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            var command = dispatcher.register(ClientCommandManager.literal("autoride")
                .executes(AutoRide::showHelp)
                .then(ClientCommandManager.literal("help")
                    .executes(AutoRide::showHelp))
                .then(ClientCommandManager.literal("target")
                    .then(ClientCommandManager.argument("player", StringArgumentType.string())
                        .suggests((context, builder) -> suggestPlayerNames(context, builder))
                        .executes(context -> {
                            String target = context.getArgument("player", String.class);
                            CONFIG.autoRideTargetPlayer.setValue(target);
                            CONFIG.saveConfig();
                            context.getSource().sendFeedback(T.tl("autoride.target.message", target));
                            return 1;
                        })))
                .then(ClientCommandManager.literal("interval")
                    .then(ClientCommandManager.argument("interval", IntegerArgumentType.integer())
                        .executes(context -> {
                            int interval = context.getArgument("interval", Integer.class);
                            CONFIG.autoRideRunInterval.setValue(interval);
                            CONFIG.saveConfig();
                            context.getSource().sendFeedback(T.tl("autoride.interval.message", interval));
                            return 1;
                        })))
                .then(ClientCommandManager.literal("distance")
                    .then(ClientCommandManager.argument("distance", FloatArgumentType.floatArg())
                        .executes(context -> {
                            float distance = context.getArgument("distance", Float.class);
                            CONFIG.autoRideMinDistance.setValue(distance);
                            CONFIG.saveConfig();
                            context.getSource().sendFeedback(T.tl("autoride.distance.message", distance));
                            return 1;
                        })))
                .then(ClientCommandManager.literal("reset").executes(context -> {
                    CONFIG.resetConfig();
                    setEnabled(false);
                    context.getSource().sendFeedback(T.tl("autoride.reset.message"));
                    return 1;
                }))
                .then(ClientCommandManager.literal("smoothHead")
                    .then(ClientCommandManager.literal("on")
                        .executes(context -> {
                            CONFIG.autoRideEnableShakeOffPlayer.enable();
                            CONFIG.saveConfig();
                            context.getSource().sendFeedback(T.tl("autoride.enableShakeOffPlayer.message", true));
                            return 1;
                        }))
                    .then(ClientCommandManager.literal("off")
                        .executes(context -> {
                            CONFIG.autoRideEnableShakeOffPlayer.disable();
                            CONFIG.saveConfig();
                            context.getSource().sendFeedback(T.tl("autoride.enableShakeOffPlayer.message", false));
                            return 1;
                        }))
                )
                .then(ClientCommandManager.literal("on").executes(context -> {
                    setEnabled(true);
                    context.getSource().sendFeedback(T.tl("autoride.enabled.message"));
                    return 1;
                }))
                .then(ClientCommandManager.literal("off").executes(context -> {
                    setEnabled(false);
                    context.getSource().sendFeedback(T.tl("autoride.disabled.message"));
                    return 1;
                }))
                .then(ClientCommandManager.literal("config").executes(context -> {
                    GuiUtil.openYaclScreen(Makemoney.MOD_ID, MODULE_NAME);
                    return 1;
                }))
            );

            dispatcher.register(ClientCommandManager.literal("ar")
                    .executes(AutoRide::showHelp)
                    .redirect(command));
        });

    }

    private static CompletableFuture<Suggestions> suggestPlayerNames(
        CommandContext<FabricClientCommandSource> context,
        SuggestionsBuilder builder
    ) {
        List<String> playerNameList = GameUtil.getOnlinePlayerNames();
        for (String name : playerNameList) {
            builder.suggest(name);
        }
        return builder.buildFuture();
    }
}
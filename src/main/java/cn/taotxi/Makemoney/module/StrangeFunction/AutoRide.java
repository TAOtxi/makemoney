package cn.taotxi.Makemoney.module.StrangeFunction;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
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
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.game.ServerboundPlayerInputPacket;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.EntityHitResult;

public class AutoRide {
    private static boolean enabled = false;
    private static int tickCounter = 0;
    private static final StrangeConfig CONFIG = StrangeConfig.getInstance();

    public static void initialize() {
        registerTickEvents();
        registerCommand();
    }

    private static void registerTickEvents() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.level == null || client.player == null) return;
            
            if (client.player.isCrouching()) return;
            if (client.player.getVehicle() != null) return;
            
            if (CONFIG.autoRideEnableShakeOffPlayer.getValue()) {
                tryToShakeOffPlayer(client.player);
            }
            if (!enabled) return;
            
            tickCounter++;
            if (tickCounter % CONFIG.autoRideRunInterval.getValue() != 0) return;

            if (!client.player.getMainHandItem().isEmpty()) return;
            if (client.level.players().size() < 2) return;

            Player target = findTargetPlayer();
            if (target == null) return;

            rideTargetPlayer(target);
        });
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static void setEnabled(boolean enabled) {
        AutoRide.enabled = enabled;
    }

    private static void tryToShakeOffPlayer(LocalPlayer player) {
        if (player.getPassengers().isEmpty()) return;
        if (player.getAbilities().flying) return;   // 飞行状态下无法潜行
        if (TaskUtil.hasTimeTask("shakeOffPlayer")) return;

        // TODO: 找到比较优雅让玩家潜行的方法
        // TODO: 考虑是否要覆盖掉原有输入
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
        TaskUtil.createOnceTimeTask("shakeOffPlayer", () -> {
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
        }, 0);  // 暂时不考虑自定义周期
    }

    private static Player findTargetPlayer() {
        Minecraft client = Minecraft.getInstance();
        LocalPlayer player = client.player;
        String targetPlayer = CONFIG.autoRideTargetPlayer.getValue();
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
        Minecraft client = Minecraft.getInstance();
        client.gameMode.interactAt(client.player, playerCow, 
                new EntityHitResult(playerCow), InteractionHand.MAIN_HAND);
    }

    public static void resetConfig() {
        setEnabled(false);
        CONFIG.autoRideTargetPlayer.resetValue();
        CONFIG.autoRideMinDistance.resetValue();
        CONFIG.autoRideRunInterval.resetValue();
        CONFIG.autoRideEnableShakeOffPlayer.resetValue();
        CONFIG.saveConfig();
    }

    private static int showHelp(CommandContext<FabricClientCommandSource> context) {
        context.getSource().sendFeedback(T.tl("autoride.help.message"));
        return 1;
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
                        .suggests(new PlayerSuggestionProvider())
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
                    resetConfig();
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
                    enabled = true;
                    context.getSource().sendFeedback(T.tl("autoride.enabled.message"));
                    return 1;
                }))
                .then(ClientCommandManager.literal("off").executes(context -> {
                    enabled = false;
                    context.getSource().sendFeedback(T.tl("autoride.disabled.message"));
                    return 1;
                }))
                .then(ClientCommandManager.literal("config").executes(context -> {
                    GuiUtil.openYaclScreen(Makemoney.MOD_ID);
                    return 1;
                }))
            );

            dispatcher.register(ClientCommandManager.literal("ar")
                    .executes(AutoRide::showHelp)
                    .redirect(command));
        });

    }
}


class PlayerSuggestionProvider implements SuggestionProvider<FabricClientCommandSource> {
	@Override
	public CompletableFuture<Suggestions> getSuggestions(CommandContext<FabricClientCommandSource> context, SuggestionsBuilder builder) throws CommandSyntaxException {
		FabricClientCommandSource source = context.getSource();

		// Thankfully, the ServerCommandSource has a method to get a list of player names.
		Collection<String> playerNames = source.getOnlinePlayerNames();

		// Add all player names to the builder.
		for (String playerName : playerNames) {
			builder.suggest(playerName);
		}

		// Lock the suggestions after we've modified them.
		return builder.buildFuture();
	}
}
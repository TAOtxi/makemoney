package cn.taotxi.Makemoney.module.StrangeFunction;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import cn.taotxi.Makemoney.util.EventBus;
import cn.taotxi.Makemoney.util.T;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.EntityHitResult;

public class AutoRide {
    private static boolean enabled = false;
    private static int tickCounter = 0;

    public static void init() {
        registerTickEvents();
        registerCommand();
    }

    private static void registerTickEvents() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.level == null || client.player == null) return;

            if (!enabled) return;
            if (client.player.getVehicle() != null) return;
            tickCounter++;
            if (tickCounter % getRunInterval(false) != 0) return;

            if (!client.player.getMainHandItem().isEmpty()) return;
            if (client.player.isCrouching()) return;

            Player target = findTargetPlayer();
            if (target == null) return;
            client.player.stopRiding();

            rideTargetPlayer(target);
        });
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static void setEnabled(boolean enabled) {
        AutoRide.enabled = enabled;
    }

    public static int getRunInterval(boolean isDefault) {
        return StrangeConfig.getInstance().getInt("autoride_runInterval", isDefault);
    }

    public static void setRunInterval(int interval) {
        StrangeConfig.getInstance().putInt("autoride_runInterval", interval);
    }

    public static double getMinDistance(boolean isDefault) {
        return StrangeConfig.getInstance().getDouble("autoride_minDistance", isDefault);
    }

    public static void setMinDistance(double distance) {
        StrangeConfig.getInstance().putDouble("autoride_minDistance", distance);
    }

    public static String getTargetPlayer(boolean isDefault) {
        return StrangeConfig.getInstance().getString("autoride_targetPlayer", isDefault);
    }

    public static void setTargetPlayer(String player) {
        StrangeConfig.getInstance().putString("autoride_targetPlayer", player);
    }

    private static Player findTargetPlayer() {
        Minecraft client = Minecraft.getInstance();
        Player player = client.player;
        String targetPlayer = getTargetPlayer(false);
        return client.level.getNearestPlayer(
            player.getX(), player.getY(), player.getZ(), 
            getMinDistance(false), cow -> {
                // TODO: 使用在线玩家列表过滤NPC生物
                if (targetPlayer.isEmpty() && 
                    cow != player && 
                    !cow.getName().getString().startsWith("CIT-")) 
                {
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
        StrangeConfig.getInstance().reset("autoride_targetPlayer");
        StrangeConfig.getInstance().reset("autoride_minDistance");
        StrangeConfig.getInstance().reset("autoride_runInterval");
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
                            setTargetPlayer(target);
                            context.getSource().sendFeedback(T.tl("autoride.target.message", target));
                            return 1;
                        })))
                .then(ClientCommandManager.literal("interval")
                    .then(ClientCommandManager.argument("interval", IntegerArgumentType.integer())
                        .executes(context -> {
                            int interval = context.getArgument("interval", Integer.class);
                            setRunInterval(interval);
                            context.getSource().sendFeedback(T.tl("autoride.interval.message", interval));
                            return 1;
                        })))
                .then(ClientCommandManager.literal("distance")
                    .then(ClientCommandManager.argument("distance", DoubleArgumentType.doubleArg())
                        .executes(context -> {
                            double distance = context.getArgument("distance", Double.class);
                            setMinDistance(distance);
                            context.getSource().sendFeedback(T.tl("autoride.distance.message", distance));
                            return 1;
                        })))
                .then(ClientCommandManager.literal("reset").executes(context -> {
                    resetConfig();
                    context.getSource().sendFeedback(T.tl("autoride.reset.message"));
                    return 1;
                }))
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
                    EventBus.post("openMainConfigGui", Map.of("tab", 1));
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
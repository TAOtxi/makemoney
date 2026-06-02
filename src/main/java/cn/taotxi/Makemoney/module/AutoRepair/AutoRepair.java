package cn.taotxi.Makemoney.module.AutoRepair;

import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.network.chat.MutableComponent;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;

import java.util.Map;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;

import cn.taotxi.Makemoney.util.MLogger;
import cn.taotxi.Makemoney.util.T;

public class AutoRepair {
    public static final String MODULE_NAME = "autorepair";
    public static final MLogger LOGGER = new MLogger(MODULE_NAME);
    public static AutoRepairConfig config = AutoRepairConfig.load(AutoRepairConfig.class, MODULE_NAME);

    public static void registerTickEvents(Minecraft client, int tickCounter) {
        if (!config.enabled)
            return;
        if (tickCounter % config.checkoffHandInterval == 0) {
            Replace.tryToReplace();
        }
        if (tickCounter % config.repairInterval == 0) {
            EnchantExp.tryToEnchantMending();
        }
    }

    public static void registerCommand(CommandDispatcher<FabricClientCommandSource> dispatcher,
            CommandBuildContext registryAccess) {
        dispatcher.register(ClientCommandManager.literal(MODULE_NAME).executes(AutoRepair::showHelp)
            .then(ClientCommandManager.literal("help").executes(AutoRepair::showHelp))
            .then(ClientCommandManager.literal("reload").executes(AutoRepair::reloadConfig))
            .then(ClientCommandManager.literal("config").executes(AutoRepair::openConfigGui))
            .then(ClientCommandManager.literal("true")
                .executes(context -> toggleAutoRepair(context, true)))
            .then(ClientCommandManager.literal("false")
                .executes(context -> toggleAutoRepair(context, false)))
        );
    }

    private static int showHelp(CommandContext<FabricClientCommandSource> context) {
        context.getSource().sendFeedback(T.tl(MODULE_NAME + ".message.help"));
        return 1;
    }

    private static int reloadConfig(CommandContext<FabricClientCommandSource> context) {
        config = AutoRepairConfig.load(AutoRepairConfig.class, MODULE_NAME);
        return 1;
    }

    private static int toggleAutoRepair(CommandContext<FabricClientCommandSource> context, boolean enable) {
        MutableComponent feedbackMsg = T.tl("message." + (enable ? "enable" : "disable"), MODULE_NAME);
        context.getSource().sendFeedback(feedbackMsg);
        if (config.enabled == enable) {
            return 1;
        }
        config.enabled = enable;
        config.save();
        return 1;
    }

    private static int openConfigGui(CommandContext<FabricClientCommandSource> context) {
        // EventBus.post("openConfigGui", Map.of("title", T.t(MODULE_NAME + ".name")));
        return 1;
    }
}

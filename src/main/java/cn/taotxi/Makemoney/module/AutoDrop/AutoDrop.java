package cn.taotxi.Makemoney.module.AutoDrop;

import java.util.Map;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;

import cn.taotxi.Makemoney.Makemoney;
import cn.taotxi.Makemoney.util.EventBus;
import cn.taotxi.Makemoney.util.MLogger;
import cn.taotxi.Makemoney.util.Message;
import cn.taotxi.Makemoney.util.T;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.network.chat.MutableComponent;

public class AutoDrop {
    public static final String MODULE_NAME = "autodrop";
    public static final MLogger LOGGER = new MLogger(MODULE_NAME);
    public static AutoDropConfig config = AutoDropConfig.load(AutoDropConfig.class, MODULE_NAME);
    public static int tickCounter = 0;
    public static boolean isDebug = false;

    public static void registerTickEvents(Minecraft client, int tickCounter) {
        if (!config.enabled || Makemoney.isOpenYaclScreen) return;
        if (config.showAttentionMsg) {
            Message.actionBarMsg(T.tl("autodrop.message.attention"));
        }

        if (tickCounter % config.checkInterval != 0) return;
        Dropper.tryToDropItems();
    }

    public static void registerCommand(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandBuildContext registryAccess) {
        for (String name: new String[]{MODULE_NAME, "ad"}) {
            dispatcher.register(ClientCommandManager.literal(name).executes(AutoDrop::showHelp)
                .then(ClientCommandManager.literal("help").executes(AutoDrop::showHelp))
                .then(ClientCommandManager.literal("reload").executes(AutoDrop::reloadConfig))
                .then(ClientCommandManager.literal("config").executes(AutoDrop::openConfigGui))
                .then(ClientCommandManager.literal("true")
                    .executes(context -> toggleAutoDrop(context, true)))
                .then(ClientCommandManager.literal("false")
                    .executes(context -> toggleAutoDrop(context, false)))
                .then(ClientCommandManager.literal("debug")
                    .then(ClientCommandManager.literal("true")
                        .executes(context -> setDebug(true)))
                    .then(ClientCommandManager.literal("false")
                        .executes(context -> setDebug(false))))
            );
        }
    }

    private static int setDebug(boolean debug) {
        isDebug = debug;
        LOGGER.setDebug(debug);
        return 1;
    }

    private static int showHelp(CommandContext<FabricClientCommandSource> context) {
        context.getSource().sendFeedback(T.tl(MODULE_NAME + ".message.help"));
        return 1;
    }

    private static int reloadConfig(CommandContext<FabricClientCommandSource> context) {
        config = AutoDropConfig.load(AutoDropConfig.class, MODULE_NAME);
        return 1;
    }

    private static int toggleAutoDrop(CommandContext<FabricClientCommandSource> context, boolean enable) {
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
        EventBus.post("openConfigGui", Map.of("title", T.t(MODULE_NAME + ".name")));
        return 1;
    }
}

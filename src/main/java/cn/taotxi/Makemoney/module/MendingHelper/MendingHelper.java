package cn.taotxi.Makemoney.module.MendingHelper;

import com.mojang.brigadier.context.CommandContext;

import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import cn.taotxi.Makemoney.Makemoney;
import cn.taotxi.Makemoney.gui.GuiUtil;
import cn.taotxi.Makemoney.util.Message;
import cn.taotxi.Makemoney.util.T;

public class MendingHelper {
    public static final String MODULE_NAME = "mendinghelper";
    public static final MendingHelperConfig CONFIG = MendingHelperConfig.getInstance();

    public static void initialize() {
        CONFIG.loadConfig();

        registerCommand();
        AutoMendingReplace.initialize();
        AutoDecompose.initialize();
    }

    private static void registerCommand() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            var cmd = dispatcher.register(ClientCommandManager.literal(MODULE_NAME)
                .executes(MendingHelper::showHelp)
                .then(ClientCommandManager.literal("autoreplace")
                    .then(ClientCommandManager.literal("on")
                        .executes(context -> setAutoReplaceEnabled(true)))
                    .then(ClientCommandManager.literal("off")
                        .executes(context -> setAutoReplaceEnabled(false))))
                .then(ClientCommandManager.literal("autoenchant")
                    .then(ClientCommandManager.literal("on")
                        .executes(context -> setAutoEnchantEnabled(true)))
                    .then(ClientCommandManager.literal("off")
                        .executes(context -> setAutoEnchantEnabled(false))))
                .then(ClientCommandManager.literal("autodecompose")
                    .then(ClientCommandManager.literal("on")
                        .executes(context -> setAutoDecomposeEnabled(true)))
                    .then(ClientCommandManager.literal("off")
                        .executes(context -> setAutoDecomposeEnabled(false))))
                .then(ClientCommandManager.literal("config")
                    .executes(MendingHelper::openConfigScreen))
            );

            dispatcher.register(ClientCommandManager.literal("mh")
                .executes(MendingHelper::showHelp)
                .redirect(cmd)
            );
        });
    }

    private static int openConfigScreen(CommandContext<FabricClientCommandSource> context) {
        GuiUtil.openYaclScreen(Makemoney.MOD_ID, 1);
        return 1;
    }

    private static int setAutoReplaceEnabled(boolean enabled) {
        CONFIG.autoReplaceEnabled.setValue(enabled);
        CONFIG.saveConfig();
        Message.clientSideMsg(enabled ? 
            T.tl("mendingHelper.autoReplace.enabled.message") : 
            T.tl("mendingHelper.autoReplace.disabled.message"));
        return 1;
    }

    private static int setAutoEnchantEnabled(boolean enabled) {
        CONFIG.autoEnchantEnabled.setValue(enabled);
        CONFIG.saveConfig();
        Message.clientSideMsg(enabled ? 
            T.tl("mendingHelper.autoEnchant.enabled.message") : 
            T.tl("mendingHelper.autoEnchant.disabled.message"));
        return 1;
    }

    private static int setAutoDecomposeEnabled(boolean enabled) {
        CONFIG.autoDecomposeEnabled.setValue(enabled);
        CONFIG.saveConfig();
        Message.clientSideMsg(enabled ? 
            T.tl("mendingHelper.autoDecompose.enabled.message") : 
            T.tl("mendingHelper.autoDecompose.disabled.message"));
        return 1;
    }

    private static int showHelp(CommandContext<FabricClientCommandSource> context) {
        context.getSource().sendFeedback(T.tl("mendinghelper.help.message"));
        return 1;
    }
}

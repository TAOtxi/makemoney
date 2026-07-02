package cn.taotxi.Makemoney.module.AutoAFK;

import com.mojang.brigadier.context.CommandContext;

import cn.taotxi.Makemoney.Makemoney;
import cn.taotxi.Makemoney.gui.GuiUtil;
import cn.taotxi.Makemoney.module.AutoFish.AutoFish;
import cn.taotxi.Makemoney.util.T;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;

public class AutoAFK {
    public static final String MODULE_NAME = "autoafk";

    public static void initialize() {
        AutoAFKConfig.getInstance().loadConfig();
        AutoAttack.initialize();
        registerCommand();
    }

    private static void registerCommand() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            var cmd = dispatcher.register(ClientCommandManager.literal(MODULE_NAME)
                .executes(AutoAFK::showHelp)
                .then(ClientCommandManager.literal("help")
                    .executes(AutoAFK::showHelp))
                .then(AutoAttack.attackCommand())
                .then(ClientCommandManager.literal("config")
                    .executes(context -> {
                        GuiUtil.openYaclScreen(Makemoney.MOD_ID, MODULE_NAME);
                        return 1;
                    }))
            );

            dispatcher.register(ClientCommandManager.literal("afkk")
                .executes(AutoAFK::showHelp)
                .redirect(cmd)
            );
        });
    }

    private static int showHelp(CommandContext<FabricClientCommandSource> context) {
        context.getSource().sendFeedback(T.tl("autoAFK.help.message"));
        return 1;
    }
}
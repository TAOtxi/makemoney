package cn.taotxi.Makemoney.module.AutoAFK;

import cn.taotxi.Makemoney.Makemoney;
import cn.taotxi.Makemoney.gui.GuiUtil;
import cn.taotxi.Makemoney.util.help.HelpMenu;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLevelEvents;

public class AutoAFK {
    public static final String MODULE_NAME = "autoafk";
    private static final HelpMenu HELP = HelpMenu.of(MODULE_NAME, "autoAFK.help")
        .alias("afkk")
        .runEntry("config", "autoAFK.help.config")
        .runEntry("attack help", "autoAFK.help.attack")
        .entry("tpsCheck on", "autoAFK.help.tpsCheckOn")
        .entry("tpsCheck off", "autoAFK.help.tpsCheckOff")
        .build();

    public static void initialize() {
        AutoAFKConfig.getInstance().loadConfig();
        AutoAttack.initialize();
        TpsChecker.initialize();
        PositionChecker.initialize();
        registerCommand();

        ClientLevelEvents.AFTER_CLIENT_LEVEL_CHANGE.register((mc, level) -> {
            calcServerTps.reset();
        });
    }

    private static void registerCommand() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            var cmd = dispatcher.register(ClientCommands.literal(MODULE_NAME)
                .executes(HELP::executeFirstPage)
                .then(HELP.helpCommand())
                .then(AutoAttack.attackCommand())
                .then(TpsChecker.tpsCheckCmd())
                .then(ClientCommands.literal("config")
                    .executes(context -> {
                        GuiUtil.openYaclScreen(Makemoney.MOD_ID, MODULE_NAME);
                        return 1;
                    }))
            );

            dispatcher.register(ClientCommands.literal("afkk")
                .executes(HELP::executeFirstPage)
                .redirect(cmd)
            );
        });
    }
}
package cn.taotxi.Makemoney;

import net.fabricmc.api.ModInitializer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mojang.blaze3d.platform.Window;
import com.mojang.brigadier.context.CommandContext;

import cn.taotxi.Makemoney.config.MakemoneyConfig;
import cn.taotxi.Makemoney.gui.GuiUtil;
import cn.taotxi.Makemoney.gui.ConfigScreen;
import cn.taotxi.Makemoney.gui.dialog.ConfirmWindow;
import cn.taotxi.Makemoney.module.AutoDrop.AutoDrop;
import cn.taotxi.Makemoney.module.AutoDrop.AutoDropConfigGui;
import cn.taotxi.Makemoney.module.AutoFish.AutoFish;
import cn.taotxi.Makemoney.module.StrangeFunction.StrangeFunctionInit;
import cn.taotxi.Makemoney.util.T;
import cn.taotxi.Makemoney.util.TaskUtil;
import dev.isxander.yacl3.gui.YACLScreen;

// TODO: 屏蔽扫地机信息
// TODO: 屏蔽地震消息
public class Makemoney implements ModInitializer {
	public static final String MOD_ID = "makemoney";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("Starting mod...");

        MakemoneyConfig.getInstance().loadConfig();
        List<String> configChangeNameList = MakemoneyConfig.getInstance().getConfigChangeNameList();
        if (!configChangeNameList.isEmpty()) {
            GuiUtil.openConfigChangeTipWindow(configChangeNameList);
        }

        registerCommand();
        AutoDrop.initialize();
        AutoFish.initialize();
        StrangeFunctionInit.initialize();
        TaskUtil.initialize();

        // TaskUtil.createTimeTask("a", () -> {
        //     System.out.println(Minecraft.getInstance().screen.getClass());
        // }, 20);
	}

    private void registerCommand() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            // AutoRepair.registerCommand(dispatcher, registryAccess);
            AutoDrop.registerCommand(dispatcher, registryAccess);
            // AutoAction.registerCommand(dispatcher, registryAccess);

            var command = dispatcher.register(ClientCommandManager.literal("makemoney")
                .executes(Makemoney::showHelp)
                .then(ClientCommandManager.literal("help")
                    .executes(Makemoney::showHelp))
                .then(ClientCommandManager.literal("config")
                    .executes(context -> {
                        GuiUtil.openYaclScreen(MOD_ID);
                        return 1;
                    }))
            );

            dispatcher.register(ClientCommandManager.literal("mn")
                    .executes(Makemoney::showHelp)
                    .redirect(command));

            dispatcher.register(ClientCommandManager.literal("mk")
                    .executes(Makemoney::showHelp)
                    .redirect(command));

            dispatcher.register(ClientCommandManager.literal("mkm")
                    .executes(Makemoney::showHelp)
                    .redirect(command));

            dispatcher.register(ClientCommandManager.literal("tt")
                .then(ClientCommandManager.literal("1")
                    .executes(context -> {
                        Minecraft.getInstance().setScreen(new ConfirmWindow(T.l("Confirm")));
                        return 1;
                    }))
                .then(ClientCommandManager.literal("2")
                    .executes(context -> {
                        var player = context.getSource().getClient().player;
                        System.out.println(player.getPassengers());
                        return 1;
                    }))
            );
                
        });
    }


    private static int showHelp(CommandContext<FabricClientCommandSource> context) {
        context.getSource().sendFeedback(T.tl("help.message"));
        return 1;
    }
}
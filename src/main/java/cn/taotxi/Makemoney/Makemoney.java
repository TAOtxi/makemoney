package cn.taotxi.Makemoney;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;

import cn.taotxi.Makemoney.config.MakemoneyConfig;
import cn.taotxi.Makemoney.gui.GuiUtil;
import cn.taotxi.Makemoney.module.AutoAFK.AutoAFK;
import cn.taotxi.Makemoney.module.AutoDrop.AutoDrop;
import cn.taotxi.Makemoney.module.AutoFish.AutoFish;
import cn.taotxi.Makemoney.module.MendingHelper.MendingHelper;
import cn.taotxi.Makemoney.module.MenuClick.MenuClick;
import cn.taotxi.Makemoney.module.MessageCommand.MessageCommand;
import cn.taotxi.Makemoney.module.NineteenWorld.NineteenWorld;
// import cn.taotxi.Makemoney.module.Highlight.Highlight;
import cn.taotxi.Makemoney.module.Test.Test;
import cn.taotxi.Makemoney.util.T;
import cn.taotxi.Makemoney.util.TaskUtil;
import cn.taotxi.Makemoney.module.Task.TaskEntry;


public class Makemoney implements ModInitializer {
	public static final String MOD_ID = "makemoney";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("Starting mod...");

        File folder = new File(FabricLoader.getInstance().getConfigDir().toFile(), MOD_ID);
        boolean isNewUser = !folder.exists();
        MakemoneyConfig.getInstance().loadConfig();
        
        if (!isNewUser) {
            LOGGER.info("Not a new user, check config change.");
            List<String> configChangeNameList = MakemoneyConfig.getInstance().getConfigChangeNameList();
            if (!configChangeNameList.isEmpty()) {
                LOGGER.info("Config change detected: {}", configChangeNameList);
                GuiUtil.openConfigChangeTipWindow(configChangeNameList);
            }
        } else {
            LOGGER.info("New user, update config version field.");
            MakemoneyConfig.getInstance().updateConfigVersionField();
        }

        registerCommand();
        AutoDrop.initialize();
        AutoFish.initialize();
        NineteenWorld.initialize();
        MessageCommand.initialize();
        MenuClick.initialize();
        MendingHelper.initialize();
        AutoAFK.initialize();
        TaskUtil.initialize();
        TaskEntry.initialize();
        // Highlight.initialize();
        Test.initialize();
	}

    private void registerCommand() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            AutoDrop.registerCommand(dispatcher, registryAccess);

            var command = dispatcher.register(ClientCommands.literal("makemoney")
                .executes(Makemoney::showHelp)
                .then(ClientCommands.literal("help")
                    .executes(Makemoney::showHelp))
                .then(ClientCommands.literal("config")
                    .executes(context -> {
                        GuiUtil.openYaclScreen(MOD_ID);
                        return 1;
                    })
                    .then(ClientCommands.argument("tab", IntegerArgumentType.integer(0, 5))
                        .executes(context -> {
                            int tab = context.getArgument("tab", Integer.class);
                            GuiUtil.openYaclScreen(MOD_ID, tab);
                            return 1;
                        }))
                )
            );

            dispatcher.register(ClientCommands.literal("mn")
                    .executes(Makemoney::showHelp)
                    .redirect(command));

            // dispatcher.register(ClientCommands.literal("mk")
            //         .executes(Makemoney::showHelp)
            //         .redirect(command));

            // dispatcher.register(ClientCommands.literal("mkm")
            //         .executes(Makemoney::showHelp)
            //         .redirect(command)); 
        });
    }


    private static int showHelp(CommandContext<FabricClientCommandSource> context) {
        context.getSource().sendFeedback(T.tl("help.message"));
        return 1;
    }
}
package cn.taotxi.Makemoney;

import net.fabricmc.api.ModInitializer;
import net.minecraft.world.item.Items;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.mojang.brigadier.context.CommandContext;

import cn.taotxi.Makemoney.config.MakemoneyConfig;
import cn.taotxi.Makemoney.gui.GuiUtil;
import cn.taotxi.Makemoney.module.AutoDrop.AutoDrop;
import cn.taotxi.Makemoney.module.AutoFish.AutoFish;
import cn.taotxi.Makemoney.module.MendingHelper.MendingHelper;
import cn.taotxi.Makemoney.module.MenuClick.MenuClick;
import cn.taotxi.Makemoney.module.MessageCommand.MessageCommand;
import cn.taotxi.Makemoney.module.StrangeFunction.StrangeFunctionInit;
import cn.taotxi.Makemoney.util.Message;
import cn.taotxi.Makemoney.util.T;
import cn.taotxi.Makemoney.util.TaskUtil;


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
        StrangeFunctionInit.initialize();
        MessageCommand.initialize();
        MenuClick.initialize();
        MendingHelper.initialize();
        TaskUtil.initialize();

        // TaskUtil.createTimeTask("a", () -> {
        //     if (Minecraft.getInstance().player == null) {
        //         return;
        //     }
        //     System.out.println(Minecraft.getInstance().player.containerMenu.getClass());
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
                        var client = context.getSource().getClient();
                        var itemStack = client.player.getInventory().getSelectedItem();
                        Message.clientSideMsg("" + itemStack.is(Items.DIAMOND));
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
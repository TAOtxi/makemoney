package cn.taotxi.Makemoney;

import net.fabricmc.api.ModInitializer;
import net.minecraft.client.Minecraft;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mojang.blaze3d.platform.Window;
import com.mojang.brigadier.context.CommandContext;

import cn.taotxi.Makemoney.gui.ConfigScreen;
import cn.taotxi.Makemoney.module.AutoAction.AutoAction;
import cn.taotxi.Makemoney.module.AutoDrop.AutoDrop;
import cn.taotxi.Makemoney.module.AutoDrop.AutoDropConfigGui;
import cn.taotxi.Makemoney.module.AutoFish.AutoFish;
import cn.taotxi.Makemoney.module.StrangeFunction.StrangeFunctionInit;
import cn.taotxi.Makemoney.util.EventBus;
import cn.taotxi.Makemoney.util.T;
import cn.taotxi.Makemoney.util.TaskUtil;
import dev.isxander.yacl3.gui.YACLScreen;

// TODO: 屏蔽扫地机信息
// TODO: 屏蔽地震消息
public class Makemoney implements ModInitializer {
	public static final String MOD_ID = "makemoney";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    private static int tickCounter = 0;

	@Override
	public void onInitialize() {
		LOGGER.info("Starting mod...");
        
        registerTickEvents();
        registerSomeEvents();
        registerCommand();
        AutoDrop.init();
        AutoFish.initialize();
        StrangeFunctionInit.init();
        TaskUtil.initialize();
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
                        EventBus.post("openMainConfigGui", Map.of("tab", 0));
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
        });
    }

    private void registerTickEvents() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.level == null || client.player == null) return;
            EventBus.checkQueue();
            
            tickCounter++;
            // AutoRepair.registerTickEvents(client, tickCounter);
            AutoDrop.registerTickEvents(client, tickCounter);
            // AutoAction.registerTickEvents(client, tickCounter);
            // EntityHighlightBox.registerTickEvents(client, tickCounter);
        });
    }

    private void registerSomeEvents() {
        // ScreenEvents.AFTER_INIT.register((client, screen, width, height) -> {
        //     Message.sendMessage("Screen: " + screen.getClass().getSimpleName());
        // });
        // EntityHighlightBox.registerRenderEvents();
        EventBus.register("openConfigGui", (args) -> {
            Minecraft client = Minecraft.getInstance();
            if (client.screen != null) {
                EventBus.post("openConfigGui", args);
                return;
            }
            
            YACLScreen configScreen = null;
            String module = (String) args.get("module");

            if (module.equals(AutoDrop.MODULE_NAME)) {
                configScreen = (YACLScreen) AutoDropConfigGui.createScreen(null);
            } else {
                LOGGER.error("Can not find module: " + module);
                return;
            }

            Window window = client.getWindow(); 
            configScreen.init(window.getGuiScaledWidth(), 
                    window.getGuiScaledHeight());
            client.setScreen(configScreen);
        });

        EventBus.register("openMainConfigGui", (args) -> {
            Minecraft client = Minecraft.getInstance();
            if (client.screen != null) {
                EventBus.post("openMainConfigGui", args);
                return;
            }
            
            YACLScreen configScreen = (YACLScreen) ConfigScreen.getConfigScreen(null);
            int openTab = (int) args.get("tab");
            configScreen.init(client.getWindow().getGuiScaledWidth(), 
                    client.getWindow().getGuiScaledHeight());
            if (openTab < 0 || openTab >= configScreen.tabNavigationBar.getTabs().size()) {
                LOGGER.error("Invalid tab: " + openTab);
                return;
            }
            configScreen.tabNavigationBar.selectTab(openTab, true);

            client.setScreen(configScreen);
        });
    }

    private static int showHelp(CommandContext<FabricClientCommandSource> context) {
        context.getSource().sendFeedback(T.tl("help.message"));
        return 1;
    }
}
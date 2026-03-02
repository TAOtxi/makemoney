package cn.taotxi.Makemoney;

import net.fabricmc.api.ModInitializer;
import net.minecraft.client.Minecraft;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.taotxi.Makemoney.gui.ConfigScreen;
import cn.taotxi.Makemoney.module.AutoAction.AutoAction;
import cn.taotxi.Makemoney.module.AutoDrop.AutoDrop;
import cn.taotxi.Makemoney.module.AutoRepair.AutoRepair;
import cn.taotxi.Makemoney.module.EntityHighlightBox.EntityHighlightBox;
import cn.taotxi.Makemoney.test.TestMod;
import cn.taotxi.Makemoney.util.EventBus;
import cn.taotxi.Makemoney.util.Message;
import cn.taotxi.Makemoney.util.T;
import dev.isxander.yacl3.gui.YACLScreen;


public class Makemoney implements ModInitializer {
	public static final String MOD_ID = "makemoney";
    public static boolean isOpenYaclScreen = false;
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    private static int tickCounter = 0;

	@Override
	public void onInitialize() {
		LOGGER.info("Starting mod...");
        
        registerTickEvents();
        registerSomeEvents();
        registerCommand();
        // TestMod.register();
	}

    private void registerCommand() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            AutoRepair.registerCommand(dispatcher, registryAccess);
            AutoDrop.registerCommand(dispatcher, registryAccess);
            AutoAction.registerCommand(dispatcher, registryAccess);
        });
    }

    private void registerTickEvents() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.level == null || client.player == null) return;
            EventBus.checkQueue();
            
            tickCounter++;
            // AutoRepair.registerTickEvents(client, tickCounter);
            AutoDrop.registerTickEvents(client, tickCounter);
            AutoAction.registerTickEvents(client, tickCounter);

            // EntityHighlightBox.registerTickEvents(client, tickCounter);
        });
    }

    private void registerSomeEvents() {
        // ScreenEvents.AFTER_INIT.register((client, screen, width, height) -> {
        //     Message.sendMessage("Screen: " + screen.getClass().getSimpleName());
        // });

        EventBus.register("openConfigGui", (args) -> {
            Minecraft client = Minecraft.getInstance();
            if (client.screen != null) {
                EventBus.post("openConfigGui", args);
                return;
            }
            
            YACLScreen configScreen = (YACLScreen) ConfigScreen.getConfigScreen(null);
            configScreen.init(client, client.getWindow().getGuiScaledWidth(), client.getWindow().getGuiScaledHeight());
            int tabIndex = -1;
            String title = (String) args.get("title");
            for (int i=0; i<configScreen.tabNavigationBar.getTabs().size(); i++) {
                if (configScreen.tabNavigationBar.getTabs().get(i).getTabTitle().getString().equals(title)) {
                    tabIndex = i;
                    break;
                }
            }
            if (tabIndex == -1) {
                LOGGER.error("Can not find tab with title: " + title);
                return;
            }
            // true: play click sound
            configScreen.tabNavigationBar.selectTab(tabIndex, true);
            client.setScreen(configScreen);
        });
    }
}
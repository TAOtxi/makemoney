package com.example;

import net.fabricmc.api.ModInitializer;
import net.minecraft.client.Minecraft;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.example.util.T;

import dev.isxander.yacl3.gui.YACLScreen;

import com.example.util.EventBus;
import com.example.util.Message;
import com.example.test.TestMod;
import com.example.gui.ConfigScreen;
import com.example.module.AutoDrop.AutoDrop;
import com.example.module.AutoRepair.AutoRepair;
import com.example.module.EntityHighlightBox.EntityHighlightBox;


public class Makemoney implements ModInitializer {
	public static final String MOD_ID = "makemoney";
    public static boolean isOpenYaclScreen = false;
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    private static int tickCounter = 0;

	@Override
	public void onInitialize() {
		LOGGER.info("Starting mod...");
		LOGGER.info("Mod description: {}", T.t("gui.config.category.fishing.tooltip"));

        
        // AutoRepair.config.remove();
        // AutoDrop.config.remove();
        // EntityHighlightBox.config.remove();
        
		AutoRepair.init();
        AutoDrop.init();
		EntityHighlightBox.init();
        registerTickEvents();
        registerSomeEvents();
        
        TestMod.register();
	}

    private void registerTickEvents() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.level == null || client.player == null) return;
            EventBus.checkQueue();
            
            tickCounter++;
            AutoRepair.registerTickEvents(client, tickCounter);
            AutoDrop.registerTickEvents(client, tickCounter);
            // EntityHighlightBox.registerTickEvents(client, tickCounter);
            // if (tickCounter % 100 == 0) {
            //     Message.sendMessage("try...");
            //     client.setScreen(ConfigScreen.getConfigScreen(client.screen));
            // }
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
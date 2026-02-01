package com.example;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.example.gui.ConfigScreen;
import com.example.module.AutoCommand.AutoCommand;
import com.example.module.AutoDrop.AutoDrop;
import com.example.module.AutoRepair.AutoRepair;
import com.example.module.EntityHighlightBox.EntityHighlightBox;
import com.example.test.TestMod;
import com.example.util.EventBus;
import com.example.util.Message;
import com.example.util.T;

import dev.isxander.yacl3.gui.YACLScreen;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;

public class Makemoney implements ModInitializer {
	public static final String MOD_ID = "makemoney";
    public static boolean isOpenYaclScreen = false;
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    private static int tickCounter = 0;

	@Override
	public void onInitialize() {
		LOGGER.info("Starting mod...");
		LOGGER.info("Mod description: {}", T.t("gui.config.category.fishing.tooltip"));

		TestMod.register();

        // AutoRepair.config.remove();
        // AutoDrop.config.remove();
        // AutoCommand.config.remove();
        // EntityHighlightBox.config.remove();
        
		AutoRepair.init();
        // AutoCommand.init();
        AutoDrop.init();
		// EntityHighlightBox.init();
        registerTickEvents();
        registerSomeEvents();
	}

    private void registerTickEvents() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.level == null || client.player == null) return;
            EventBus.checkQueue();
            
            tickCounter++;
            AutoRepair.registerTickEvents(client, tickCounter);
            // AutoCommand.registerTickEvents(client, tickCounter);
            AutoDrop.registerTickEvents(client, tickCounter);
            // EntityHighlightBox.registerTickEvents(client, tickCounter);
            // if (tickCounter % 100 == 0) {
            //     Message.sendMessage("try...");
            //     client.setScreen(ConfigScreen.getConfigScreen(client.screen));
            // }
        });
    }

    private void registerSomeEvents() {
        ScreenEvents.AFTER_INIT.register((client, screen, width, height) -> {
            // Message.sendMessage("Screen: " + screen.getClass().getSimpleName());
        });

        EventBus.register("openConfigGui", () -> {
            Minecraft client = Minecraft.getInstance();
            if (client.screen != null) return;
            client.setScreen(ConfigScreen.getConfigScreen(null));
            EventBus.removeFromQueue("openConfigGui");
        }, false);
    }
}
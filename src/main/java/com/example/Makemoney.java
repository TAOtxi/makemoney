package com.example;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.example.module.AutoCommand.AutoCommand;
import com.example.module.AutoRepair.AutoRepair;
import com.example.util.T;

public class Makemoney implements ModInitializer {
	public static final String MOD_ID = "makemoney";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("Starting mod...");
		LOGGER.info("Mod description: {}", T.t("makemoney.gui.config.category.fishing.tooltip"));

        // File configDir = FabricLoader.getInstance().getConfigDir().toFile();
        // configDir = new File(configDir, MOD_ID);
        // if (configDir.exists()) {
        //     File configFile = new File(configDir, AutoRepair.MODULE_NAME + ".json");
        //     if (configFile.exists()) {
        //         configFile.delete();
        //     }
        // }

		AutoRepair.init();
        AutoCommand.init();
	}
}
package com.example;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.example.module.AutoCommand.AutoCommand;
import com.example.module.AutoDrop.AutoDrop;
import com.example.module.AutoRepair.AutoRepair;
import com.example.test.TestMod;
import com.example.util.T;

public class Makemoney implements ModInitializer {
	public static final String MOD_ID = "makemoney";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("Starting mod...");
		LOGGER.info("Mod description: {}", T.t("gui.config.category.fishing.tooltip"));

		TestMod.register();

        AutoRepair.config.remove();
        AutoCommand.config.remove();
        AutoDrop.config.remove();
        
		AutoRepair.init();
        // TODO: 待测试 ~~~oh mdfk
        // AutoCommand.init();
        AutoDrop.init();
	}
}
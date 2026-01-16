package com.example;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.example.module.AutoRepair.AutoRepair;
import com.example.util.T;

public class Makemoney implements ModInitializer {
	public static final String MOD_ID = "makemoney";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("Starting mod...");
		LOGGER.info("Mod Name: {}", T.t("makemoney.name"));
		AutoRepair.init();
	}
}
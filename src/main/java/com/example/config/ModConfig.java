package com.example.config;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.fabricmc.loader.api.FabricLoader;
import com.example.Makemoney;

public class ModConfig {
    public final String MODULE_NAME;
    public static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public ModConfig(String moduleName) {
        this.MODULE_NAME = moduleName;
    }

    public static ModConfig load(ModConfig config) {
        File configDir = new File(FabricLoader.getInstance().getConfigDir().toFile(), Makemoney.MOD_ID);
        if (!configDir.exists()) {
            Makemoney.LOGGER.info("Config directory does not exist, creating...");
            configDir.mkdirs();
        }
        File configFile = new File(configDir, config.MODULE_NAME + ".json");
        if (!configFile.exists()) {
            Makemoney.LOGGER.info("Config file does not exist, creating...");
            try (FileWriter writer = new FileWriter(configFile)) {
                gson.toJson(config, writer);
                return config;
            } catch (IOException e) {
                Makemoney.LOGGER.error("[{}] Can not create config file", config.MODULE_NAME, e);
            }
        } else {
            try (FileReader reader = new FileReader(configFile)) {
                return gson.fromJson(reader, config.getClass());
            } catch (Exception e) {
                Makemoney.LOGGER.error("[{}] Can not load config file", config.MODULE_NAME, e);
            }
        }
        return config;
    }

    public static void save(ModConfig config) {
        File configDir = new File(FabricLoader.getInstance().getConfigDir().toFile(), Makemoney.MOD_ID);
        if (!configDir.exists()) {
            Makemoney.LOGGER.info("Config directory does not exist, creating...");
            configDir.mkdirs();
        }
        File configFile = new File(configDir, config.MODULE_NAME + ".json");
        try (FileWriter writer = new FileWriter(configFile)) {
            gson.toJson(config, writer);
        } catch (IOException e) {
            Makemoney.LOGGER.error("[{}] Can not save config file", config.MODULE_NAME, e);
        }
    }
}
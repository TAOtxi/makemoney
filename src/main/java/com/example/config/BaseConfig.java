package com.example.config;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.fabricmc.loader.api.FabricLoader;
import com.example.Makemoney;

public class BaseConfig {
    public final transient String MODULE_NAME;
    public static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public BaseConfig(String moduleName) {
        this.MODULE_NAME = moduleName;
    }

    public static <T extends BaseConfig> T load(Class<T> clazz, String moduleName) {
        File configDir = new File(FabricLoader.getInstance().getConfigDir().toFile(), Makemoney.MOD_ID);
        if (!configDir.exists()) {
            Makemoney.LOGGER.info("Config directory does not exist, creating...");
            configDir.mkdirs();
        }

        File configFile = new File(configDir, moduleName + ".json");
        try {
            if (!configFile.exists()) {
                Makemoney.LOGGER.info("Config file does not exist, creating...");
                T config = clazz.getDeclaredConstructor(String.class).newInstance(moduleName);
                try (FileWriter writer = new FileWriter(configFile)) {
                    gson.toJson(config, writer);
                }
                return config;
            } else {
                try (FileReader reader = new FileReader(configFile)) {
                    return gson.fromJson(reader, clazz);
                }
            }
        } catch (Exception e) {
            Makemoney.LOGGER.error("[{}] Can not load config file", moduleName, e);
            try {
                return clazz.getDeclaredConstructor().newInstance();
            } catch (Exception ex) {
                throw new RuntimeException("Failed to create config instance", ex);
            }
        }
    }

    public void save() {
        File configDir = new File(FabricLoader.getInstance().getConfigDir().toFile(), Makemoney.MOD_ID);
        if (!configDir.exists()) {
            Makemoney.LOGGER.info("Config directory does not exist, creating...");
            configDir.mkdirs();
        }
        File configFile = new File(configDir, MODULE_NAME + ".json");
        try (FileWriter writer = new FileWriter(configFile)) {
            gson.toJson(this, writer);
        } catch (IOException e) {
            Makemoney.LOGGER.error("[{}] Can not save config file", MODULE_NAME, e);
        }
    }
}
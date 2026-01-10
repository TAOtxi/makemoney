package com.example.common;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.impl.discovery.DomainObject.Mod;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import com.example.Makemoney;

public class ModConfig {
    private static final String modelName = "default";

    private static final Gson GSON = new GsonBuilder()
        .setPrettyPrinting()
        .excludeFieldsWithoutExposeAnnotation()
        .create();

    private static final File CONFIG_FILE = new File(
        FabricLoader.getInstance().getConfigDir().toFile(),
        "makemoney.json"
    );

    
    public static ModConfig load(Class<? extends ModConfig> ConfigClass) {
        if (CONFIG_FILE.exists()) {
            try (FileReader reader = new FileReader(CONFIG_FILE)) {
                return GSON.fromJson(reader, ConfigClass);
            } catch (IOException e) {
                Makemoney.LOGGER.error("Unable to load configuration", e);
            }
        }
        return new ModConfig();
    }
    
    public void save() {
        try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
            GSON.toJson(this, writer);
        } catch (IOException e) {
            Makemoney.LOGGER.error("Unable to save configuration", e);
        }
    }
}
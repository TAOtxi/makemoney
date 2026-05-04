package cn.taotxi.Makemoney.util;

import cn.taotxi.Makemoney.Makemoney;
import net.fabricmc.loader.api.FabricLoader;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class ConfigMaker {
    private static final File configDir = new File(FabricLoader.getInstance().getConfigDir().toFile(), Makemoney.MOD_ID);
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public static JsonElement loadConfig(String fileName, JsonElement defaultConfig) {
        if (!configDir.exists()) {
            configDir.mkdirs();
        }
        
        File file = new File(configDir, fileName + ".json");
        if (!file.exists()) {
            try(FileWriter writer = new FileWriter(file)) {
                Makemoney.LOGGER.info("Config not found, creating default config file {}", file);
                gson.toJson(defaultConfig, writer);
                writer.flush();
            } catch (IOException e) {
                Makemoney.LOGGER.error("Failed to write default config to file {}", file, e);
            }
            return defaultConfig;
        }
        
        try(FileReader reader = new FileReader(file)) {
            Makemoney.LOGGER.info("Loading config file {}", file);
            return gson.fromJson(reader, JsonElement.class);
        } catch (IOException e) {
            Makemoney.LOGGER.error("Failed to read config file {}", file, e);
            return defaultConfig;
        }
    }

    public static void saveConfig(String fileName, JsonElement config) {
        if (!configDir.exists()) {
            configDir.mkdirs();
        }

        File file = new File(configDir, fileName + ".json");
        try(FileWriter writer = new FileWriter(file)) {
            Makemoney.LOGGER.info("Saving config file {}", file);
            gson.toJson(config, writer);
            writer.flush();
        } catch (IOException e) {
            Makemoney.LOGGER.error("Failed to write config to file {}", file, e);
        }
    }
}

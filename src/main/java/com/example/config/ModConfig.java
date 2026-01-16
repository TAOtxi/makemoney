package com.example.config;

import com.example.Makemoney;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class ModConfig {
    private final String MODULE_NAME;
    private final File CONFIG_FILE;
    private final Map<String, Object> defaultConfig;
    private final Map<String, Object> config = new HashMap<>();
    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .create();
            
    public ModConfig (String moduleName, Map<String, Object> defaultConfig) {
        this.MODULE_NAME = moduleName;
        this.defaultConfig = defaultConfig;
        CONFIG_FILE = new File(
            FabricLoader.getInstance().getConfigDir().toFile(),
            Makemoney.MOD_ID + "/" + moduleName + ".json"
        );

        load();
    }

    public void load() {
        File configDir = new File(FabricLoader.getInstance().getConfigDir().toFile(), Makemoney.MOD_ID);
        if (!configDir.exists()) {
            configDir.mkdirs();
        }
        if (!CONFIG_FILE.exists()) {
            try {
                reset();
            } catch (Exception e) {
                Makemoney.LOGGER.error("[{}] Can not create config file", MODULE_NAME, e);
            }
        } else {
            try (FileReader reader = new FileReader(CONFIG_FILE)) {
                Type type = new TypeToken<Map<String, Object>>(){}.getType();
                config.clear();
                config.putAll(GSON.fromJson(reader, type));
            } catch (IOException e) {
                Makemoney.LOGGER.error("[{}] Can not load config file", MODULE_NAME, e);
            }
        }
    }
    
    public void doSave() {
        File configDir = new File(FabricLoader.getInstance().getConfigDir().toFile(), Makemoney.MOD_ID);
        if (!configDir.exists()) {
            Makemoney.LOGGER.info("Config directory does not exist, creating...");
            configDir.mkdirs();
        }
        try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
            GSON.toJson(config, writer);
        } catch (IOException e) {
            Makemoney.LOGGER.error("[{}] Can not save config file", MODULE_NAME, e);
        }
    }
    
    public void reset() {
        config.clear();
        config.putAll(defaultConfig);
        save();
    }
            
    public void set(String key, Object value) {
        Makemoney.LOGGER.info("[{}] Set {} to {}", MODULE_NAME, key, value);
        config.put(key, value);
        save();
    }

    public Object get(String key, Boolean isDefault) {
        return isDefault ? defaultConfig.get(key) : config.get(key);
    }

    public String getString(String key) {
        return getString(key, false);
    }

    public boolean getBoolean(String key) {
        return (boolean) get(key, false);
    }

    public int getInt(String key) {
        return (int) get(key, false);
    }

    public double getDouble(String key) {
        return (double) get(key, false);
    }

    
    public String getString(String key, Boolean isDefault) {
        return (String) get(key, isDefault);
    }

    public boolean getBoolean(String key, Boolean isDefault) {
        return (boolean) get(key, isDefault);
    }

    public int getInt(String key, Boolean isDefault) {
        return (int) get(key, isDefault);
    }

    public double getDouble(String key, Boolean isDefault) {
        return (double) get(key, isDefault);
    }
    
    
    private static final ScheduledExecutorService SCHEDULER = Executors.newSingleThreadScheduledExecutor();
    private static ScheduledFuture<?> saveFuture;
    private static final long DEBOUNCE_DELAY = 2000;

    public void save() {
        saveDebounced();
    }
    
    private void saveDebounced() {
        if (saveFuture != null && !saveFuture.isDone()) {
            saveFuture.cancel(false);
        }
        
        saveFuture = SCHEDULER.schedule(() -> {
            doSave();
        }, DEBOUNCE_DELAY, TimeUnit.MILLISECONDS);
    }
    
}
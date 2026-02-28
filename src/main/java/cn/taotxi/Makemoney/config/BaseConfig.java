package cn.taotxi.Makemoney.config;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import cn.taotxi.Makemoney.Makemoney;
import cn.taotxi.Makemoney.util.Debouncer;
import net.fabricmc.loader.api.FabricLoader;

public class BaseConfig {
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private static final File configDir = new File(FabricLoader.getInstance().getConfigDir().toFile(), Makemoney.MOD_ID);
    public transient String MODULE_NAME;
    public transient Debouncer debouncer;

    public BaseConfig(String moduleName) {
        this.MODULE_NAME = moduleName;
    }

    public static <T extends BaseConfig> T load(Class<T> clazz, String moduleName) {
        Makemoney.LOGGER.info("Loading config file for module {}", moduleName);
        if (!configDir.exists()) {
            Makemoney.LOGGER.info("Config directory does not exist, creating...");
            configDir.mkdirs();
        }

        File configFile = new File(configDir, moduleName + ".json");
        // TODO: 加载逻辑待优化
        try {
            if (!configFile.exists()) {
                T config = clazz.getDeclaredConstructor(String.class).newInstance(moduleName);
                Makemoney.LOGGER.info("Config file does not exist, creating {}", configFile.getPath());
                try (FileWriter writer = new FileWriter(configFile)) {
                    gson.toJson(config, writer);
                } catch (IOException e) {
                    Makemoney.LOGGER.error("[{}] Can not save config file the first time", moduleName, e);
                }
                return config;
            } else {
                try (FileReader reader = new FileReader(configFile)) {
                    Makemoney.LOGGER.info("[{}] Loading config file {}", moduleName, configFile.getPath());
                    T config = gson.fromJson(reader, clazz);
                    config.MODULE_NAME = moduleName;
                    return config;
                } catch (IOException e) {
                    Makemoney.LOGGER.error("[{}] Can not load config file, create new one", moduleName, e);
                    T config = clazz.getDeclaredConstructor(String.class).newInstance(moduleName);
                    return config;
                }
            }
        } catch (Exception e) {
            Makemoney.LOGGER.error("[{}] Can not load config file", moduleName, e);
            try {
                return clazz.getDeclaredConstructor(String.class).newInstance(moduleName);
            } catch (Exception ex) {
                throw new RuntimeException("Failed to create config instance", ex);
            }
        }
    }

    public void remove() {
        File configFile = new File(configDir, MODULE_NAME + ".json");
        if (configFile.exists()) {
            configFile.delete();
            Makemoney.LOGGER.info("[{}] Config file deleted from {}", MODULE_NAME, configFile.getPath());
        } else {
            Makemoney.LOGGER.info("[{}] Config file does not exist, nothing to delete", MODULE_NAME);
        }
    }

    private void doSave() {
        if (!configDir.exists()) {
            Makemoney.LOGGER.info("Config directory does not exist, creating {}", configDir.getPath());
            configDir.mkdirs();
        }
        File configFile = new File(configDir, MODULE_NAME + ".json");
        try (FileWriter writer = new FileWriter(configFile)) {
            Makemoney.LOGGER.info("[{}] Saving config file {}", MODULE_NAME, configFile.getPath());
            gson.toJson(this, writer);
        } catch (IOException e) {
            Makemoney.LOGGER.error("[{}] Can not save config file", MODULE_NAME, e);
        }
    }

    public void save() {
        if (debouncer == null) {
            debouncer = new Debouncer(1000);
        }
        debouncer.debounce(() -> {
            doSave();
        });
    }
}
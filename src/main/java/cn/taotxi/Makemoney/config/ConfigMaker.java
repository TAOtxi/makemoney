package cn.taotxi.Makemoney.config;

import cn.taotxi.Makemoney.Makemoney;
import net.fabricmc.loader.api.FabricLoader;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonParseException;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class ConfigMaker {
    private static final File configDir = new File(FabricLoader.getInstance().getConfigDir().toFile(), Makemoney.MOD_ID);
    public static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public static JsonElement loadConfig(String fileName, JsonElement defaultConfig) {
        if (!configDir.exists()) {
            configDir.mkdirs();
        }

        File file = new File(configDir, fileName + ".json");
        if (!file.exists()) {
            Makemoney.LOGGER.info("Config not found, creating default config file {}", file);
            writeConfig(file, defaultConfig);
            // 返回副本，调用方拿到的配置对象不与传入的默认值同体
            return defaultConfig.deepCopy();
        }

        try (Reader reader = Files.newBufferedReader(file.toPath(), StandardCharsets.UTF_8)) {
            Makemoney.LOGGER.info("Loading config file {}", file);
            JsonElement loaded = gson.fromJson(reader, JsonElement.class);

            // 空文件时 Gson 返回 null；结构与默认配置不一致时同样不可用
            if (loaded == null || loaded.isJsonNull()) {
                Makemoney.LOGGER.error("Config file {} is empty, falling back to default config", file);
                return recoverWithDefault(file, defaultConfig);
            }

            if (defaultConfig.isJsonObject() && !loaded.isJsonObject()) {
                Makemoney.LOGGER.error(
                    "Config file {} is not a json object, falling back to default config", file);
                return recoverWithDefault(file, defaultConfig);
            }

            return loaded;
        } catch (JsonParseException e) {
            Makemoney.LOGGER.error("Config file {} is corrupted, falling back to default config", file, e);
            return recoverWithDefault(file, defaultConfig);
        } catch (IOException e) {
            // 读取失败时不覆盖原文件，避免磁盘临时故障导致用户配置丢失
            Makemoney.LOGGER.error("Failed to read config file {}", file, e);
            return defaultConfig.deepCopy();
        }
    }

    public static void saveConfig(String fileName, JsonElement config) {
        if (!configDir.exists()) {
            configDir.mkdirs();
        }

        File file = new File(configDir, fileName + ".json");
        Makemoney.LOGGER.info("Saving config file {}", file);
        writeConfig(file, config);
    }

    /**
     * 备份坏掉的配置文件、重写默认配置，并返回可安全交给调用方的副本。
     */
    private static JsonElement recoverWithDefault(File file, JsonElement defaultConfig) {
        backupBrokenConfig(file);
        writeConfig(file, defaultConfig);
        return defaultConfig.deepCopy();
    }

    /**
     * 先写临时文件再原子替换，避免写入中途崩退留下残缺 JSON。
     */
    private static void writeConfig(File file, JsonElement config) {
        Path target = file.toPath();
        Path tmp = target.resolveSibling(file.getName() + ".tmp");

        try {
            try (Writer writer = Files.newBufferedWriter(tmp, StandardCharsets.UTF_8)) {
                gson.toJson(config == null ? JsonNull.INSTANCE : config, writer);
                writer.flush();
            }

            try {
                Files.move(tmp, target,
                    StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(tmp, target, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            Makemoney.LOGGER.error("Failed to write config to file {}", file, e);
            try {
                Files.deleteIfExists(tmp);
            } catch (IOException suppressed) {
                Makemoney.LOGGER.error("Failed to clean up temp config file {}", tmp, suppressed);
            }
        }
    }

    /**
     * 把无法解析的配置文件另存一份，方便用户手动挽回内容。
     */
    private static void backupBrokenConfig(File file) {
        Path source = file.toPath();
        Path backup = source.resolveSibling(file.getName() + ".broken");
        try {
            Files.move(source, backup, StandardCopyOption.REPLACE_EXISTING);
            Makemoney.LOGGER.warn("Broken config saved as {}", backup);
        } catch (IOException e) {
            Makemoney.LOGGER.error("Failed to back up broken config file {}", file, e);
        }
    }
}

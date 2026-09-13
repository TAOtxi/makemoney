package cn.taotxi.Makemoney.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import cn.taotxi.Makemoney.Makemoney;
import cn.taotxi.Makemoney.config.type.IConfigBase;

// TODO: 配置文件不再存储为JsonElement的形式
public class ConfigManager {
    public String MODULE_NAME;
    private List<IConfigBase<?>> options;
    private JsonObject config;

    public ConfigManager(String moduleName) {
        MODULE_NAME = moduleName;
        options = new ArrayList<>();
    }

    public ConfigManager addOption(IConfigBase<?> option) {
        options.add(option);
        return this;
    }

    private JsonObject createDefaultConfig() {
        JsonObject defaultConfig = new JsonObject();
        for (IConfigBase<?> option : options) {
            defaultConfig.add(option.getKey(), getGson().toJsonTree(option.getDefaultValue()));
        }
        return defaultConfig;
    };

    public void loadConfig() {
        JsonObject defaultConfig = createDefaultConfig();
        config = ConfigMaker
            .loadConfig(MODULE_NAME, defaultConfig)
            .getAsJsonObject();

        // 补齐新版本新增的字段
        boolean added = false;
        for (Map.Entry<String, JsonElement> entry : defaultConfig.entrySet()) {
            if (!config.has(entry.getKey())) {
                config.add(entry.getKey(), entry.getValue().deepCopy());
                added = true;
            }
        }

        boolean removed = pruneUnusedFields(defaultConfig);

        if (added || removed) {
            saveConfig();
        }
    }

    // 清理配置文件里已经没有对应 option 的字段（旧版本遗留）
    private boolean pruneUnusedFields(JsonObject defaultConfig) {
        // 没有注册任何 option 时不做清理，避免把整个配置文件清空
        if (options.isEmpty()) {
            return false;
        }

        List<String> unusedKeys = new ArrayList<>();
        for (String key : config.keySet()) {
            if (!defaultConfig.has(key)) {
                unusedKeys.add(key);
            }
        }
        if (unusedKeys.isEmpty()) {
            return false;
        }

        unusedKeys.forEach(config::remove);
        Makemoney.LOGGER.info("Removed unused config field of {}: {}", MODULE_NAME, unusedKeys);
        return true;
    }

    public void reloadConfig() {
        loadConfig();

        for (IConfigBase<?> option : options) {
            option.triggerConfigChange();
        }
    }
    
    public void saveConfig() {
        ConfigMaker.saveConfig(MODULE_NAME, config);
    }

    public void resetConfig() {
        JsonObject defaultConfig = createDefaultConfig();
        config = defaultConfig.deepCopy();
        saveConfig();

        for (IConfigBase<?> option : options) {
            option.triggerConfigChangeDefault();
        }
    }

    public static Gson getGson() {
        return ConfigMaker.gson;
    }

    public boolean has(String key) {
        return config.has(key);
    }

    public JsonElement get(String key) {
        return config.get(key);
    }

    public ConfigManager set(String key, JsonElement value) {
        config.remove(key);
        config.add(key, value);
        return this;
    }

    public static List<String> jsonArrayToListStr(JsonArray jsonArray) {
        return jsonArray.asList().stream().map(JsonElement::getAsString).collect(Collectors.toList());
    }

    public static List<Integer> jsonIntToList(JsonArray jsonArray) {
        return jsonArray.asList().stream().map(JsonElement::getAsInt).collect(Collectors.toList());
    }

    public static <T> List<T> jsonToList(JsonArray jsonArray, Class<T> type) {
        return jsonArray.asList().stream().map(jsonElement -> getGson().fromJson(jsonElement, type)).collect(Collectors.toList());
    }
}
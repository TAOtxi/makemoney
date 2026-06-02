package cn.taotxi.Makemoney.config;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import cn.taotxi.Makemoney.config.type.IConfigBase;


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
    }

    public void reloadConfig() {
        loadConfig();
    }
    
    public void saveConfig() {
        ConfigMaker.saveConfig(MODULE_NAME, config);
    }

    public void resetConfig() {
        JsonObject defaultConfig = createDefaultConfig();
        config = defaultConfig.deepCopy();
        saveConfig();
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
        if (config.has(key)) {
            config.remove(key);
        }
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
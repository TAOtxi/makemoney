package cn.taotxi.Makemoney.config;

import java.util.List;
import java.util.stream.Collectors;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;


public abstract class ConfigManager {
    public String MODULE_NAME;
    private JsonObject defaultConfig;
    private JsonObject config;

    public ConfigManager(String moduleName) {
        MODULE_NAME = moduleName;
        defaultConfig = createDefaultConfig();
        config = loadConfig(defaultConfig);
    }

    protected abstract JsonObject createDefaultConfig();

    private JsonObject loadConfig(JsonObject defaultConfig) {
        return ConfigMaker
            .loadConfig(MODULE_NAME, defaultConfig)
            .getAsJsonObject();
    }
    
    public void saveConfig() {
        ConfigMaker.saveConfig(MODULE_NAME, config);
    }

    public int getInt(String key, boolean forceDefault) {
        if (config.has(key) && !forceDefault) {
            return config.get(key).getAsInt();
        }
        return defaultConfig.get(key).getAsInt();
    }

    public String getString(String key, boolean forceDefault) {
        if (config.has(key) && !forceDefault) {
            return config.get(key).getAsString();
        }
        return defaultConfig.get(key).getAsString();
    }

    public boolean getBoolean(String key, boolean forceDefault) {
        if (config.has(key) && !forceDefault) {
            return config.get(key).getAsBoolean();
        }
        return defaultConfig.get(key).getAsBoolean();
    }

    public double getDouble(String key, boolean forceDefault) {
        if (config.has(key) && !forceDefault) {
            return config.get(key).getAsDouble();
        }
        return defaultConfig.get(key).getAsDouble();
    }

    public float getFloat(String key, boolean forceDefault) {
        if (config.has(key) && !forceDefault) {
            return config.get(key).getAsFloat();
        }
        return defaultConfig.get(key).getAsFloat();
    }

    public JsonArray getJsonArray(String key, boolean forceDefault) {
        if (config.has(key) && !forceDefault) {
            return config.get(key).getAsJsonArray();
        }
        return defaultConfig.get(key).getAsJsonArray();
    }

    public JsonObject getJsonObject(String key, boolean forceDefault) {
        if (config.has(key) && !forceDefault) {
            return config.get(key).getAsJsonObject();
        }
        return defaultConfig.get(key).getAsJsonObject();
    }

    public int getInt(String key) {
        return getInt(key, false);
    }

    public String getString(String key) {
        return getString(key, false);
    }

    public boolean getBoolean(String key) {
        return getBoolean(key, false);
    }

    public double getDouble(String key) {
        return getDouble(key, false);
    }

    public float getFloat(String key) {
        return getFloat(key, false);
    }

    public JsonArray getJsonArray(String key) {
        return getJsonArray(key, false);
    }

    public JsonObject getJsonObject(String key) {
        return getJsonObject(key, false);
    }

    public void put(String key, JsonElement element) {
        if (config.has(key)) {
            config.remove(key);
        }
        config.add(key, element);
    }

    public void putBoolean(String key, boolean value) {
        put(key, new JsonPrimitive(value));
    }

    public void putInt(String key, int value) {
        put(key, new JsonPrimitive(value));
    }

    public void putString(String key, String value) {
        put(key, new JsonPrimitive(value));
    }

    public void putDouble(String key, double value) {
        put(key, new JsonPrimitive(value));
    }

    public void putFloat(String key, float value) {
        put(key, new JsonPrimitive(value));
    }

    public void putJsonArray(String key, JsonArray value) {
        put(key, value);
    }

    public void putJsonObject(String key, JsonObject value) {
        put(key, value);
    }

    public void reset(String key) {
        if (!config.has(key)) return;
        if (!defaultConfig.has(key)) return;

        JsonElement element = defaultConfig.get(key);
        put(key, element.deepCopy());
    }

    public static List<String> jsonStrToList(JsonArray jsonArray) {
        return jsonArray.asList().stream().map(JsonElement::getAsString).collect(Collectors.toList());
    }
}

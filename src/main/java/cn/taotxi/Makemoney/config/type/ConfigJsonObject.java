package cn.taotxi.Makemoney.config.type;

import java.util.function.BiConsumer;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import cn.taotxi.Makemoney.config.ConfigManager;

public class ConfigJsonObject implements IConfigBase<JsonObject> {
    private String key;
    private String comment;
    private JsonObject defaultValue;
    private BiConsumer<JsonObject, JsonObject> listener;
    private final ConfigManager configManager;
    
    public ConfigJsonObject(String key, JsonObject defaultValue, String comment, ConfigManager configManager) {
        this.key = key;
        this.comment = comment;
        this.defaultValue = defaultValue;
        this.configManager = configManager;
        configManager.addOption(this);
    }
    
    public String getKey() {
        return key;
    }

    @Override
    public String getComment() {
        return comment;
    }

    public void clear() {
        setValue(new JsonObject());
    }
    
    @Override
    public JsonObject getDefaultValue() {
        return defaultValue;
    }   
    
    @Override
    public JsonObject getValue() {
        if (configManager.has(key)) {
            return configManager.get(key).getAsJsonObject();
        }
        return defaultValue;
    }

    public String getString(String key) {
        return getValue().get(key).getAsString();
    }

    public void setString(String key, String value) {
        JsonObject jsonObject = getValue();
        if (jsonObject.has(key)) {
            jsonObject.remove(key);
        }
        jsonObject.addProperty(key, value);
    }

    public int getInt(String key) {
        return getValue().get(key).getAsInt();
    }

    public void setInt(String key, int value) {
        JsonObject jsonObject = getValue();
        if (jsonObject.has(key)) {
            jsonObject.remove(key);
        }
        jsonObject.addProperty(key, value);
    }

    public float getFloat(String key) {
        return getValue().get(key).getAsFloat();
    }
    
    public void setFloat(String key, float value) {
        JsonObject jsonObject = getValue();
        if (jsonObject.has(key)) {
            jsonObject.remove(key);
        }
        jsonObject.addProperty(key, value);
    }

    public boolean getBoolean(String key) {
        return getValue().get(key).getAsBoolean();
    }
    
    public void setBoolean(String key, boolean value) {
        JsonObject jsonObject = getValue();
        if (jsonObject.has(key)) {
            jsonObject.remove(key);
        }
        jsonObject.addProperty(key, value);        
    }

    public double getDouble(String key) {
        return getValue().get(key).getAsDouble();
    }

    public void setDouble(String key, double value) {
        JsonObject jsonObject = getValue();
        if (jsonObject.has(key)) {
            jsonObject.remove(key);
        }
        jsonObject.addProperty(key, value);
    }

    public JsonArray getJsonArray(String key) {
        return getValue().get(key).getAsJsonArray();
    }

    public void setJsonArray(String key, JsonArray value) {
        JsonObject jsonObject = getValue();
        if (jsonObject.has(key)) {
            jsonObject.remove(key);
        }
        jsonObject.add(key, value);
    }

    public JsonObject getJsonObject(String key) {
        return getValue().get(key).getAsJsonObject();
    }
    
    public void setJsonObject(String key, JsonObject value) {
        JsonObject jsonObject = getValue();
        if (jsonObject.has(key)) {
            jsonObject.remove(key);
        }
        jsonObject.add(key, value);
    }
    
    @Override
    public void setValue(JsonObject value) {
        if (listener != null) {
            listener.accept(getValue(), value);
        }
        configManager.set(key, value);
    }
    
    @Override
    public void resetValue() {
        setValue(getDefaultValue().deepCopy());
    }
    
    @Override
    public boolean exists() {
        return configManager.has(key);
    }
    
    @Override
    public void onChange(BiConsumer<JsonObject, JsonObject> listener) {
        this.listener = listener;
    }

    @Override
    public void triggerConfigChangeDefault() {
        if (listener != null) {
            listener.accept(getValue(), getDefaultValue());
        }
    }

    @Override
    public void triggerConfigChange() {
        if (listener != null) {
            JsonObject value = getValue();
            listener.accept(value, value);
        }
    }
}
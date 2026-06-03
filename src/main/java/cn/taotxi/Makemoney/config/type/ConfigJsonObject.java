package cn.taotxi.Makemoney.config.type;

import java.util.function.BiConsumer;

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
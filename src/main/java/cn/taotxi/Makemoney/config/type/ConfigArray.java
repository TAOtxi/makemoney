package cn.taotxi.Makemoney.config.type;

import java.util.List;
import java.util.function.BiConsumer;

import com.google.gson.JsonArray;

import cn.taotxi.Makemoney.config.ConfigManager;

public class ConfigArray implements IConfigBase<JsonArray> {
    private String key;
    private String comment;
    private JsonArray defaultValue;
    private BiConsumer<JsonArray, JsonArray> listener;
    private final ConfigManager configManager;
    
    public ConfigArray(String key, JsonArray defaultValue, String comment, ConfigManager configManager) {
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
    public JsonArray getDefaultValue() {
        return defaultValue;
    }
    
    @Override
    public JsonArray getValue() {
        if (configManager.has(key)) {
            return configManager.get(key).getAsJsonArray();
        }
        return defaultValue;
    }

    public <T> List<T> getValueAsList(Class<T> type) {
        return ConfigManager.jsonToList(getValue(), type);
    }
    
    @Override
    public void setValue(JsonArray value) {
        if (listener != null) {
            listener.accept(getValue(), value);
        }
        configManager.set(key, value);
    }

    public void setValue(List<?> list) {
        setValue(ConfigManager.getGson().toJsonTree(list).getAsJsonArray());
    }

    public int size() {
        return getValue().size();
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
    public void onChange(BiConsumer<JsonArray, JsonArray> listener) {
        this.listener = listener;
    }
}
package cn.taotxi.Makemoney.config.type;

import java.util.function.BiConsumer;

import com.google.gson.JsonPrimitive;

import cn.taotxi.Makemoney.config.ConfigManager;

public class ConfigInteger implements IConfigBase<Integer> {
    private String key;
    private String comment;
    private Integer defaultValue;
    private BiConsumer<Integer, Integer> listener;
    private final ConfigManager configManager;
    
    public ConfigInteger(String key, Integer defaultValue, String comment, ConfigManager configManager) {
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
    public Integer getDefaultValue() {
        return defaultValue;
    }
    
    @Override
    public Integer getValue() {
        if (!configManager.has(key)) {
            configManager.set(key, new JsonPrimitive(defaultValue));
        }
        return configManager.get(key).getAsInt();
    }
    
    @Override
    public void setValue(Integer value) {
        if (listener == null) {
            configManager.set(key, new JsonPrimitive(value));
            return;
        }
        Integer oldValue = getValue();
        configManager.set(key, new JsonPrimitive(value));
        listener.accept(oldValue, value);
    }
    
    @Override
    public void resetValue() {
        setValue(getDefaultValue());
    }
    
    @Override
    public boolean exists() {
        return configManager.has(key);
    }

    @Override
    public void onChange(BiConsumer<Integer, Integer> listener) {
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
            Integer value = getValue();
            listener.accept(value, value);
        }
    }
}
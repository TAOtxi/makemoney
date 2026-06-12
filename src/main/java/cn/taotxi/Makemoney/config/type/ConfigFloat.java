package cn.taotxi.Makemoney.config.type;

import java.util.function.BiConsumer;

import com.google.gson.JsonPrimitive;

import cn.taotxi.Makemoney.config.ConfigManager;

public class ConfigFloat implements IConfigBase<Float> {
    private String key;
    private String comment;
    private float defaultValue;
    private BiConsumer<Float, Float> listener;
    private final ConfigManager configManager;
    
    public ConfigFloat(String key, float defaultValue, String comment, ConfigManager configManager) {
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
    public Float getDefaultValue() {
        return defaultValue;
    }
    
    @Override
    public Float getValue() {
        if (configManager.has(key)) {
            return configManager.get(key).getAsFloat();
        }
        return defaultValue;
    }
    
    @Override
    public void setValue(Float value) {
        if (listener == null) {
            configManager.set(key, new JsonPrimitive(value));
            return;
        }
        Float oldValue = getValue();
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
    public void onChange(BiConsumer<Float, Float> listener) {
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
            Float value = getValue();
            listener.accept(value, value);
        }
    }
}
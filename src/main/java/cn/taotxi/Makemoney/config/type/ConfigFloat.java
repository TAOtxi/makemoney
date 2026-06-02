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
        float oldValue = getValue();
        if (value == oldValue) return;

        if (listener != null) {
            listener.accept(oldValue, value);
        }
        configManager.set(key, new JsonPrimitive(value));
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
}
package cn.taotxi.Makemoney.config.type;

import java.util.function.BiConsumer;

import com.google.gson.JsonPrimitive;

import cn.taotxi.Makemoney.config.ConfigManager;

public class ConfigDouble implements IConfigBase<Double> {
    private String key;
    private String comment;
    private Double defaultValue;
    private BiConsumer<Double, Double> listener;
    private final ConfigManager configManager;
    
    public ConfigDouble(String key, Double defaultValue, String comment, ConfigManager configManager) {
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
    public Double getDefaultValue() {
        return defaultValue;
    }   
    
    @Override
    public Double getValue() {
        if (configManager.has(key)) {
            return configManager.get(key).getAsDouble();
        }
        return defaultValue;
    }
    
    @Override
    public void setValue(Double value) {
        if (listener != null) {
            listener.accept(getValue(), value);
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
    public void onChange(BiConsumer<Double, Double> listener) {
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
            Double value = getValue();
            listener.accept(value, value);
        }
    }
}
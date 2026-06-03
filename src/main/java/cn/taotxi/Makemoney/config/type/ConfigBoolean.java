package cn.taotxi.Makemoney.config.type;

import java.util.function.BiConsumer;

import com.google.gson.JsonPrimitive;

import cn.taotxi.Makemoney.config.ConfigManager;

public class ConfigBoolean implements IConfigBase<Boolean> {
    private String key;
    private String comment;
    private boolean defaultValue;
    private BiConsumer<Boolean, Boolean> listener;
    private final ConfigManager configManager;
    
    public ConfigBoolean(String key, boolean defaultValue, String comment, ConfigManager configManager) {
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
    public Boolean getDefaultValue() {
        return defaultValue;
    }
    
    @Override
    public Boolean getValue() {
        if (configManager.has(key)) {
            return configManager.get(key).getAsBoolean();
        }
        return defaultValue;
    }
    
    @Override
    public void setValue(Boolean value) {
        if (listener != null) {
            listener.accept(getValue(), value);
        }
        configManager.set(key, new JsonPrimitive(value));
    }

    public void enable() {
        setValue(true);
    }

    public void disable() {
        setValue(false);
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
    public void onChange(BiConsumer<Boolean, Boolean> listener) {
        this.listener = listener;
    }
    
    @Override
    public void triggerConfigChange() {
        if (listener != null) {
            listener.accept(getValue(), getDefaultValue());
        }
    }
}
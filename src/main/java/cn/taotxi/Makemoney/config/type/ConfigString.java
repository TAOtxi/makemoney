package cn.taotxi.Makemoney.config.type;

import java.util.function.BiConsumer;

import com.google.gson.JsonPrimitive;

import cn.taotxi.Makemoney.config.ConfigManager;

public class ConfigString implements IConfigBase<String> {
    private String key;
    private String comment;
    private String defaultValue;
    private BiConsumer<String, String> listener;
    private final ConfigManager configManager;
    
    public ConfigString(String key, String defaultValue, String comment, ConfigManager configManager) {
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
    public String getDefaultValue() {
        return defaultValue;
    }   
    
    @Override
    public String getValue() {
        if (configManager.has(key)) {
            return configManager.get(key).getAsString();
        }
        return defaultValue;
    }
    
    @Override
    public void setValue(String value) {
        if (listener != null) {
            listener.accept(getValue(), value);
        }
        configManager.set(key, new JsonPrimitive(value));
    }

    public void setValue(Enum<?> value) {
        setValue(value.name());
    }

    public boolean equals(Enum<?> value) {
        return getValue().equals(value.name());
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
    public void onChange(BiConsumer<String, String> listener) {
        this.listener = listener;
    }

    @Override
    public void triggerConfigChange() {
        if (listener != null) {
            listener.accept(getValue(), getDefaultValue());
        }
    }
}
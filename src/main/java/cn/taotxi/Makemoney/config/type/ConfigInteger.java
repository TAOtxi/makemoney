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
        if (configManager.has(key)) {
            return configManager.get(key).getAsInt();
        }
        return defaultValue;
    }
    
    @Override
    public void setValue(Integer value) {
        int oldValue = getValue();
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
    public void onChange(BiConsumer<Integer, Integer> listener) {
        this.listener = listener;
    }
}
package cn.taotxi.Makemoney.config.type;

import java.util.function.BiConsumer;

import cn.taotxi.Makemoney.config.ConfigManager;

public class ConfigObject implements IConfigBase<Object> {
    private String key;
    private String comment;
    private Object defaultValue;
    private BiConsumer<Object, Object> listener;
    private final ConfigManager configManager;
    
    public ConfigObject(String key, Object defaultValue, String comment, ConfigManager configManager) {
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
    public Object getDefaultValue() {
        return defaultValue;
    }   
    
    @Override
    public Object getValue() {
        if (configManager.has(key)) {
            return configManager.get(key).getAsJsonObject();
        }
        return defaultValue;
    }
    
    @Override
    public void setValue(Object value) {
        if (listener != null) {
            listener.accept(getValue(), value);
        }
        configManager.set(key, ConfigManager.getGson().toJsonTree(value));
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
    public void onChange(BiConsumer<Object, Object> listener) {
        this.listener = listener;
    }
}
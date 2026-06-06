package cn.taotxi.Makemoney.config.type;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

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

    
    public <T> T get(int index, Class<T> type) {
        return ConfigManager.getGson().fromJson(getValue().get(index), type);
    }

    public JsonElement get(int index) {
        return getValue().get(index);
    }

    public <T> List<T> getValueAsList(Class<T> type) {
        return ConfigManager.jsonToList(getValue(), type);
    }

    public List<String> getValueAsStringList() {
        JsonArray jsonList = getValue();
        List<String> list = new ArrayList<>(jsonList.size());
        for (JsonElement element : jsonList) {
            list.add(element.getAsString());
        }
        return list;
    }

    public List<Integer> getValueAsIntList() {
        JsonArray jsonList = getValue();
        List<Integer> list = new ArrayList<>(jsonList.size());
        for (JsonElement element : jsonList) {
            list.add(element.getAsInt());
        }
        return list;
    }

    public void add(String element) {
        getValue().add(element);
    }

    public void add(JsonElement element) {
        getValue().add(element);
    }

    public void addTop(JsonElement element) {
        JsonArray newArray = new JsonArray();
        newArray.add(element);
        newArray.addAll(getValue());
        setValue(newArray);
    }

    public void add(int element) {
        getValue().add(element);
    }

    public void add(boolean element) {
        getValue().add(element);
    }

    public void add(double element) {
        getValue().add(element);
    }

    public void add(float element) {
        getValue().add(element);
    }

    public JsonElement remove(int index) {
        return getValue().remove(index);
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

    public void setStringValue(List<String> list) {
        JsonArray jsonArray = new JsonArray();
        for (String element : list) {
            jsonArray.add(element);
        }
        setValue(jsonArray);
    }

    public int size() {
        return getValue().size();
    }
    
    @Override
    public void resetValue() {
        setValue(getDefaultValue().deepCopy());
    }

    public void clear() {
        setValue(new JsonArray());
    }
    
    @Override
    public boolean exists() {
        return configManager.has(key);
    }
    
    @Override
    public void onChange(BiConsumer<JsonArray, JsonArray> listener) {
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
            JsonArray value = getValue();
            listener.accept(value, value);
        }
    }
}
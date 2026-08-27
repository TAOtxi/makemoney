package cn.taotxi.Makemoney.config.type;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import cn.taotxi.Makemoney.config.ConfigManager;

public class ConfigArray<T> implements IConfigBase<JsonArray> {
    private String key;
    private String comment;
    private JsonArray defaultValue;
    private BiConsumer<JsonArray, JsonArray> listener;
    private final ConfigManager configManager;
    private final Class<T> elementType;
    
    public ConfigArray(String key, JsonArray defaultValue, String comment, ConfigManager configManager) {
        this.key = key;
        this.comment = comment;
        this.defaultValue = defaultValue;
        this.configManager = configManager;
        this.elementType = null;
        configManager.addOption(this);
    }

    public ConfigArray(String key, String comment, ConfigManager configManager) {
        this(key, new JsonArray(), comment, configManager);
    }

    @SuppressWarnings("unchecked")
    public ConfigArray(String key, List<T> defaultValue, String comment, ConfigManager configManager) {
        this.key = key;
        this.comment = comment;
        this.configManager = configManager;
        
        if (!defaultValue.isEmpty()) {
            this.elementType = (Class<T>) defaultValue.get(0).getClass();
        } else {
            this.elementType = null;
        }
        
        JsonArray defaultArray = new JsonArray(defaultValue.size());
        for (T element : defaultValue) {
            addToJsonArray(defaultArray, element);
        }
        this.defaultValue = defaultArray;
        
        configManager.addOption(this);
    }

    public ConfigArray(String key, List<T> defaultValue, String comment, ConfigManager configManager, Class<T> elementType) {
        this.key = key;
        this.comment = comment;
        this.configManager = configManager;
        this.elementType = elementType;
        
        JsonArray defaultArray = new JsonArray(defaultValue.size());
        for (T element : defaultValue) {
            addToJsonArray(defaultArray, element);
        }
        this.defaultValue = defaultArray;
        
        configManager.addOption(this);
    }

    public ConfigArray(String key, String comment, ConfigManager configManager, Class<T> elementType) {
        this.key = key;
        this.comment = comment;
        this.configManager = configManager;
        this.elementType = elementType;
    
        this.defaultValue = new JsonArray();
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
    
    /**
     * 返回配置中实际存储的数组。key 不存在时先把默认值的副本写入配置，
     * 因此返回的始终是配置里的实体，调用方就地修改不会污染默认值。
     */
    @Override
    public JsonArray getValue() {
        if (!configManager.has(key)) {
            configManager.set(key, defaultValue.deepCopy());
        }
        return configManager.get(key).getAsJsonArray();
    }

    @SuppressWarnings("unchecked")
    public T get(int index) {
        JsonElement element = getValue().get(index);
        if (elementType != null) {
            return convertJsonElement(element, elementType);
        }
        return (T) ConfigManager.getGson().fromJson(element, Object.class);
    }

    public JsonElement getRaw(int index) {
        return getValue().get(index);
    }

    public List<T> getValueAsList() {
        JsonArray jsonList = getValue();
        List<T> list = new ArrayList<>(jsonList.size());
        for (JsonElement element : jsonList) {
            list.add(convertJsonElement(element, elementType));
        }
        return list;
    }

    public JsonObject getValueAsObject(int index) {
        return getValue().get(index).getAsJsonObject();
    }

    public void add(T element) {
        addToJsonArray(getValue(), element);
        triggerConfigChange();
    }

    public void add(JsonElement element) {
        getValue().add(element);
        triggerConfigChange();
    }

    public void addTop(T element) {
        JsonArray newArray = new JsonArray();
        addToJsonArray(newArray, element);
        newArray.addAll(getValue());
        setValue(newArray);
    }

    public JsonElement remove(int index) {
        JsonArray value = getValue();
        JsonElement element = value.get(index);
        value.remove(index);
        triggerConfigChange();
        return element;
    }
    
    @Override
    public void setValue(JsonArray value) {
        if (listener == null) {
            configManager.set(key, value);
            return;
        }
        JsonArray oldValue = getValue();
        configManager.set(key, value);
        listener.accept(oldValue, value);
    }

    public void setValue(List<T> list) {
        JsonArray jsonArray = new JsonArray();
        for (T element : list) {
            addToJsonArray(jsonArray, element);
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

    private void addToJsonArray(JsonArray array, T element) {
        if (element instanceof String) {
            array.add((String) element);
        } else if (element instanceof Number) {
            array.add((Number) element);
        } else if (element instanceof Boolean) {
            array.add((Boolean) element);
        } else if (element instanceof Character) {
            array.add(String.valueOf(element));
        } else {
            array.add(ConfigManager.getGson().toJsonTree(element));
        }
    }

    @SuppressWarnings("unchecked")
    private T convertJsonElement(JsonElement element, Class<T> type) {
        if (type == null) {
            return (T) ConfigManager.getGson().fromJson(element, Object.class);
        }
        
        if (type == String.class) {
            return (T) element.getAsString();
        } else if (type == Integer.class || type == int.class) {
            return (T) Integer.valueOf(element.getAsInt());
        } else if (type == Long.class || type == long.class) {
            return (T) Long.valueOf(element.getAsLong());
        } else if (type == Float.class || type == float.class) {
            return (T) Float.valueOf(element.getAsFloat());
        } else if (type == Double.class || type == double.class) {
            return (T) Double.valueOf(element.getAsDouble());
        } else if (type == Boolean.class || type == boolean.class) {
            return (T) Boolean.valueOf(element.getAsBoolean());
        } else {
            return ConfigManager.getGson().fromJson(element, type);
        }
    }
}
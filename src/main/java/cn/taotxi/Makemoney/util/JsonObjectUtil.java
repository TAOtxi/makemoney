package cn.taotxi.Makemoney.util;

import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

public class JsonObjectUtil {
    public static void put(JsonObject jsonObject, String key, JsonElement value) {
        if (jsonObject.has(key)) {
            jsonObject.remove(key);
        }
        jsonObject.add(key, value);
    }

    public static void putInt(JsonObject jsonObject, String key, int value) {
        put(jsonObject, key, new JsonPrimitive(value));
    }

    public static void putBoolean(JsonObject jsonObject, String key, boolean value) {
        put(jsonObject, key, new JsonPrimitive(value));
    }

    public static void putString(JsonObject jsonObject, String key, String value) {
        put(jsonObject, key, new JsonPrimitive(value));
    }

    public static List<String> jsonStrToList(JsonArray jsonArray) {
        return jsonArray.asList().stream().map(JsonElement::getAsString).toList();
    }
}

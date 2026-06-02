package cn.taotxi.Makemoney.util;

import cn.taotxi.Makemoney.Makemoney;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public class T {
    public static final String keyPrefix = Makemoney.MOD_ID + ".";

    public static String tt(String key) {
        return ttl(key).getString();
    }
    
    public static String tt(String key, Object... objects) {
        return ttl(key, objects).getString();
    }

    public static String t(String key) {
        return tt(keyPrefix + key);
    }

    public static String t(String key, Object... objects) {
        return tt(keyPrefix + key, objects);
    }

    public static String t(String key, boolean bool) {
        String boolStr = bool ? t("message.value.on") : t("message.value.off");
        return tt(keyPrefix + key, boolStr);
    }

    public static MutableComponent l(String key) {
        return Component.literal(key);
    }

    public static Component l() {
        return Component.empty();
    }

    public static MutableComponent ttl(String key) {
        return Component.translatable(key);
    }

    public static MutableComponent ttl(String key, Object... objects) {
        return Component.translatable(key, objects);
    } 

    public static MutableComponent tl(String key) {
        return ttl(keyPrefix + key);
    }

    public static MutableComponent tl(String key, Object... objects) {
        return ttl(keyPrefix + key, objects);
    }

    public static MutableComponent ls(Object... objs) {
        MutableComponent component = Component.empty();
        for (Object obj : objs) {
            if (obj instanceof MutableComponent) {
                component.append((MutableComponent) obj);
            } else {
                component.append(obj.toString());
            }
        }
        return component;
    }


}

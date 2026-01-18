package com.example.util;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public class T {
    public static String t(String str) {
        return Component.translatable(str).getString();
    }
    
    public static String t(String str, Object... objects) {
        return Component.translatable(str, objects).getString();
    }

    public static String t(String str, boolean bool) {
        String boolStr = bool ? t("makemoney.message.value.on") : t("makemoney.message.value.off");
        return Component.translatable(str, boolStr).getString();
    }

    public static MutableComponent l(String str) {
        return Component.literal(str);
    }

    public static MutableComponent tl(String str) {
        str = t(str);
        return l(str);
    }

    public static MutableComponent tl(String str, Object... objects) {
        str = t(str, objects);
        return l(str);
    }
}

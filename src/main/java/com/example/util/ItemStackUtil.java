package com.example.util;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.example.Makemoney;

import net.minecraft.world.item.ItemStack;

public class ItemStackUtil {
    public static final Pattern KEY_PATTERN = Pattern.compile("key='(.*?)'");

    public static String getId(ItemStack item) {
        return item.getItemHolder().getRegisteredName();
    }

    public static String getName(ItemStack item) {
        if (item.getCustomName() != null) {
            String name = item.getCustomName().getString();
            return name;
        }

        String translationKey = item.getItemName().toString();
        Matcher matcher = KEY_PATTERN.matcher(translationKey);
        if (matcher.find()) {
            return T.tt(matcher.group(1));
        }
        Makemoney.LOGGER.error("Error translation key: {} !!!", translationKey);
        
        // fallback to id
        return getId(item);
    }

    public static List<String> getTags(ItemStack item) {
        return item.getTags().map(tagKey -> "#" + tagKey.location().toString()).toList();
    }
}

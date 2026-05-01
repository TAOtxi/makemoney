package cn.taotxi.Makemoney.util;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.taotxi.Makemoney.Makemoney;
import net.minecraft.world.item.ItemStack;

public class ItemStackUtil {
    public static final Pattern KEY_PATTERN = Pattern.compile("key='(.*?)'");

    public static String getId(ItemStack item) {
        return item.getItemHolder().getRegisteredName();
    }

    public static String tryToGetTranslateName(ItemStack item) {
        String translationKey = item.getItemName().toString();
        Matcher matcher = KEY_PATTERN.matcher(translationKey);
        if (matcher.find()) {
            return T.tt(matcher.group(1));
        }
        Makemoney.LOGGER.error("Error translation key: {} !!!", translationKey);
        
        // fallback to id
        return getId(item);
    }

    public static String getName(ItemStack item) {
        if (item.getCustomName() != null) {
            return item.getCustomName().getString();
        }
        return tryToGetTranslateName(item);
    }

    public static boolean equalName(ItemStack item, String name) {
        return StringUtil.regMatch(getName(item), name);
    }

    public static boolean equalId(ItemStack item, String id) {
        return StringUtil.regMatch(getId(item), withDefaultNamespace(id));
    }

    public static List<String> getTags(ItemStack item) {
        return item.getTags().map(tagKey -> "#" + tagKey.location().toString()).toList();
    }

    public static boolean isTag(String tag) {
        return tag.startsWith("#");
    }

    public static boolean hasNamespace(String namespace, String id) {
        return id.startsWith(namespace + ":");
    }

    public static boolean hasDefaultNamespace(String id) {
        return hasNamespace("minecraft", id);
    }

    public static String withNamespace(String namespace, String id) {
        if (StringUtil.isRegex(id)) {
            return id;
        };

        if (isTag(id)) {
            String rawId = id.substring(1);
            if (hasNamespace(namespace, rawId)) {
                return id;
            }
            return "#" + namespace + ":" + rawId;
        }

        if (hasNamespace(namespace, id)) {
            return id;
        }
        return namespace + ":" + id;
    }

    public static String withDefaultNamespace(String id) {
        return withNamespace("minecraft", id);
    }

    public static String withoutDefaultNamespace(String id) {
        return id.replace("minecraft:", "");
    }
}

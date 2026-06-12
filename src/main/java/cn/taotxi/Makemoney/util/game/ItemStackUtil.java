package cn.taotxi.Makemoney.util.game;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.taotxi.Makemoney.Makemoney;
import cn.taotxi.Makemoney.util.StringUtil;
import cn.taotxi.Makemoney.util.T;
import net.minecraft.world.item.ItemStack;

public class ItemStackUtil {
    public static final Pattern KEY_PATTERN = Pattern.compile("key='(.*?)'");
    public static final Pattern HAS_NAMESPACE_PATTERN = Pattern.compile("^\\w+:\\w+$");

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
        if (item.isEmpty()) return false;
        return StringUtil.regMatch(getName(item), name);
    }

    public static boolean equalId(ItemStack item, String id) {
        if (item.isEmpty()) return false;
        return StringUtil.regMatch(getId(item), id);
    }

    public static boolean equalIdWithDefaultNamespace(ItemStack item, String id) {
        if (item.isEmpty()) return false;
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

        if (hasNamespace(namespace, id)) {
            return id;
        }
        return namespace + ":" + id;
    }

    public static String withDefaultNamespace(String id) {
        int index = id.indexOf(":");
        if (index != -1 && index != 0 && index != id.length() - 1) {
            return id;
        }
        return withNamespace("minecraft", id);
    }

    public static String tagWithDefaultNamespace(String tag) {
        String rawTag = tag.substring(1);
        return "#" + withNamespace("minecraft", rawTag);
    }

    public static String withoutDefaultNamespace(String id) {
        return id.replace("minecraft:", "");
    }

    
}

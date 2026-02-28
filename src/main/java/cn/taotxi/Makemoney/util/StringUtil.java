package cn.taotxi.Makemoney.util;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class StringUtil {
    public static <Type> String listToStr(List<Type> list, String sep) {
        if (list.isEmpty()) return "";
        
        StringBuilder sb = new StringBuilder();
        for (Type t : list) {
            sb.append(String.valueOf(t)).append(sep);
        }
        sb.setLength(sb.length() - sep.length());
        return sb.toString();
    }

    public static <Type> String listToStr(List<Type> list) {
        return listToStr(list, ", ");
    }

    public static List<String> strToList(String str, String sep) {
        return new ArrayList<>(List.of(str.split(sep)));
    }

    public static List<String> strToList(String str) {
        str = str.replace(" ", "")
                 .replace("，", ",");
        return strToList(str, ",");
    }

    public static List<Integer> strToIntList(String str, String sep) {
        if (str.isEmpty()) return new ArrayList<>();
        return strToList(str, sep).stream().map(Integer::parseInt).collect(Collectors.toList());
    }

    public static List<Integer> strToIntList(String str) {
        str = str.replace(" ", "")
                 .replace("，", ",");
        return strToIntList(str, ",");
    }

    public static String colorToStr(Color color) {
        return String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue())
            .toUpperCase();
    }

    public static Color strToColor(String str) {
        if (!str.matches("#[0-9A-Fa-f]{6}")) {
            return Color.WHITE;
        }
        str = str.substring(1);
        int color = Integer.parseUnsignedInt(str, 16);
        return new Color(color);
    }
}

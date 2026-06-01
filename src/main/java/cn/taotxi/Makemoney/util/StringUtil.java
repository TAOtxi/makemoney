package cn.taotxi.Makemoney.util;

import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


public class StringUtil {
    public static <Type> String join(List<Type> list, String sep) {
        if (list.isEmpty()) return "";
        
        StringBuilder sb = new StringBuilder();
        for (Type t : list) {
            sb.append(String.valueOf(t)).append(sep);
        }
        sb.setLength(sb.length() - sep.length());
        return sb.toString();
    }

    public static <Type> String join(List<Type> list) {
        return join(list, ", ");
    }

    public static String joinStr(List<String> list) {
        return join(list, ", ");
    }

    public static List<String> strToList(String str, String sep) {
        return new ArrayList<>(List.of(str.split(sep)));
    }

    public static String strReplace(String str) {
        return str.replace("，", ",")
                 .replace(", ", ",");
    }

    public static List<String> strToList(String str) {
        return strToList(strReplace(str), ",");
    }

    public static List<Integer> strToIntList(String str, String sep) {
        if (str.isEmpty()) return new ArrayList<>();
        return strToList(str, sep).stream().map(Integer::parseInt).collect(Collectors.toList());
    }

    public static List<Integer> strToIntList(String str) {
        return strToIntList(strReplace(str), ",");
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

    public static boolean isRegex(String str) {
        return str.startsWith("/") && str.endsWith("/");
    }

    public static boolean regMatch(String str, String regex) {
        if (!isRegex(regex)) return str.equals(regex);
        regex = getRawRegex(regex);
        return Pattern.matches(regex, str);
    }

    public static String getRawRegex(String str) {
        return str.substring(1, str.length() - 1);
    }

    public static String toRegex(String str) {
        return "/" + str + "/";
    }

    public static List<Float> parseFloatPos(String str) {
        Pattern pattern = Pattern.compile("\\d+(?:\\.\\d+)?");
        Matcher matcher = pattern.matcher(str);
        List<Float> posList = new ArrayList<>();
        while (matcher.find()) {
            posList.add(Float.parseFloat(matcher.group()));
        }
        return posList;
    }

    public static List<Integer> parseIntPos(String str) {
        Pattern pattern = Pattern.compile("\\d+");
        Matcher matcher = pattern.matcher(str);
        List<Integer> posList = new ArrayList<>();
        while (matcher.find()) {
            posList.add(Integer.parseInt(matcher.group()));
        }
        return posList;
    }

    public static <Type> String posToString(List<Type> pos) {
        return "<" + join(pos, ", ") + ">";
    }

    public static boolean isValidChar(char c) {
        return c >= '0' && c <= '9' ||
               c >= 'a' && c <= 'z' ||
               c >= 'A' && c <= 'Z' ||
               c == '_';
    }

    public static boolean isValidName(String name) {
        int size = name.length();
        if (size < 1 || size > 16) return false;
        for (int i = 0; i < size; i++) {
            char c = name.charAt(i);
            if (!isValidChar(c)) return false;
        }
        return true;
    }
}

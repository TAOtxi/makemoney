package com.example.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
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
        return strToList(str, ", ");
    }

    public static List<Integer> strToIntList(String str, String sep) {
        if (str.isEmpty()) return new ArrayList<>();
        return strToList(str, sep).stream().map(Integer::parseInt).collect(Collectors.toList());
    }

    public static List<Integer> strToIntList(String str) {
        str = str.replace(" ", "")
                 .replace("，", ",");
        return strToIntList(str, ", ");
    }
}

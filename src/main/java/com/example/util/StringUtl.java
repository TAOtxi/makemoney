package com.example.util;

import java.util.List;

public class StringUtl {
    public static <T> String listToString(List<T> list, String sep) {
        if (list.size() < 2) return "";
        
        StringBuilder sb = new StringBuilder();
        for (T t : list) {
            sb.append(String.valueOf(t)).append(sep);
        }
        return sb.toString();
    }

    public static List<String> strToList(String str, String sep) {
        return List.of(str.split(sep));
    }

    public static List<Integer> strToIntList(String str, String sep) {
        return strToList(str, sep).stream().map(Integer::parseInt).toList();
    }




}

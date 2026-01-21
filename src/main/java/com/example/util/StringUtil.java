package com.example.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class StringUtil {
    public static <T> String listToString(List<T> list, String sep) {
        if (list.size() < 1) return "";
        if (list.size() < 2) return String.valueOf(list.get(0));
        
        StringBuilder sb = new StringBuilder();
        for (T t : list) {
            sb.append(String.valueOf(t)).append(sep);
        }
        sb.setLength(sb.length() - sep.length());
        return sb.toString();
    }

    public static List<String> strToList(String str, String sep) {
        return new ArrayList<>(List.of(str.split(sep)));
    }

    public static List<Integer> strToIntList(String str, String sep) {
        if (str.isEmpty()) return new ArrayList<>();
        return strToList(str, sep).stream().map(Integer::parseInt).collect(Collectors.toList());
    }




}

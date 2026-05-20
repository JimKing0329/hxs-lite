package com.hxs.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class StringParseUtil {

    private static final Pattern PAREN = Pattern.compile("\\(([^)]+)\\)");
    private static final Pattern DIGIT = Pattern.compile("\\d+");

    private StringParseUtil() {}

    public static String extractParenthesesContent(String s) {
        if (s == null || s.isEmpty()) return null;
        Matcher m = PAREN.matcher(s);
        return m.find() ? m.group(1) : null;
    }

    public static List<Integer> parseWeekList(String s) {
        List<Integer> r = new ArrayList<>();
        if (s == null || s.isEmpty()) return r;
        for (String p : s.split(",")) {
            p = p.trim();
            if (p.contains("-")) {
                String[] range = p.split("-");
                for (int i = Integer.parseInt(range[0].trim()); i <= Integer.parseInt(range[1].trim()); i++)
                    r.add(i);
            } else r.add(Integer.parseInt(p));
        }
        return r;
    }

    public static List<Integer> parseSessionList(String s) {
        List<Integer> r = new ArrayList<>();
        if (s == null || s.isEmpty()) return r;
        Matcher m = DIGIT.matcher(s);
        while (m.find()) r.add(Integer.parseInt(m.group()));
        return r;
    }

}

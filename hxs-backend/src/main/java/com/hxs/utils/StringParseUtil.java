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
        // 去除"周"、"单"、"双"等非数字/非连字符字符，兼容 "5周"、"1-10周" 等格式
        s = s.replaceAll("[^0-9,-]", "");
        if (s.isEmpty()) return r;
        for (String p : s.split(",")) {
            p = p.trim();
            if (p.isEmpty()) continue;
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


    public static List<String> courseParser(String input) {
        List<String> courseInfo = new ArrayList();
        String courseName = "";
        int starIndex = input.indexOf(9734) == -1 ? input.indexOf(9733) : input.indexOf(9734);
        if (starIndex != -1) {
            courseName = input.substring(0, starIndex + 1);
            if (courseName.contains("★")) {
                courseName = courseName + "理论";
            } else {
                courseName = courseName + "实验";
            }
        } else {
            Pattern namePattern = Pattern.compile("^[\\u4e00-\\u9fa5a-zA-Z0-9]+");
            Matcher nameMatcher = namePattern.matcher(input);
            if (nameMatcher.find()) {
                courseName = nameMatcher.group();
            }
        }

        String weekInfo = "";
        Pattern weekPattern = Pattern.compile("\\d+\\s*-\\s*\\d+周$");
        Matcher weekMatcher = weekPattern.matcher(input);
        if (weekMatcher.find()) {
            weekInfo = weekMatcher.group();
        } else {
            Pattern fallbackPattern = Pattern.compile("共(\\d+)周");
            Matcher fallbackMatcher = fallbackPattern.matcher(input);
            if (fallbackMatcher.find()) {
                weekInfo = fallbackMatcher.group(1) + "周";
            }
        }

        courseInfo.add(courseName);
        courseInfo.add(weekInfo);
        return courseInfo;
    }

}

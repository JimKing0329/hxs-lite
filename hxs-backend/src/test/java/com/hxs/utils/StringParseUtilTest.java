package com.hxs.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("StringParseUtil 工具类测试")
class StringParseUtilTest {

    @Nested
    @DisplayName("extractParenthesesContent - 提取括号内内容")
    class ExtractParenthesesContent {

        @Test
        @DisplayName("正常括号内容（半角括号）")
        void shouldExtractContentInParentheses() {
            assertEquals("080901", StringParseUtil.extractParenthesesContent("计算机科学与技术(080901)"));
        }

        @Test
        @DisplayName("正常括号内容（全角括号不匹配，返回 null）")
        void shouldReturnNullForFullwidth() {
            assertNull(StringParseUtil.extractParenthesesContent("计算机科学与技术（080901）"));
        }

        @Test
        @DisplayName("空字符串返回 null")
        void shouldReturnNullForEmpty() {
            assertNull(StringParseUtil.extractParenthesesContent(""));
        }

        @Test
        @DisplayName("null 返回 null")
        void shouldReturnNullForNull() {
            assertNull(StringParseUtil.extractParenthesesContent(null));
        }

        @Test
        @DisplayName("无括号返回 null")
        void shouldReturnNullWhenNoParentheses() {
            assertNull(StringParseUtil.extractParenthesesContent("计算机科学与技术"));
        }

        @Test
        @DisplayName("嵌套括号取第一层")
        void shouldExtractInnerParentheses() {
            // 正则 \(([^)]+)\) 从第一个 ( 匹配到第一个 )
            assertEquals("方向(080901", StringParseUtil.extractParenthesesContent("专业(方向(080901))"));
        }
    }

    @Nested
    @DisplayName("parseWeekList - 解析周列表")
    class ParseWeekList {

        @Test
        @DisplayName("单周解析")
        void shouldParseSingleWeek() {
            List<Integer> result = StringParseUtil.parseWeekList("3");
            assertEquals(List.of(3), result);
        }

        @Test
        @DisplayName("多周逗号分隔")
        void shouldParseCommaSeparatedWeeks() {
            List<Integer> result = StringParseUtil.parseWeekList("1,3,5");
            assertEquals(List.of(1, 3, 5), result);
        }

        @Test
        @DisplayName("范围解析 1-3")
        void shouldParseRange() {
            List<Integer> result = StringParseUtil.parseWeekList("1-3");
            assertEquals(List.of(1, 2, 3), result);
        }

        @Test
        @DisplayName("混合解析 1-3,5,7-9")
        void shouldParseMixed() {
            List<Integer> result = StringParseUtil.parseWeekList("1-3,5,7-9");
            assertEquals(List.of(1, 2, 3, 5, 7, 8, 9), result);
        }

        @Test
        @DisplayName("空字符串返回空列表")
        void shouldReturnEmptyForEmpty() {
            assertTrue(StringParseUtil.parseWeekList("").isEmpty());
        }

        @Test
        @DisplayName("null 返回空列表")
        void shouldReturnEmptyForNull() {
            assertTrue(StringParseUtil.parseWeekList(null).isEmpty());
        }
    }

    @Nested
    @DisplayName("parseSessionList - 解析节次列表")
    class ParseSessionList {

        @Test
        @DisplayName("提取所有数字")
        void shouldExtractAllDigits() {
            List<Integer> result = StringParseUtil.parseSessionList("1-2节");
            assertEquals(List.of(1, 2), result);
        }

        @Test
        @DisplayName("多节次")
        void shouldExtractMultipleSessions() {
            List<Integer> result = StringParseUtil.parseSessionList("3-4-5节");
            assertEquals(List.of(3, 4, 5), result);
        }

        @Test
        @DisplayName("空字符串返回空列表")
        void shouldReturnEmptyForEmpty() {
            assertTrue(StringParseUtil.parseSessionList("").isEmpty());
        }

        @Test
        @DisplayName("null 返回空列表")
        void shouldReturnEmptyForNull() {
            assertTrue(StringParseUtil.parseSessionList(null).isEmpty());
        }
    }
}

package com.hxs.client;

import com.hxs.constant.URLConstant;
import com.hxs.exception.NotLoginException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("EduSession 测试")
class EduSessionTest {

    private static final Map<String, String> TEST_COOKIES = Map.of(
            "jw", "test-jw-value",
            "JSESSIONID", "test-session-id"
    );

    @Nested
    @DisplayName("构建测试")
    class Construction {

        @Test
        @DisplayName("正常构建不抛异常")
        void shouldConstructSuccessfully() {
            EduSession session = new EduSession(TEST_COOKIES, URLConstant.BASE_URL);
            assertNotNull(session.getHttpClient());
            assertEquals(URLConstant.BASE_URL, session.getBaseUrl());
            assertEquals(TEST_COOKIES, session.getCookies());
        }

        @Test
        @DisplayName("空 cookies 构建")
        void shouldConstructWithEmptyCookies() {
            EduSession session = new EduSession(Map.of(), URLConstant.BASE_URL);
            assertNotNull(session.getHttpClient());
            assertEquals("", session.getCookieString());
        }
    }

    @Nested
    @DisplayName("getCookieString 测试")
    class GetCookieString {

        @Test
        @DisplayName("正常生成 cookie 字符串")
        void shouldFormatCookieString() {
            EduSession session = new EduSession(TEST_COOKIES, URLConstant.BASE_URL);
            String cookieStr = session.getCookieString();
            assertTrue(cookieStr.contains("jw=test-jw-value"));
            assertTrue(cookieStr.contains("JSESSIONID=test-session-id"));
            assertTrue(cookieStr.contains("; "));
        }

        @Test
        @DisplayName("单 cookie 格式正确")
        void shouldFormatSingleCookie() {
            EduSession session = new EduSession(Map.of("token", "abc"), URLConstant.BASE_URL);
            assertEquals("token=abc", session.getCookieString());
        }
    }

    @Nested
    @DisplayName("checkLogin 测试")
    class CheckLogin {

        @Test
        @DisplayName("正常响应不抛异常")
        void shouldNotThrowForNormalResponse() {
            EduSession session = new EduSession(TEST_COOKIES, URLConstant.BASE_URL);
            assertDoesNotThrow(() -> session.checkLogin("{\"status\":\"ok\"}"));
        }

        @Test
        @DisplayName("含'用户登录'文本抛 NotLoginException")
        void shouldThrowWhenBodyContainsLoginHint() {
            EduSession session = new EduSession(TEST_COOKIES, URLConstant.BASE_URL);
            assertThrows(NotLoginException.class,
                    () -> session.checkLogin("<h5>用户登录</h5>"));
        }

        @Test
        @DisplayName("含'身份认证'文本抛 NotLoginException")
        void shouldThrowWhenBodyContainsAuthHint() {
            EduSession session = new EduSession(TEST_COOKIES, URLConstant.BASE_URL);
            assertThrows(NotLoginException.class,
                    () -> session.checkLogin("请先进行身份认证"));
        }

        @Test
        @DisplayName("含'身份'文本抛 NotLoginException")
        void shouldThrowWhenBodyContainsIdentity() {
            EduSession session = new EduSession(TEST_COOKIES, URLConstant.BASE_URL);
            assertThrows(NotLoginException.class,
                    () -> session.checkLogin("<title>身份验证</title>"));
        }
    }

    @Nested
    @DisplayName("close 测试")
    class Close {

        @Test
        @DisplayName("close 不抛异常")
        void shouldCloseWithoutException() {
            EduSession session = new EduSession(TEST_COOKIES, URLConstant.BASE_URL);
            assertDoesNotThrow(session::close);
        }

        @Test
        @DisplayName("重复 close 不抛异常")
        void shouldCloseMultipleTimes() {
            EduSession session = new EduSession(TEST_COOKIES, URLConstant.BASE_URL);
            session.close();
            assertDoesNotThrow(session::close);
        }
    }

    @Nested
    @DisplayName("Cookies 不可变")
    class ImmutableCookies {

        @Test
        @DisplayName("getCookies 返回不可修改 Map")
        void shouldNotBeAbleToModifyOriginalCookies() {
            EduSession session = new EduSession(TEST_COOKIES, URLConstant.BASE_URL);
            assertThrows(UnsupportedOperationException.class,
                    () -> session.getCookies().put("new", "value"));
        }
    }
}

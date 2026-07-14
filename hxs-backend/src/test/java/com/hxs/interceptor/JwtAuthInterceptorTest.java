package com.hxs.interceptor;

import com.hxs.context.UserContext;
import com.hxs.properties.JwtProperties;
import com.hxs.utils.JwtUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.method.HandlerMethod;

import java.lang.reflect.Method;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("JwtAuthInterceptor 测试")
class JwtAuthInterceptorTest {

    private final JwtProperties jwtProperties;

    {
        jwtProperties = mock(JwtProperties.class);
        when(jwtProperties.getUserSecretKey()).thenReturn("test-secret-key-needs-32-bytes-minimum-for-hmac-sha256-ok!");
        when(jwtProperties.getUserTokenName()).thenReturn("token");
    }

    private final JwtAuthInterceptor interceptor = new JwtAuthInterceptor(jwtProperties);

    @AfterEach
    void tearDown() {
        UserContext.removeCurrentId();
    }

    @Nested
    @DisplayName("preHandle")
    class PreHandle {

        @Test
        @DisplayName("有效 Bearer Token 认证成功")
        void shouldAuthWithBearerToken() {
            String token = JwtUtil.createJWT("test-secret-key-needs-32-bytes-minimum-for-hmac-sha256-ok!", 3600_000L, Map.of("userId", "2023015529"));
            MockHttpServletRequest request = new MockHttpServletRequest();
            request.addHeader("Authorization", "Bearer " + token);
            MockHttpServletResponse response = new MockHttpServletResponse();

            boolean result = interceptor.preHandle(request, response, mockHandlerMethod());

            assertTrue(result);
            assertEquals(2023015529L, UserContext.getCurrentId());
        }

        @Test
        @DisplayName("自定义 token 头认证成功")
        void shouldAuthWithCustomTokenHeader() {
            String token = JwtUtil.createJWT("test-secret-key-needs-32-bytes-minimum-for-hmac-sha256-ok!", 3600_000L, Map.of("userId", "2023015529"));
            MockHttpServletRequest request = new MockHttpServletRequest();
            request.addHeader("token", token);
            MockHttpServletResponse response = new MockHttpServletResponse();

            boolean result = interceptor.preHandle(request, response, mockHandlerMethod());

            assertTrue(result);
        }

        @Test
        @DisplayName("无 Token 返回 401")
        void shouldReturn401WithoutToken() {
            MockHttpServletRequest request = new MockHttpServletRequest();
            MockHttpServletResponse response = new MockHttpServletResponse();

            boolean result = interceptor.preHandle(request, response, mockHandlerMethod());

            assertFalse(result);
            assertEquals(401, response.getStatus());
        }

        @Test
        @DisplayName("无效 Token 返回 401")
        void shouldReturn401WithInvalidToken() {
            MockHttpServletRequest request = new MockHttpServletRequest();
            request.addHeader("Authorization", "Bearer invalid.token.here");
            MockHttpServletResponse response = new MockHttpServletResponse();

            boolean result = interceptor.preHandle(request, response, mockHandlerMethod());

            assertFalse(result);
            assertEquals(401, response.getStatus());
        }

        @Test
        @DisplayName("非 HandlerMethod 直接放行")
        void shouldSkipNonHandlerMethod() {
            MockHttpServletRequest request = new MockHttpServletRequest();
            MockHttpServletResponse response = new MockHttpServletResponse();

            boolean result = interceptor.preHandle(request, response, "not-a-handler");

            assertTrue(result);
        }
    }

    @Nested
    @DisplayName("afterCompletion")
    class AfterCompletion {

        @Test
        @DisplayName("清除 ThreadLocal")
        void shouldClearThreadLocal() {
            UserContext.setCurrentId(2023015529L);
            assertNotNull(UserContext.getCurrentId());

            interceptor.afterCompletion(
                    new MockHttpServletRequest(), new MockHttpServletResponse(), null, null);

            assertNull(UserContext.getCurrentId());
        }
    }

    /** 构造一个假的 HandlerMethod */
    private HandlerMethod mockHandlerMethod() {
        try {
            Method method = TestController.class.getMethod("testMethod");
            return new HandlerMethod(new TestController(), method);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    static class TestController {
        public void testMethod() {}
    }
}

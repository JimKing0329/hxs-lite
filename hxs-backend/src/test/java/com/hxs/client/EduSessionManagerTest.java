package com.hxs.client;

import com.hxs.constant.JwtClaimsConstant;
import com.hxs.context.UserContext;
import com.hxs.properties.JwtProperties;
import com.hxs.utils.JwtUtil;
import org.junit.jupiter.api.*;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("EduSessionManager 测试")
class EduSessionManagerTest {

    private JwtProperties jwtProperties;
    private EduSessionManager sessionManager;

    @BeforeEach
    void setUp() {
        jwtProperties = mock(JwtProperties.class);
        when(jwtProperties.getUserSecretKey()).thenReturn("test-secret-key-needs-32-bytes-minimum-for-hmac-sha256-ok!");
        when(jwtProperties.getUserTokenName()).thenReturn("token");
        sessionManager = new EduSessionManager(jwtProperties);

        // 模拟 RequestContext
        MockHttpServletRequest request = new MockHttpServletRequest();
        String token = JwtUtil.createJWT("test-secret-key-needs-32-bytes-minimum-for-hmac-sha256-ok!", 3600_000L, Map.of(
                JwtClaimsConstant.USER_ID, "2023015529",
                JwtClaimsConstant.JW, "jw-test",
                JwtClaimsConstant.JSESSION_ID, "sess-test"
        ));
        request.addHeader("Authorization", "Bearer " + token);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

    @AfterEach
    void tearDown() {
        RequestContextHolder.resetRequestAttributes();
        UserContext.removeCurrentId();
    }

    @Test
    @DisplayName("getOrCreateSession 创建独立 EduSession")
    void shouldCreateSession() {
        EduSession session = sessionManager.getOrCreateSession();
        assertNotNull(session);
        assertNotNull(session.getHttpClient());
        assertNotNull(session.getBaseUrl());
        assertTrue(session.getCookieString().contains("jw=jw-test"));
        assertTrue(session.getCookieString().contains("JSESSIONID=sess-test"));
    }

    @Test
    @DisplayName("重复调用返回同一个 Session")
    void shouldReturnSameSession() {
        EduSession s1 = sessionManager.getOrCreateSession();
        EduSession s2 = sessionManager.getOrCreateSession();
        assertSame(s1, s2);
    }

    @Test
    @DisplayName("destroy 关闭 Session 不抛异常")
    void shouldDestroyGracefully() {
        sessionManager.getOrCreateSession();
        assertDoesNotThrow(() -> sessionManager.destroy());
    }

    @Test
    @DisplayName("未创建 Session 时 destroy 不抛异常")
    void shouldDestroyBeforeCreate() {
        assertDoesNotThrow(() -> sessionManager.destroy());
    }
}

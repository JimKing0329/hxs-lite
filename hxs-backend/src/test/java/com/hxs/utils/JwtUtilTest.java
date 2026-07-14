package com.hxs.utils;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("JwtUtil 工具类测试")
class JwtUtilTest {

    private static final String SECRET = "test-secret-key-needs-32-bytes-minimum-for-hmac-sha256-ok!";

    @Test
    @DisplayName("创建并解析 JWT")
    void shouldCreateAndParseJwt() {
        String token = JwtUtil.createJWT(SECRET, 3600_000L, Map.of("userId", "2023015529", "jw", "test-jw"));
        assertNotNull(token);

        Claims claims = JwtUtil.parseJWT(SECRET, token);
        assertEquals("2023015529", claims.get("userId").toString());
        assertEquals("test-jw", claims.get("jw").toString());
    }

    @Test
    @DisplayName("错误密钥解析失败")
    void shouldFailWithWrongSecret() {
        String token = JwtUtil.createJWT(SECRET, 3600_000L, Map.of("userId", "123"));
        assertThrows(Exception.class, () -> JwtUtil.parseJWT("wrong-secret", token));
    }

    @Test
    @DisplayName("多 claims 场景")
    void shouldHandleMultipleClaims() {
        String token = JwtUtil.createJWT(SECRET, 3600_000L, Map.of(
                "userId", "2023015529",
                "jw", "abc123",
                "jsessionId", "xyz789"
        ));
        Claims claims = JwtUtil.parseJWT(SECRET, token);
        assertEquals("2023015529", claims.get("userId").toString());
        assertEquals("abc123", claims.get("jw").toString());
        assertEquals("xyz789", claims.get("jsessionId").toString());
    }
}

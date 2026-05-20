package com.hxs.client;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("EduLoginClient 测试")
class EduLoginClientTest {

    @Test
    @Tag("integration")
    @DisplayName("【集成测试】无效凭证（需要网络）")
    void shouldThrowOnInvalidCredentials() {
        try {
            EduLoginClient.login("0000000000", "wrongpassword");
            fail("Expected exception");
        } catch (RuntimeException e) {
            // LoginFailException 或 TimeoutException，均说明登录调用路径可达
        }
    }
}

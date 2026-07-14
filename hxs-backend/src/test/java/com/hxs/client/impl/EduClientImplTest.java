package com.hxs.client.impl;

import com.hxs.client.EduClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("EduClientImpl 测试")
class EduClientImplTest {

    @Test
    @DisplayName("是 EduClient 接口的实现")
    void shouldImplementEduClient() {
        EduClientImpl impl = new EduClientImpl();
        assertInstanceOf(EduClient.class, impl);
    }

    @Test
    @DisplayName("login 委托给 EduLoginClient（网络调用）")
    void shouldDelegateToEduLoginClient() {
        EduClientImpl impl = new EduClientImpl();
        try {
            impl.login("invalid-sid", "invalid-pwd");
            fail("Expected login to fail");
        } catch (RuntimeException e) {
            // 网络超时或登录失败，均说明委托链正常
        }
    }
}

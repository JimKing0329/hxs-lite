package com.hxs.result;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Result 统一响应测试")
class ResultTest {

    @Test
    @DisplayName("success() 返回 code=1")
    void shouldReturnSuccess() {
        Result<?> r = Result.success();
        assertEquals(1, r.getCode());
        assertNull(r.getData());
    }

    @Test
    @DisplayName("success(data) 携带数据")
    void shouldReturnSuccessWithData() {
        Result<String> r = Result.success("hello");
        assertEquals(1, r.getCode());
        assertEquals("hello", r.getData());
    }

    @Test
    @DisplayName("error(msg) 返回 code=0")
    void shouldReturnError() {
        Result<?> r = Result.error("出错了");
        assertEquals(0, r.getCode());
        assertEquals("出错了", r.getMsg());
    }
}

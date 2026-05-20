package com.hxs.exception;

import com.hxs.constant.MessageConstant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("异常类测试")
class ExceptionTest {

    @Nested
    @DisplayName("BaseException")
    class BaseExceptionTests {
        @Test
        @DisplayName("无参构造")
        void testNoArg() {
            BaseException ex = new BaseException();
            assertNotNull(ex);
        }

        @Test
        @DisplayName("带消息构造")
        void testWithMessage() {
            BaseException ex = new BaseException("test");
            assertEquals("test", ex.getMessage());
        }

        @Test
        @DisplayName("带消息和原因")
        void testWithCause() {
            RuntimeException cause = new RuntimeException("cause");
            BaseException ex = new BaseException("test", cause);
            assertEquals("test", ex.getMessage());
            assertEquals(cause, ex.getCause());
        }
    }

    @Nested
    @DisplayName("NotLoginException")
    class NotLoginTests {
        @Test
        @DisplayName("携带未登录消息")
        void testNotLoginMessage() {
            NotLoginException ex = new NotLoginException(MessageConstant.UNLOGIN_ERROR);
            assertEquals(MessageConstant.UNLOGIN_ERROR, ex.getMessage());
        }
    }

    @Nested
    @DisplayName("LoginFailException")
    class LoginFailTests {
        @Test
        @DisplayName("携带用户名或密码错误")
        void testUsernameOrPasswordError() {
            LoginFailException ex = new LoginFailException(MessageConstant.USERNAME_OR_PASSWORD_ERROR);
            assertEquals(MessageConstant.USERNAME_OR_PASSWORD_ERROR, ex.getMessage());
        }
    }

    @Nested
    @DisplayName("RequestFailException")
    class RequestFailTests {
        @Test
        @DisplayName("携带请求错误消息")
        void testRequestError() {
            RequestFailException ex = new RequestFailException(MessageConstant.REQUEST_ERROR);
            assertEquals(MessageConstant.REQUEST_ERROR, ex.getMessage());
        }
    }

    @Nested
    @DisplayName("TimeoutException")
    class TimeoutTests {
        @Test
        @DisplayName("携带超时消息")
        void testTimeoutMessage() {
            TimeoutException ex = new TimeoutException(MessageConstant.TIMEOUT_ERROR);
            assertEquals(MessageConstant.TIMEOUT_ERROR, ex.getMessage());
        }
    }

    @Nested
    @DisplayName("BusinessException")
    class BusinessTests {
        @Test
        @DisplayName("业务异常")
        void testBusinessException() {
            BusinessException ex = new BusinessException("业务错误");
            assertInstanceOf(BaseException.class, ex);
            assertEquals("业务错误", ex.getMessage());
        }
    }

    @Nested
    @DisplayName("MessageEmptyException")
    class MessageEmptyTests {
        @Test
        @DisplayName("信息为空异常")
        void testMessageEmpty() {
            MessageEmptyException ex = new MessageEmptyException(MessageConstant.MESSAGE_EMPTY_ERROR);
            assertEquals(MessageConstant.MESSAGE_EMPTY_ERROR, ex.getMessage());
        }
    }
}

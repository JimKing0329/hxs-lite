package com.hxs.client;

import com.hxs.exception.RequestFailException;
import com.hxs.model.entity.User;
import com.hxs.result.Result;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("EduUserClient 测试")
class EduUserClientTest extends HttpMockSupport {

    private static final String STUDENT_INFO_JSON =
            "{" +
            "\"xh\":\"2023015529\"," +
            "\"xm\":\"张三\"," +
            "\"zsjg_id\":\"CS001\"," +
            "\"zyh_id\":\"计算机科学与技术\"," +
            "\"bh_id\":\"计科2101\"," +
            "\"xbm\":\"男\"" +
            "}";

    @Test
    @DisplayName("正常获取学生信息")
    void shouldGetStudentInfo() throws Exception {
        EduSession session = spySession(mockHttpClient(200, STUDENT_INFO_JSON));
        EduUserClient client = new EduUserClient(session);

        Result<User> result = client.getStudentInfo();

        assertNotNull(result);
        assertEquals(1, result.getCode());
        User info = result.getData();
        assertEquals("张三", info.getName());
        assertEquals("计算机科学与技术", info.getMajorName());
        assertEquals("计科2101", info.getClassName());
    }

    @Test
    @DisplayName("空响应抛异常")
    void shouldThrowOnEmptyBody() throws Exception {
        EduSession session = spySession(mockHttpClient(200, ""));
        EduUserClient client = new EduUserClient(session);

        assertThrows(RuntimeException.class, client::getStudentInfo);
    }

    @Test
    @DisplayName("HTTP 异常抛 RequestFailException")
    void shouldThrowOnHttpException() throws Exception {
        EduSession session = spySession(mockHttpClientException());
        EduUserClient client = new EduUserClient(session);

        assertThrows(RequestFailException.class, client::getStudentInfo);
    }
}

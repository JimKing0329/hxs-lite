package com.hxs.client;

import com.hxs.exception.RequestFailException;
import com.hxs.model.support.CourseTableItem;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("EduCourseClient 测试")
class EduCourseClientTest extends HttpMockSupport {

    private static final String COURSE_TABLE_JSON =
            "{\"items\":[{" +
            "\"kch_id\":\"CS101\"," +
            "\"kcmc\":\"数据结构\"," +
            "\"xm\":\"张教授\"," +
            "\"jxbmc\":\"计科2101\"," +
            "\"xf\":3.0," +
            "\"xqj\":1," +
            "\"jc\":\"1-2\"," +
            "\"zcd\":\"1-6,8-16\"," +
            "\"khfsmc\":\"考试\"," +
            "\"xqmc\":\"东校区\"," +
            "\"cdmc\":\"教一301\"," +
            "\"zhxs\":4," +
            "\"zxs\":64" +
            "}]}";

    @Test
    @DisplayName("正常获取课表")
    void shouldGetCourseTable() throws Exception {
        EduSession session = spySession(mockHttpClient(200, COURSE_TABLE_JSON));
        EduCourseClient client = new EduCourseClient(session);

        List<CourseTableItem> result = client.getCourseTable(2024, 3);

        assertNotNull(result);
        assertEquals(1, result.size());
        CourseTableItem item = result.get(0);
        assertEquals("数据结构", item.getTitle());
        assertEquals("张教授", item.getTeacher());
        assertEquals(1, item.getWeekday());
        assertNotNull(item.getWeekList());
        assertTrue(item.getWeekList().contains(1));
    }

    @Test
    @DisplayName("HTTP 异常抛 RequestFailException")
    void shouldThrowOnHttpException() throws Exception {
        EduSession session = spySession(mockHttpClientException());
        EduCourseClient client = new EduCourseClient(session);

        assertThrows(RequestFailException.class, () -> client.getCourseTable(2024, 3));
    }
}

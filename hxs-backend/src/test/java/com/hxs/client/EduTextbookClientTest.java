package com.hxs.client;

import com.hxs.exception.RequestFailException;
import com.hxs.model.support.TextBookItem;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("EduTextbookClient 测试")
class EduTextbookClientTest extends HttpMockSupport {

    private static final String TEXTBOOK_JSON =
            "{\"items\":[" +
            "{\"kcmc\":\"高等数学\",\"jcmc\":\"高等数学（第七版）\",\"jcbb\":\"第七版\",\"zz\":\"同济大学数学系\",\"cbs\":\"高等教育出版社\",\"isbn\":\"978-7-04-052365-0\"}," +
            "{\"kcmc\":\"\",\"jcmc\":\"无教材\",\"jcbb\":\"\",\"zz\":\"\",\"cbs\":\"\",\"isbn\":\"\"}" +
            "]}";

    @Test
    @DisplayName("正常获取教材（过滤掉无教材项）")
    void shouldGetTextBooksFilteringEmpty() throws Exception {
        EduSession session = spySession(mockHttpClient(200, TEXTBOOK_JSON));
        EduTextbookClient client = new EduTextbookClient(session);

        List<TextBookItem> result = client.getTextBook("2024", "3");

        assertNotNull(result);
        // 第二项 "无教材" 和无课程名应被过滤
        assertEquals(1, result.size());
        TextBookItem book = result.get(0);
        assertEquals("高等数学", book.getCourseName());
        assertEquals("高等数学（第七版）", book.getBookName());
        assertEquals("978-7-04-052365-0", book.getIsbn());
    }

    @Test
    @DisplayName("全部是无教材时返回空列表")
    void shouldReturnEmptyWhenAllNoBook() throws Exception {
        String json = "{\"items\":[{\"kcmc\":\"\",\"jcmc\":\"无教材\",\"jcbb\":\"\",\"zz\":\"\",\"cbs\":\"\",\"isbn\":\"\"}]}";
        EduSession session = spySession(mockHttpClient(200, json));
        EduTextbookClient client = new EduTextbookClient(session);

        List<TextBookItem> result = client.getTextBook("2024", "3");
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("HTTP 异常抛 RequestFailException")
    void shouldThrowOnHttpException() throws Exception {
        EduSession session = spySession(mockHttpClientException());
        EduTextbookClient client = new EduTextbookClient(session);

        assertThrows(RequestFailException.class, () -> client.getTextBook("2024", "3"));
    }
}

package com.hxs.client;

import com.hxs.exception.RequestFailException;
import com.hxs.model.support.EmptyClassRoomItem;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("EduClassroomClient 测试")
class EduClassroomClientTest extends HttpMockSupport {



    private static final String CLASSROOM_JSON =
            "{\"items\":[" +
            "{\"cd_id\":\"CR001\",\"cdmc\":\"教二201\",\"xqmc\":\"东校区\",\"cdlbmc\":\"多媒体教室\",\"cdjyz\":\"80\"}," +
            "{\"cd_id\":\"CR002\",\"cdmc\":\"教二202\",\"xqmc\":\"东校区\",\"cdlbmc\":\"普通教室\",\"cdjyz\":\"45\"}" +
            "]}";

    @Test
    @DisplayName("正常获取空教室")
    void shouldGetEmptyClassrooms() throws Exception {
        EduSession session = spySession(mockHttpClient(200, CLASSROOM_JSON));
        EduClassroomClient client = new EduClassroomClient(session);

        List<EmptyClassRoomItem> result = client.getEmptyClassroom(2024, 12, 1, 1, 1);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("教二201", result.get(0).getClassName());
        assertEquals("80", result.get(0).getSeatCount());
    }

    @Test
    @DisplayName("空列表正常返回")
    void shouldReturnEmptyList() throws Exception {
        EduSession session = spySession(mockHttpClient(200, "{\"items\":[]}"));
        EduClassroomClient client = new EduClassroomClient(session);

        List<EmptyClassRoomItem> result = client.getEmptyClassroom(2024, 12, 1, 1, 1);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("HTTP 异常抛 RequestFailException")
    void shouldThrowOnHttpException() throws Exception {
        EduSession session = spySession(mockHttpClientException());
        EduClassroomClient client = new EduClassroomClient(session);

        assertThrows(RequestFailException.class,
                () -> client.getEmptyClassroom(2024, 12, 1, 1, 1));
    }
}

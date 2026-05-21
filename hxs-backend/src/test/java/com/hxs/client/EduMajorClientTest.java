package com.hxs.client;

import com.hxs.exception.RequestFailException;
import com.hxs.model.entity.ExecuteCourse;
import com.hxs.model.support.MajorInfoItem;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("EduMajorClient 测试")
class EduMajorClientTest extends HttpMockSupport {

    private static final String MAJOR_CODE_HTML =
            "<html><body>" +
            "<span class=\"form-control-static\">计算机科学与技术（080901）</span>" +
            "</body></html>";

    private static final String EXECUTE_PLAN_JSON =
            "{\"items\":[{" +
            "\"kcmc\":\"高等数学\"," +
            "\"xf\":\"5.0\"," +
            "\"zxs\":\"80\"," +
            "\"kkxymc\":\"数学学院\"," +
            "\"jyxdxq\":\"1\"," +
            "\"kclbmc\":\"必修\"" +
            "}]}";

    private static final String MAJOR_INFO_JSON =
            "{\"items\":[{" +
            "\"njdm_id\":\"2024\"," +
            "\"nj\":\"2024\"," +
            "\"zyh_id\":\"080901\"," +
            "\"zymc\":\"计算机科学与技术\"," +
            "\"zyfxmc\":\"\"," +
            "\"jxzxjhxx_id\":\"PLAN001\"," +
            "\"zsjg_id\":\"CS\"" +
            "}]}";

    @Nested
    @DisplayName("getMajorCode")
    class GetMajorCode {

        @Test
        @DisplayName("正常获取专业代码")
        void shouldGetMajorCode() throws Exception {
            // getMajorCode 需要 28 个 .form-control-static 元素
            StringBuilder html = new StringBuilder("<html><body>");
            for (int i = 0; i < 27; i++) {
                html.append("<span class=\"form-control-static\">value").append(i).append("</span>");
            }
            html.append("<span class=\"form-control-static\">计算机科学与技术（080901）</span>");
            html.append("</body></html>");

            EduSession session = spySession(mockHttpClient(200, html.toString()));
            EduMajorClient client = new EduMajorClient(session);

            String code = client.getMajorCode();
            assertEquals("计算机科学与技术（080901）", code);
        }

        @Test
        @DisplayName("HTTP 异常抛 RequestFailException")
        void shouldThrowOnException() throws Exception {
            EduSession session = spySession(mockHttpClientException());
            EduMajorClient client = new EduMajorClient(session);
            assertThrows(RequestFailException.class, client::getMajorCode);
        }
    }

    @Nested
    @DisplayName("getExecutePlan")
    class GetExecutePlan {

        @Test
        @DisplayName("正常获取执行计划")
        void shouldGetExecutePlan() throws Exception {
            EduSession session = spySession(mockHttpClient(200, EXECUTE_PLAN_JSON));
            EduMajorClient client = new EduMajorClient(session);

            List<ExecuteCourse> result = client.getExecutePlan("PLAN001");

            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals("高等数学", result.get(0).getCourseName());
            assertEquals("5.0", result.get(0).getCoursePoint());
        }

        @Test
        @DisplayName("HTTP 异常抛 RequestFailException")
        void shouldThrowOnException() throws Exception {
            EduSession session = spySession(mockHttpClientException());
            EduMajorClient client = new EduMajorClient(session);
            assertThrows(RequestFailException.class, () -> client.getExecutePlan("PLAN001"));
        }
    }

    @Nested
    @DisplayName("getMajorInfo")
    class GetMajorInfo {

        @Test
        @DisplayName("正常获取专业信息")
        void shouldGetMajorInfo() throws Exception {
            EduSession session = spySession(mockHttpClient(200, MAJOR_INFO_JSON));
            EduMajorClient client = new EduMajorClient(session);

            List<MajorInfoItem> result = client.getMajorInfo();

            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals("计算机科学与技术", result.get(0).getMajorName());
            assertEquals("PLAN001", result.get(0).getPlanId());
        }

        @Test
        @DisplayName("HTTP 异常抛 RequestFailException")
        void shouldThrowOnException() throws Exception {
            EduSession session = spySession(mockHttpClientException());
            EduMajorClient client = new EduMajorClient(session);
            assertThrows(RequestFailException.class, client::getMajorInfo);
        }
    }
}

package com.hxs.client;

import com.hxs.exception.RequestFailException;
import com.hxs.model.entity.Score;
import com.hxs.model.entity.ExamInfo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("EduExamClient 测试")
class EduExamClientTest extends HttpMockSupport {

    private static final String SCORE_JSON =
            "{\"items\":[{" +
            "\"xnm\":2024," +
            "\"xqm\":1," +
            "\"kcmc\":\"高等数学\"," +
            "\"xf\":\"5.0\"," +
            "\"cj\":\"85\"," +
            "\"jd\":\"3.5\"," +
            "\"kclbmc\":\"必修课\"," +
            "\"kkbmmc\":\"数学学院\"," +
            "\"jsxm\":\"张老师\"," +
            "\"jxb_id\":\"MATH101-001\"," +
            "\"kcbj\":\"0\"," +
            "\"kcxzmc\":\"必修\"" +
            "}]}";

    private static final String EXAM_JSON =
            "{\"items\":[{" +
            "\"kcmc\":\"数据结构\"," +
            "\"kssj\":\"2024-06-15 09:00\"," +
            "\"ksdd\":\"教一301\"," +
            "\"ksxs\":\"闭卷\"," +
            "\"zwh\":\"15\"" +
            "}]}";

    private static final String DETAIL_JSON =
            "{\"items\":[{" +
            "\"kcmc\":\"高等数学\"," +
            "\"xm\":\"张同学\"," +
            "\"xbmc\":\"男\"," +
            "\"xmcj\":\"平时成绩（30%）\"," +
            "\"bfz\":\"30%\"," +
            "\"cj\":\"90\"" +
            "}]}";

    @Nested
    @DisplayName("getStudentScore")
    class GetStudentScore {

        @Test
        @DisplayName("正常获取成绩")
        void shouldGetScores() throws Exception {
            EduSession session = spySession(mockHttpClient(200, SCORE_JSON));
            EduExamClient client = new EduExamClient(session);

            List<Score> result = client.getStudentScore();

            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals("高等数学", result.get(0).getCourseName());
            assertEquals("85", result.get(0).getGrade());
        }

        @Test
        @DisplayName("HTTP 异常抛 RequestFailException")
        void shouldThrowOnHttpException() throws Exception {
            EduSession session = spySession(mockHttpClientException());
            EduExamClient client = new EduExamClient(session);

            assertThrows(RequestFailException.class, client::getStudentScore);
        }
    }

    @Nested
    @DisplayName("getExamSchedule")
    class GetExamSchedule {

        @Test
        @DisplayName("正常获取考试安排")
        void shouldGetExamSchedule() throws Exception {
            EduSession session = spySession(mockHttpClient(200, EXAM_JSON));
            EduExamClient client = new EduExamClient(session);

            List<ExamInfo> result = client.getExamSchedule(2024, 3);

            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals("数据结构", result.get(0).getTitle());
            assertEquals("闭卷", result.get(0).getExamMethod());
        }

        @Test
        @DisplayName("HTTP 异常抛 RequestFailException")
        void shouldThrowOnException() throws Exception {
            EduSession session = spySession(mockHttpClientException());
            EduExamClient client = new EduExamClient(session);

            assertThrows(RequestFailException.class, () -> client.getExamSchedule(2024, 3));
        }
    }

    @Nested
    @DisplayName("getScoreDetail")
    class GetScoreDetail {

        @Test
        @DisplayName("正常获取成绩详情")
        void shouldGetScoreDetail() throws Exception {
            EduSession session = spySession(mockHttpClient(200, DETAIL_JSON));
            EduExamClient client = new EduExamClient(session);

            EduExamClient.ScoreDetailVO result = client.getScoreDetail(2024, 1, "高等数学", "MATH101-001");

            assertNotNull(result);
            assertEquals("高等数学", result.getCourseName());
            assertEquals(1, result.getItems().size());
        }

        @Test
        @DisplayName("HTTP 异常抛 RequestFailException")
        void shouldThrowOnException() throws Exception {
            EduSession session = spySession(mockHttpClientException());
            EduExamClient client = new EduExamClient(session);

            assertThrows(RequestFailException.class,
                    () -> client.getScoreDetail(2024, 1, "高数", "CLS001"));
        }
    }
}

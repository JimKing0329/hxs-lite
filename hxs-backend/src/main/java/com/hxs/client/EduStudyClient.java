package com.hxs.client;

import com.hxs.constant.MessageConstant;
import com.hxs.exception.RequestFailException;
import com.hxs.model.support.StudySituation;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 学习情况模块爬虫 Client — 只负责学习情况（GPA、计划课程等）查询
 */
@Slf4j
public class EduStudyClient {

    private static final Pattern DIGIT = Pattern.compile("\\d+");
    private final EduSession session;

    public EduStudyClient(EduSession session) {
        this.session = session;
    }

    /**
     * 获取学习情况（GPA、已修课程等）
     */
    public StudySituation getStudySituation() {
        try {
            URI situationUrl = new URI(session.getBaseUrl())
                    .resolve("xsxy/xsxyqk_cxXsxyqkIndex.html?gnmkdm=N105515&layout=default");
            HttpGet situationGet = new HttpGet(situationUrl);
            situationGet.setHeader("Cookie", session.getCookieString());

            try (CloseableHttpResponse response = session.getHttpClient().execute(situationGet)) {
                if (response.getStatusLine().getStatusCode() != 200) {
                    throw new RequestFailException(MessageConstant.SYSTEM_ERROR);
                }
                String responseBody = EntityUtils.toString(response.getEntity());
                session.checkLogin(responseBody);

                Document doc = Jsoup.parse(responseBody);
                Elements message = doc.select("font[size=2px]");

                String gpa = message.get(2).text();
                String planCourse = message.get(3).text() + message.get(4).text() + message.get(5).text();
                List<String> planCourseNumbers = extractNumbers(planCourse);

                String outPlanCourse = message.get(6).text() + message.get(7).text();
                List<String> outPlanCourseNumbers = extractNumbers(outPlanCourse);

                StudySituation situation = new StudySituation();
                situation.setGpa(gpa);
                situation.setPlanCourse(planCourseNumbers.get(0));
                situation.setPassPlanCourse(planCourseNumbers.get(1));
                situation.setFailPlanCourse(planCourseNumbers.get(2));
                situation.setUnstudyPlanCourse(planCourseNumbers.get(3));
                situation.setStudyingPlanCourse(planCourseNumbers.get(5));
                situation.setOutPlanPassCourse(outPlanCourseNumbers.get(0));
                situation.setOutPlanFailCourse(outPlanCourseNumbers.get(1));

                log.info("获取学习情况成功 gpa={}", gpa);
                return situation;
            }
        } catch (URISyntaxException | IOException e) {
            log.error("获取学习情况失败", e);
            throw new RequestFailException(MessageConstant.REQUEST_ERROR);
        }
    }

    private List<String> extractNumbers(String text) {
        Matcher matcher = DIGIT.matcher(text);
        List<String> numbers = new ArrayList<>();
        while (matcher.find()) {
            numbers.add(matcher.group());
        }
        return numbers;
    }
}

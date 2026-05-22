package com.hxs.client;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.hxs.constant.MessageConstant;
import com.hxs.exception.MessageEmptyException;
import com.hxs.exception.RequestFailException;
import com.hxs.model.support.ExamScheduleItem;
import com.hxs.model.support.ScoreDetail;
import com.hxs.model.support.ScoreItem;
import com.hxs.utils.StringParseUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * 考试/成绩模块爬虫 Client — 只负责成绩、考试安排、成绩详情
 */
@Slf4j
public class EduExamClient {

    private final EduSession session;

    public EduExamClient(EduSession session) {
        this.session = session;
    }

    /**
     * 获取学生成绩
     */
    public List<ScoreItem> getStudentScore() {
        try {
            URI scoreURL = new URI(session.getBaseUrl())
                    .resolve("/cjcx/cjcx_cxXsgrcj.html?doType=query&gnmkdm=N305005");
            HttpPost httpPost = new HttpPost(scoreURL);
            httpPost.setHeader("Cookie", session.getCookieString());

            List<BasicNameValuePair> formData = new ArrayList<>();
            formData.add(new BasicNameValuePair("xnm", ""));
            formData.add(new BasicNameValuePair("xqm", ""));
            formData.add(new BasicNameValuePair("sfzgcj", ""));
            formData.add(new BasicNameValuePair("queryModel.showCount", "200"));
            formData.add(new BasicNameValuePair("queryModel.currentPage", "1"));
            httpPost.setEntity(new UrlEncodedFormEntity(formData, StandardCharsets.UTF_8));

            try (CloseableHttpResponse response = session.getHttpClient().execute(httpPost)) {
                if (response.getStatusLine().getStatusCode() != 200) {
                    throw new RequestFailException(MessageConstant.LOGIN_FAIL);
                }
                String responseBody = EntityUtils.toString(response.getEntity());
                session.checkLogin(responseBody);

                JSONObject root = JSON.parseObject(responseBody);
                List<ScoreItem> items = root.getList("items", ScoreItem.class);
                log.info("成绩获取成功 {} 条", items.size());
                return items;
            }
        } catch (URISyntaxException | IOException e) {
            log.error("获取学生成绩失败", e);
            throw new RequestFailException(MessageConstant.REQUEST_ERROR);
        }
    }

    /**
     * 获取考试安排
     */
    public List<ExamScheduleItem> getExamSchedule(Integer year, Integer term) {
        try {
            URI resolve = new URI(session.getBaseUrl())
                    .resolve("kwgl/kscx_cxXsksxxIndex.html?doType=query&gnmkdm=N358105");
            HttpPost examPost = new HttpPost(resolve);
            examPost.setHeader("Cookie", session.getCookieString());

            List<BasicNameValuePair> formData = new ArrayList<>();
            formData.add(new BasicNameValuePair("xnm", year.toString()));
            formData.add(new BasicNameValuePair("xqm", term.toString()));
            formData.add(new BasicNameValuePair("_search", "false"));
            formData.add(new BasicNameValuePair("nd", Long.toString(System.currentTimeMillis())));
            formData.add(new BasicNameValuePair("queryModel.showCount", "100"));
            formData.add(new BasicNameValuePair("queryModel.currentPage", "1"));
            formData.add(new BasicNameValuePair("queryModel.sortName", ""));
            formData.add(new BasicNameValuePair("queryModel.sortOrder", "asc"));
            formData.add(new BasicNameValuePair("time", "0"));
            examPost.setEntity(new UrlEncodedFormEntity(formData, "UTF-8"));

            try (CloseableHttpResponse response = session.getHttpClient().execute(examPost)) {
                if (response.getStatusLine().getStatusCode() != 200) {
                    throw new RequestFailException(MessageConstant.SYSTEM_ERROR);
                }
                String responseBody = EntityUtils.toString(response.getEntity());
                session.checkLogin(responseBody);

                JSONObject root = JSON.parseObject(responseBody);
                List<ExamScheduleItem> items = root.getList("items", ExamScheduleItem.class);
                if (items.isEmpty()) {
                    throw new MessageEmptyException(MessageConstant.MESSAGE_EMPTY_ERROR);
                }
                return items;
            }
        } catch (URISyntaxException | IOException e) {
            log.error("获取考试安排失败", e);
            throw new RequestFailException(MessageConstant.REQUEST_ERROR);
        }
    }

    /**
     * 获取成绩详情
     */
    public ScoreDetailVO getScoreDetail(int year, int term, String courseName, String classId) {
        try {
            int paramTerm = term * term * 3;
            URI scoreDetailURL = new URI(session.getBaseUrl())
                    .resolve("cjcx/cjcx_cxXsXmcjList.html?gnmkdm=N305007");
            HttpPost scoreDetailPost = new HttpPost(scoreDetailURL);
            scoreDetailPost.setHeader("Cookie", session.getCookieString());

            List<BasicNameValuePair> formData = new ArrayList<>();
            formData.add(new BasicNameValuePair("xnm", Integer.toString(year)));
            formData.add(new BasicNameValuePair("xqm", Integer.toString(paramTerm)));
            formData.add(new BasicNameValuePair("kcmc", courseName));
            formData.add(new BasicNameValuePair("jxb_id", classId));
            scoreDetailPost.setEntity(new UrlEncodedFormEntity(formData, "UTF-8"));

            try (CloseableHttpResponse response = session.getHttpClient().execute(scoreDetailPost)) {
                if (response.getStatusLine().getStatusCode() != 200) {
                    throw new RequestFailException(MessageConstant.SYSTEM_ERROR);
                }
                String responseBody = EntityUtils.toString(response.getEntity());
                session.checkLogin(responseBody);

                JSONObject root = JSON.parseObject(responseBody);
                List<ScoreDetail> scoreDetails = root.getList("items", ScoreDetail.class);
                scoreDetails.forEach(item ->
                        item.setScoreRatio(StringParseUtil.extractParenthesesContent(item.getScoreColumn()))
                );
                return new ScoreDetailVO(courseName, scoreDetails);
            }
        } catch (URISyntaxException | IOException e) {
            log.error("获取成绩详情失败", e);
            throw new RequestFailException(MessageConstant.REQUEST_ERROR);
        }
    }

    /**
     * 成绩详情 VO
     */
    @lombok.Data
    @lombok.AllArgsConstructor
    public static class ScoreDetailVO {
        private String courseName;
        private List<ScoreDetail> items;
    }
}

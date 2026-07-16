package com.hxs.client;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.hxs.constant.MessageConstant;
import com.hxs.context.UserContext;
import com.hxs.exception.RequestFailException;
import com.hxs.model.entity.ExecuteCourse;
import com.hxs.model.entity.MajorInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

/**
 * 专业信息模块爬虫 Client — 负责专业代码、执行计划、专业信息查询
 */
@Slf4j
public class EduMajorClient {

    private final EduSession session;

    public EduMajorClient(EduSession session) {
        this.session = session;
    }

    /**
     * 获取专业代码
     */
    public String getMajorCode() {
        try {
            URI resolve = new URI(session.getBaseUrl())
                    .resolve("xsxxxggl/xsgrxxwh_cxXsgrxx.html?gnmkdm=N100801&layout=default");
            HttpGet get = new HttpGet(resolve);
            get.setHeader("Cookie", session.getCookieString());

            try (CloseableHttpResponse response = session.getHttpClient().execute(get)) {
                if (response.getStatusLine().getStatusCode() != 200) {
                    throw new RequestFailException(MessageConstant.SYSTEM_ERROR);
                }
                String responseBody = EntityUtils.toString(response.getEntity());
                session.checkLogin(responseBody);
                Document doc = Jsoup.parse(responseBody);
                Elements elements = doc.select(".form-control-static");
                log.info("获取专业代码成功 sid: {} code: {}", UserContext.getCurrentId(), elements.get(27).text());
                return elements.get(27).text();
            }
        } catch (URISyntaxException | IOException e) {
            log.error("获取专业代码失败", e);
            throw new RequestFailException(MessageConstant.REQUEST_ERROR);
        }
    }

    /**
     * 获取执行计划
     */
    public List<ExecuteCourse> getExecutePlan(String majorCode) {
        try {
            URI executePlan = new URI(session.getBaseUrl())
                    .resolve("jxzxjhgl/jxzxjhkcxx_cxJxzxjhkcxxIndex.html?doType=query&gnmkdm=N153540");
            HttpPost executePlanPost = new HttpPost(executePlan);
            executePlanPost.setHeader("Cookie", session.getCookieString());

            List<BasicNameValuePair> formData = new ArrayList<>();
            formData.add(new BasicNameValuePair("jxzxjhxx_id", majorCode));
            formData.add(new BasicNameValuePair("queryModel.currentPage", "1"));
            formData.add(new BasicNameValuePair("queryModel.showCount", "200"));
            formData.add(new BasicNameValuePair("shzt", ""));
            executePlanPost.setEntity(new UrlEncodedFormEntity(formData, "UTF-8"));

            try (CloseableHttpResponse response = session.getHttpClient().execute(executePlanPost)) {
                if (response.getStatusLine().getStatusCode() != 200) {
                    throw new RequestFailException(MessageConstant.SYSTEM_ERROR);
                }
                String responseBody = EntityUtils.toString(response.getEntity());
                session.checkLogin(responseBody);

                JSONObject root = JSON.parseObject(responseBody);
                List<ExecuteCourse> courses = root.getList("items", ExecuteCourse.class);
                log.info("获取执行计划成功 {}", courses.size());
                return courses;
            }
        } catch (URISyntaxException | IOException e) {
            log.error("获取执行计划失败", e);
            throw new RequestFailException(MessageConstant.GET_EXECUTE_PLAN_ERROR);
        }
    }

    /**
     * 获取专业信息
     */
    public List<MajorInfo> getMajorInfo() {
        try {
            URI resolve = new URI(session.getBaseUrl())
                    .resolve("jxzxjhgl/jxzxjhck_cxJxzxjhckIndex.html?doType=query&gnmkdm=N153540");
            HttpPost coursePost = new HttpPost(resolve);
            coursePost.setHeader("Cookie", session.getCookieString());

            List<BasicNameValuePair> formData = new ArrayList<>();
            formData.add(new BasicNameValuePair("queryModel.showCount", "2000"));
            formData.add(new BasicNameValuePair("queryModel.currentPage", "1"));
            coursePost.setEntity(new UrlEncodedFormEntity(formData, "UTF-8"));

            try (CloseableHttpResponse response = session.getHttpClient().execute(coursePost)) {
                if (response.getStatusLine().getStatusCode() != 200) {
                    throw new RequestFailException(MessageConstant.SYSTEM_ERROR);
                }
                String responseBody = EntityUtils.toString(response.getEntity());
                session.checkLogin(responseBody);

                JSONObject root = JSON.parseObject(responseBody);
                List<MajorInfo> items = root.getList("items", MajorInfo.class);
                log.info("获取专业信息成功 {} 条", items.size());
                return items;
            }
        } catch (URISyntaxException | IOException e) {
            log.error("获取专业信息失败", e);
            throw new RequestFailException(MessageConstant.REQUEST_ERROR);
        }
    }
}

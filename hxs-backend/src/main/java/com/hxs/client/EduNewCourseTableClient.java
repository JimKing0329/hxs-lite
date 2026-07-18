package com.hxs.client;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.hxs.constant.MessageConstant;
import com.hxs.exception.RequestFailException;
import com.hxs.model.entity.NewCourseTable;
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
 * 班级课表模块爬虫 Client — 负责班级列表查询与班级课表爬取
 */
@Slf4j
public class EduNewCourseTableClient {

    private static final String CLASS_LIST_PATH = "kbdy/bjkbdy_cxBjkbdyTjkbList.html?gnmkdm=N214505";
    private static final String CLASS_COURSE_PATH = "kbdy/bjkbdy_cxBjKb.html?gnmkdm=N214505";

    private final EduSession session;

    public EduNewCourseTableClient(EduSession session) {
        this.session = session;
    }

    /**
     * 获取班级列表
     */
    public List<NewCourseTable> getAllClass(Integer year, Integer term) {
        try {
            URI uri = new URI(session.getBaseUrl()).resolve(CLASS_LIST_PATH);
            HttpPost httpPost = new HttpPost(uri);
            httpPost.setHeader("Cookie", session.getCookieString());

            List<BasicNameValuePair> formData = new ArrayList<>();
            formData.add(new BasicNameValuePair("xnm", year.toString()));
            formData.add(new BasicNameValuePair("xqm", term.toString()));
            formData.add(new BasicNameValuePair("queryModel.showCount", "5000"));
            formData.add(new BasicNameValuePair("queryModel.currentPage", "1"));
            httpPost.setEntity(new UrlEncodedFormEntity(formData, StandardCharsets.UTF_8));

            try (CloseableHttpResponse response = session.getHttpClient().execute(httpPost)) {
                if (response.getStatusLine().getStatusCode() != 200) {
                    throw new RequestFailException(MessageConstant.REQUEST_ERROR);
                }
                String responseBody = EntityUtils.toString(response.getEntity());
                session.checkLogin(responseBody);

                JSONObject root = JSON.parseObject(responseBody);
                List<NewCourseTable> items = root.getList("items", NewCourseTable.class);
                log.info("获取班级列表成功 {} 条", items.size());
                return items;
            }
        } catch (URISyntaxException | IOException e) {
            log.error("获取班级列表失败", e);
            throw new RequestFailException(MessageConstant.REQUEST_ERROR);
        }
    }

    /**
     * 获取班级课表（返回原始 JSON，包含 kbList 和 sjkList 两个列表）
     */
    public JSONObject updateClassCourseTable(NewCourseTable classInfo, Integer year, Integer term) {
        try {
            URI uri = new URI(session.getBaseUrl()).resolve(CLASS_COURSE_PATH);
            HttpPost httpPost = new HttpPost(uri);
            httpPost.setHeader("Cookie", session.getCookieString());

            List<BasicNameValuePair> formData = new ArrayList<>();
            formData.add(new BasicNameValuePair("xnm", year.toString()));
            formData.add(new BasicNameValuePair("xqm", term.toString()));
            formData.add(new BasicNameValuePair("njdm_id", classInfo.getGrade()));
            formData.add(new BasicNameValuePair("zyh_id", classInfo.getMajorId()));
            formData.add(new BasicNameValuePair("bh_id", classInfo.getClassId()));
            formData.add(new BasicNameValuePair("tjkbzdm", "1"));
            formData.add(new BasicNameValuePair("tjkbzxsdm", "0"));
            httpPost.setEntity(new UrlEncodedFormEntity(formData, StandardCharsets.UTF_8));

            try (CloseableHttpResponse response = session.getHttpClient().execute(httpPost)) {
                if (response.getStatusLine().getStatusCode() != 200) {
                    throw new RequestFailException(MessageConstant.REQUEST_ERROR);
                }
                String responseBody = EntityUtils.toString(response.getEntity());
                session.checkLogin(responseBody);

                JSONObject root = JSON.parseObject(responseBody);
                return root;
            }
        } catch (URISyntaxException | IOException e) {
            log.error("获取班级课表失败", e);
            throw new RequestFailException(MessageConstant.REQUEST_ERROR);
        }
    }
}

package com.hxs.client;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.hxs.constant.MessageConstant;
import com.hxs.exception.MessageEmptyException;
import com.hxs.exception.RequestFailException;
import com.hxs.model.support.CourseTableItem;
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
import java.util.ArrayList;
import java.util.List;

/**
 * 课程模块爬虫 Client — 只负责课表爬取
 */
@Slf4j
public class EduCourseClient {

    public static final String COURSE_TABLE_FILED = "kbList";
    private final EduSession session;

    public EduCourseClient(EduSession session) {
        this.session = session;
    }

    /**
     * 获取课表
     */
    public List<CourseTableItem> getCourseTable(Integer year, Integer term) {
        try {
            URI resolve = new URI(session.getBaseUrl())
                    .resolve("kbcx/xskbcx_cxXsKb.html?gnmkdm=N2151");
            HttpPost coursePost = new HttpPost(resolve);
            coursePost.setHeader("Cookie", session.getCookieString());

            List<BasicNameValuePair> formData = new ArrayList<>();
            formData.add(new BasicNameValuePair("xnm", year.toString()));
            formData.add(new BasicNameValuePair("xqm", term.toString()));
            coursePost.setEntity(new UrlEncodedFormEntity(formData, "UTF-8"));

            try (CloseableHttpResponse response = session.getHttpClient().execute(coursePost)) {
                if (response.getStatusLine().getStatusCode() != 200) {
                    throw new RequestFailException(MessageConstant.SYSTEM_ERROR);
                }
                String responseBody = EntityUtils.toString(response.getEntity());
                session.checkLogin(responseBody);

                JSONObject root = JSON.parseObject(responseBody);
                List<CourseTableItem> items = root.getList(COURSE_TABLE_FILED, CourseTableItem.class);
                if (items.isEmpty()) {
                    throw new MessageEmptyException(MessageConstant.MESSAGE_EMPTY_ERROR);
                }
                items.forEach(item ->
                        item.setWeekList(StringParseUtil.parseWeekList(item.getWeeks()))
                );
                log.info("获取课表成功 {} 条", items.size());
                return items;
            }
        } catch (URISyntaxException | IOException e) {
            log.error("获取课表失败", e);
            throw new RequestFailException(MessageConstant.REQUEST_ERROR);
        }
    }
}

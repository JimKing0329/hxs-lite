package com.hxs.client;

import com.alibaba.fastjson2.JSON;
import com.hxs.constant.MessageConstant;
import com.hxs.exception.RequestFailException;
import com.hxs.model.support.EmptyClassRoomItem;
import com.hxs.model.support.EmptyClassroomResponse;
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
 * 空教室模块爬虫 Client — 只负责空教室查询
 */
@Slf4j
public class EduClassroomClient {

    private final EduSession session;

    public EduClassroomClient(EduSession session) {
        this.session = session;
    }

    /**
     * 获取单时段的空教室
     */
    public List<EmptyClassRoomItem> getEmptyClassroom(Integer year, Integer term,
                                                       Integer week, Integer weekday, Integer sessionNum) {
        try {
            URI resolve = new URI(session.getBaseUrl())
                    .resolve("cdjy/cdjy_cxKxcdlb.html?doType=query&gnmkdm=N2155");
            HttpPost emptyClassRoomPost = new HttpPost(resolve);
            emptyClassRoomPost.setHeader("Cookie", session.getCookieString());

            List<BasicNameValuePair> formData = new ArrayList<>();
            formData.add(new BasicNameValuePair("fwzt", "cx"));
            formData.add(new BasicNameValuePair("xnm", year.toString()));
            formData.add(new BasicNameValuePair("xqm", term.toString()));
            formData.add(new BasicNameValuePair("jyfs", "0"));
            formData.add(new BasicNameValuePair("zcd", week.toString()));
            formData.add(new BasicNameValuePair("xqj", weekday.toString()));
            formData.add(new BasicNameValuePair("jcd", sessionNum.toString()));
            formData.add(new BasicNameValuePair("queryModel.showCount", "1000"));
            emptyClassRoomPost.setEntity(new UrlEncodedFormEntity(formData, "UTF-8"));

            try (CloseableHttpResponse response = session.getHttpClient().execute(emptyClassRoomPost)) {
                if (response.getStatusLine().getStatusCode() != 200) {
                    throw new RequestFailException(MessageConstant.SYSTEM_ERROR);
                }
                String responseBody = EntityUtils.toString(response.getEntity());
                EmptyClassroomResponse emptyResp = JSON.parseObject(responseBody, EmptyClassroomResponse.class);

                List<EmptyClassRoomItem> list = emptyResp.getEmptyClassRoomItems();
                log.debug("获取空教室成功 {} 条", list.size());
                return list;
            }
        } catch (URISyntaxException | IOException e) {
            log.error("获取空教室失败", e);
            throw new RequestFailException(MessageConstant.GET_EMPTY_CLASSROOM_ERROR);
        }
    }
}

package com.hxs.client;

import com.alibaba.fastjson2.JSON;
import com.hxs.constant.MessageConstant;
import com.hxs.exception.LoginFailException;
import com.hxs.exception.MessageEmptyException;
import com.hxs.exception.RequestFailException;
import com.hxs.model.entity.User;
import com.hxs.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * 用户信息模块爬虫 Client — 只负责学生信息查询
 */
@Slf4j
public class EduUserClient {

    private final EduSession session;

    public EduUserClient(EduSession session) {
        this.session = session;
    }

    /**
     * 获取学生信息
     */
    public Result<User> getStudentInfo() {
        try {
            URI getInfoURL = new URI(session.getBaseUrl())
                    .resolve("xsxxxggl/xsxxwh_cxCkDgxsxx.html?gnmkdm=N100801");
            HttpGet httpGet = new HttpGet(getInfoURL);
            httpGet.setHeader("Cookie", session.getCookieString());

            try (CloseableHttpResponse response = session.getHttpClient().execute(httpGet)) {
                if (response.getStatusLine().getStatusCode() != 200) {
                    throw new LoginFailException(MessageConstant.LOGIN_FAIL);
                }
                String responseBody = EntityUtils.toString(response.getEntity());
                log.info("获取学生信息成功");

                if (responseBody.isEmpty()) {
                    throw new MessageEmptyException(MessageConstant.MESSAGE_EMPTY_ERROR);
                }
                session.checkLogin(responseBody);

                User data = JSON.parseObject(responseBody, User.class);
                return Result.success(data);
            }
        } catch (URISyntaxException | IOException e) {
            log.error("获取学生信息失败: {}", e.getMessage(), e);
            throw new RequestFailException(MessageConstant.REQUEST_ERROR);
        }
    }
}

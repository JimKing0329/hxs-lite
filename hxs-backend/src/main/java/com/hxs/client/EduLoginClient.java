package com.hxs.client;

import com.hxs.constant.MessageConstant;
import com.hxs.constant.URLConstant;
import com.hxs.exception.LoginFailException;
import com.hxs.exception.TimeoutException;
import com.hxs.utils.RsaUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.CookieStore;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.apache.http.message.BasicNameValuePair;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

/**
 * 教务系统登录客户端 — 只负责登录操作
 * 登录成功后返回已认证的 EduSession（复用同一个 HttpClient 连接）
 */
@Slf4j
public class EduLoginClient {

    private static final int TIMEOUT = 15;
    private static final String BASE_URL = URLConstant.BASE_URL;

    /**
     * 登录并返回已认证的 EduSession（复用登录时的 HttpClient，避免重建连接）
     */
    public static EduSession login(String sid, String password) {
        log.info("教务系统登录开始 sid={}", sid);
        String keyUrl;
        String loginUrl;
        try {
            keyUrl = new URI(BASE_URL).resolve("xtgl/login_getPublicKey.html").toString();
            loginUrl = new URI(BASE_URL).resolve("xtgl/login_slogin.html").toString();
        } catch (URISyntaxException e) {
            throw new RuntimeException("Invalid base URL", e);
        }

        CookieStore cookieStore = new BasicCookieStore();
        RequestConfig config = RequestConfig.custom()
                .setConnectTimeout(TIMEOUT * 1000)
                .setSocketTimeout(TIMEOUT * 1000)
                .build();

        CloseableHttpClient httpClient = HttpClients.custom()
                .setDefaultRequestConfig(config)
                .setRedirectStrategy(new LaxRedirectStrategy())
                .setDefaultCookieStore(cookieStore)
                .build();

        try {

            // 1. 请求登录页获取 CSRF token
            HttpGet reqCsrf = new HttpGet(loginUrl);
            try (CloseableHttpResponse response = httpClient.execute(reqCsrf)) {
                if (response.getStatusLine().getStatusCode() != 200) {
                    httpClient.close();
                    throw new LoginFailException(MessageConstant.LOGIN_FAIL);
                }
                String responseBody = EntityUtils.toString(response.getEntity());
                Document doc = Jsoup.parse(responseBody);
                Element csrfElement = doc.selectFirst("#csrftoken");
                String csrfToken = csrfElement != null ? csrfElement.attr("value") : "";
                log.debug("CSRF Token: {}", csrfToken);

                // 2. 获取 RSA 公钥并加密密码
                HttpGet reqPubkey = new HttpGet(keyUrl);
                try (CloseableHttpResponse pubkeyResponse = httpClient.execute(reqPubkey)) {
                    String pubkeyBody = EntityUtils.toString(pubkeyResponse.getEntity());
                    JSONObject pubkeyJson = JSON.parseObject(pubkeyBody);
                    String modulus = pubkeyJson.getString("modulus");
                    String exponent = pubkeyJson.getString("exponent");
                    String encryptPassword = RsaUtil.encrypt(password, modulus, exponent);
                    log.debug("RSA加密密码完成");

                    // 3. 提交登录
                    List<BasicNameValuePair> formData = new ArrayList<>();
                    formData.add(new BasicNameValuePair("csrftoken", csrfToken));
                    formData.add(new BasicNameValuePair("yhm", sid));
                    formData.add(new BasicNameValuePair("mm", encryptPassword));

                    HttpPost reqLogin = new HttpPost(loginUrl);
                    reqLogin.setEntity(new UrlEncodedFormEntity(formData, "UTF-8"));

                    try (CloseableHttpResponse loginResponse = httpClient.execute(reqLogin)) {
                        String loginBody = EntityUtils.toString(loginResponse.getEntity());
                        Document loginDoc = Jsoup.parse(loginBody);
                        Element tipsElement = loginDoc.selectFirst("p#tips");

                        if (tipsElement != null) {
                            String tipsText = tipsElement.text();
                            httpClient.close();
                            if (tipsText.contains("用户名或密码")) {
                                log.error("用户名或密码不正确");
                                throw new LoginFailException(MessageConstant.USERNAME_OR_PASSWORD_ERROR);
                            }
                            throw new LoginFailException(tipsText);
                        }

                        // 4. 从 CookieStore 收集 cookies → 构建已登录的 EduSession
                        Map<String, String> cookies = new HashMap<>();
                        cookieStore.getCookies().forEach(c ->
                                cookies.put(c.getName(), c.getValue()));

                        log.info("教务系统登录成功 sid={}", sid);
                        return new EduSession(httpClient, cookies, BASE_URL);
                    }
                }
            }
        } catch (IOException e) {
            try { httpClient.close(); } catch (IOException ignored) {}
            log.error("登录超时 {}", e.getMessage());
            throw new TimeoutException(MessageConstant.TIMEOUT_ERROR);
        } catch (LoginFailException e) {
            throw e;
        } catch (Exception e) {
            try { httpClient.close(); } catch (IOException ignored) {}
            log.error("登录错误: {}", e.getMessage());
            throw new LoginFailException(e.getMessage());
        }
    }
}

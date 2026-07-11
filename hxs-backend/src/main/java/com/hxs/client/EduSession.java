package com.hxs.client;

import com.hxs.constant.MessageConstant;
import com.hxs.exception.NotLoginException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.apache.http.impl.cookie.BasicClientCookie;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 教务系统用户会话封装
 * 每个用户一个独立实例，不可变且线程安全
 */
public class EduSession implements AutoCloseable {

    private static final int CONNECT_TIMEOUT = 15_000;
    private static final int SOCKET_TIMEOUT = 15_000;
    private static final String COOKIE_DOMAIN = "jwgl.hebtu.edu.cn";
    private static final String COOKIE_PATH = "/";

    private final CloseableHttpClient httpClient;
    private final Map<String, String> cookies;
    private final String baseUrl;

    /**
     * 从 cookies 构建新 Session（创建全新的 HttpClient + CookieStore）
     */
    public EduSession(Map<String, String> cookies, String baseUrl) {
        this.cookies = Collections.unmodifiableMap(new HashMap<>(cookies));
        this.baseUrl = baseUrl;

        BasicCookieStore cookieStore = new BasicCookieStore();
        cookies.forEach((key, value) -> {
            BasicClientCookie cookie = new BasicClientCookie(key, value);
            cookie.setDomain(COOKIE_DOMAIN);
            cookie.setPath(COOKIE_PATH);
            cookieStore.addCookie(cookie);
        });

        RequestConfig config = RequestConfig.custom()
                .setConnectTimeout(CONNECT_TIMEOUT)
                .setSocketTimeout(SOCKET_TIMEOUT)
                .build();

        this.httpClient = HttpClients.custom()
                .setDefaultRequestConfig(config)
                .setDefaultCookieStore(cookieStore)
                .setRedirectStrategy(new LaxRedirectStrategy())
                .build();
    }

    /**
     * 从已有 HttpClient 构建 Session（用于登录后复用连接，避免重建 TCP/SSL）
     */
    EduSession(CloseableHttpClient httpClient, Map<String, String> cookies, String baseUrl) {
        this.httpClient = httpClient;
        this.cookies = Collections.unmodifiableMap(new HashMap<>(cookies));
        this.baseUrl = baseUrl;
    }

    public CloseableHttpClient getHttpClient() {
        return httpClient;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public Map<String, String> getCookies() {
        return cookies;
    }

    /**
     * 格式化 Cookie 字符串（用于请求头）
     */
    public String getCookieString() {
        return cookies.entrySet().stream()
                .map(e -> e.getKey() + "=" + e.getValue())
                .collect(Collectors.joining("; "));
    }

    /**
     * 检查登录状态
     */
    public void checkLogin(String responseBody) {
        if (responseBody.contains("用户登录") || responseBody.contains("身份认证")) {
            throw new NotLoginException(MessageConstant.UNLOGIN_ERROR);
        }
    }

    @Override
    public void close() {
        try {
            httpClient.close();
        } catch (IOException ignored) {
            // 静默关闭
        }
    }
}

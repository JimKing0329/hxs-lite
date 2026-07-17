package com.hxs.client;

import com.hxs.constant.MessageConstant;
import com.hxs.exception.SessionExpiredException;
import lombok.extern.slf4j.Slf4j;
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
 * 每个用户一个独立实例，支持通过 refresh() 自动续期
 */
@Slf4j
public class EduSession implements AutoCloseable {

    private static final int CONNECT_TIMEOUT = 15_000;
    private static final int SOCKET_TIMEOUT = 15_000;
    private static final String COOKIE_DOMAIN = "jwgl.hebtu.edu.cn";
    private static final String COOKIE_PATH = "/";

    private volatile CloseableHttpClient httpClient;
    private volatile Map<String, String> cookies;
    private final String baseUrl;

    /**
     * 从 cookies 构建新 Session（创建全新的 HttpClient + CookieStore）
     */
    public EduSession(Map<String, String> cookies, String baseUrl) {
        this.cookies = Collections.unmodifiableMap(new HashMap<>(cookies));
        this.baseUrl = baseUrl;
        this.httpClient = buildHttpClient(cookies);
    }

    /**
     * 从已有 HttpClient 构建 Session（用于登录后复用连接，避免重建 TCP/SSL）
     */
    EduSession(CloseableHttpClient httpClient, Map<String, String> cookies, String baseUrl) {
        this.httpClient = httpClient;
        this.cookies = Collections.unmodifiableMap(new HashMap<>(cookies));
        this.baseUrl = baseUrl;
    }

    /**
     * 刷新 Session — 关闭旧连接，替换为新的 httpClient 和 cookies
     */
    public synchronized void refresh(CloseableHttpClient newClient, Map<String, String> newCookies) {
        try {
            this.httpClient.close();
        } catch (IOException e) {
            log.warn("关闭旧 httpClient 失败: {}", e.getMessage());
        }
        this.httpClient = newClient;
        this.cookies = Collections.unmodifiableMap(new HashMap<>(newCookies));
        log.info("EduSession 已刷新");
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
     * 检查登录状态 — 过期时抛出 SessionExpiredException，由 AOP 切面捕获后自动续期
     */
    public void checkLogin(String responseBody) {
        if (responseBody.contains("用户登录") || responseBody.contains("身份认证")) {
            log.warn("教务 Session 已失效，抛出 SessionExpiredException 等待 AOP 续期");
            throw new SessionExpiredException("教务 Session 已失效");
        }
    }

    @Override
    public void close() {
        try {
            log.debug("关闭教务 HTTP 连接");
            httpClient.close();
        } catch (IOException e) {
            log.warn("关闭教务 HTTP 连接异常: {}", e.getMessage());
        }
    }

    /**
     * 根据 cookies 构建 HttpClient
     */
    private static CloseableHttpClient buildHttpClient(Map<String, String> cookies) {
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

        return HttpClients.custom()
                .setDefaultRequestConfig(config)
                .setDefaultCookieStore(cookieStore)
                .setRedirectStrategy(new LaxRedirectStrategy())
                .build();
    }
}

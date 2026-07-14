package com.hxs.client;

import com.hxs.constant.JwtClaimsConstant;
import com.hxs.constant.URLConstant;
import com.hxs.properties.JwtProperties;
import com.hxs.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.PreDestroy;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * 用户隔离的核心：每个用户一个独立的 EduSession
 * 通过 Request 作用域 + ThreadLocal 实现天然隔离
 */
@Slf4j
@Component
@Scope(value = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
@RequiredArgsConstructor
public class EduSessionManager {

    private final JwtProperties jwtProperties;
    private EduSession currentSession;

    /**
     * 获取当前请求用户的独享 Session
     * 通过 ThreadLocal 中的用户 ID 区分，天然线程隔离
     */
    public EduSession getOrCreateSession() {
        if (currentSession != null) {
            return currentSession;
        }

        // 1. 从当前请求的 Authorization 头中提取 JWT Token
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        HttpServletRequest request = attrs.getRequest();
        String token = extractToken(request);

        Claims claims = JwtUtil.parseJWT(jwtProperties.getUserSecretKey(), token);

        // 2. 为该用户创建独立的 EduSession
        Map<String, String> cookies = new HashMap<>();
        cookies.put("jw", claims.get(JwtClaimsConstant.JW).toString());
        cookies.put("JSESSIONID", claims.get(JwtClaimsConstant.JSESSION_ID).toString());

        // 3. 构建 Session（每个用户独立的 CookieStore + HttpClient）
        this.currentSession = new EduSession(cookies, URLConstant.BASE_URL);
        return this.currentSession;
    }

    private String extractToken(HttpServletRequest request) {
        String auth = request.getHeader("Authorization");
        if (auth != null && auth.startsWith("Bearer ")) {
            return auth.substring(7);
        }
        return request.getHeader(jwtProperties.getUserTokenName());
    }

    /**
     * 销毁当前 Session，释放 HTTP 连接
     */
    @PreDestroy
    public void destroy() {
        if (currentSession != null) {
            currentSession.close();
        }
    }
}

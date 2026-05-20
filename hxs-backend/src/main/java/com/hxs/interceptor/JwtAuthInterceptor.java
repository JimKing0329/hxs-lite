package com.hxs.interceptor;

import com.hxs.client.EduClient;
import com.hxs.constant.JwtClaimsConstant;
import com.hxs.context.UserContext;
import com.hxs.properties.JwtProperties;
import com.hxs.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;

/**
 * JWT 认证拦截器 — 优先 Authorization: Bearer，兼容自定义 token 头
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthInterceptor implements HandlerInterceptor {

    private final JwtProperties jwtProperties;
    private final EduClient eduClient;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!(handler instanceof HandlerMethod)) return true;

        String token = extractToken(request);
        if (token == null) {
            response.setStatus(401);
            return false;
        }

        try {
            Claims claims = JwtUtil.parseJWT(jwtProperties.getUserSecretKey(), token);
            Long userId = Long.valueOf(claims.get(JwtClaimsConstant.USER_ID).toString());
            UserContext.setCurrentId(userId);

            HashMap<String, String> cookie = new HashMap<>();
            cookie.put("jw", claims.get(JwtClaimsConstant.JW).toString());
            cookie.put("JSESSIONID", claims.get(JwtClaimsConstant.JSESSION_ID).toString());
            eduClient.setCookies(cookie);

            return true;
        } catch (Exception e) {
            log.warn("JWT 校验失败: {}", e.getMessage());
            response.setStatus(401);
            return false;
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                 Object handler, Exception ex) {
        UserContext.removeCurrentId();
    }

    private String extractToken(HttpServletRequest request) {
        String auth = request.getHeader("Authorization");
        if (auth != null && auth.startsWith("Bearer ")) return auth.substring(7);
        String token = request.getHeader(jwtProperties.getUserTokenName());
        return token;
    }

}

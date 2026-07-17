package com.hxs.client;

import com.hxs.constant.JwtClaimsConstant;
import com.hxs.constant.URLConstant;
import com.hxs.context.UserContext;
import com.hxs.mapper.UserMapper;
import com.hxs.model.entity.User;
import com.hxs.properties.JwtProperties;
import com.hxs.utils.AesUtil;
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
    private final UserMapper userMapper;
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
        String userId = claims.get(JwtClaimsConstant.USER_ID).toString();
        log.info("为用户创建教务 Session userId={}", userId);

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
     * 重新登录并刷新当前 Session — 由 AOP 切面在 Session 过期时调用
     * <p>流程：UserContext → 查 DB → 解密密码 → EduLoginClient.login() → 刷新 Session
     */
    public void relogin() {
        Long userId = UserContext.getCurrentId();
        if (userId == null) {
            throw new RuntimeException("UserContext 中无用户 ID，无法自动续期");
        }
        log.info("自动续期：重新登录 userId={}", userId);

        User user = userMapper.selectById(userId);
        if (user == null || user.getPassword() == null) {
            throw new RuntimeException("无法获取用户信息或密码为空 userId=" + userId);
        }

        try {
            String password = AesUtil.decrypt(user.getPassword());
            EduSession newSession = EduLoginClient.login(user.getSid().toString(), password);

            // 刷新当前 Session（关闭旧连接，替换为新的）
            if (currentSession != null) {
                currentSession.refresh(newSession.getHttpClient(), newSession.getCookies());
            } else {
                this.currentSession = newSession;
            }
            log.info("自动续期成功 userId={}", userId);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            log.error("自动续期失败 userId={}", userId, e);
            throw new RuntimeException("自动续期失败: " + e.getMessage(), e);
        }
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

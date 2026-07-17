package com.hxs.aspect;

import com.hxs.annotation.RetryOnSessionExpired;
import com.hxs.client.EduSessionManager;
import com.hxs.constant.MessageConstant;
import com.hxs.exception.NotLoginException;
import com.hxs.exception.SessionExpiredException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

/**
 * 教务系统请求自动续期切面
 *
 * <p>拦截所有标注 {@link RetryOnSessionExpired} 的方法：
 * <ol>
 *   <li>正常执行 → 返回结果</li>
 *   <li>抛出 {@link SessionExpiredException} → 触发重新登录 → 重试一次</li>
 *   <li>重试仍然失败 → 抛出 {@link NotLoginException}</li>
 * </ol>
 */
@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class EduSessionRetryAspect {

    private final EduSessionManager sessionManager;

    @Around("@annotation(com.hxs.annotation.RetryOnSessionExpired)")
    public Object retryOnSessionExpired(ProceedingJoinPoint joinPoint) throws Throwable {
        try {
            return joinPoint.proceed();
        } catch (SessionExpiredException e) {
            log.info("Session 过期，触发自动续期 method={}", joinPoint.getSignature().toShortString());

            // 触发重新登录并刷新 Session
            sessionManager.relogin();

            // 重试一次
            try {
                return joinPoint.proceed();
            } catch (SessionExpiredException e2) {
                log.error("自动续期后重试仍失败 method={}", joinPoint.getSignature().toShortString());
                throw new NotLoginException(MessageConstant.UNLOGIN_ERROR);
            }
        }
    }
}

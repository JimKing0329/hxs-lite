package com.hxs.exception;

/**
 * 教务 Session 过期异常 — 由 EduSession.checkLogin() 抛出，
 * 由 AOP 切面捕获后触发自动续期并重试
 */
public class SessionExpiredException extends RuntimeException {

    public SessionExpiredException(String message) {
        super(message);
    }
}

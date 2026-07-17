package com.hxs.handler;

import com.hxs.constant.MessageConstant;
import com.hxs.exception.BaseException;
import com.hxs.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.connector.ClientAbortException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.sql.SQLIntegrityConstraintViolationException;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /** 获取当前请求路径（用于日志上下文） */
    private String getRequestPath() {
        try {
            ServletRequestAttributes attrs =
                    (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
            return attrs.getRequest().getRequestURI();
        } catch (Exception e) {
            return "unknown";
        }
    }

    @ExceptionHandler(ClientAbortException.class)
    public void handleClientAbort(ClientAbortException ex) {
        log.debug("客户端已断开连接: {} path={}", ex.getMessage(), getRequestPath());
    }

    @ExceptionHandler(BaseException.class)
    public Result<?> handleBase(BaseException ex) {
        log.error("业务异常 path={} msg={}", getRequestPath(), ex.getMessage());
        return Result.error(ex.getMessage());
    }

    @ExceptionHandler(SQLIntegrityConstraintViolationException.class)
    public Result<?> handleSql(SQLIntegrityConstraintViolationException ex) {
        String msg = ex.getMessage();
        log.error("SQL 异常 path={} msg={}", getRequestPath(), msg);
        if (msg != null && msg.contains("Duplicate entry")) {
            String[] parts = msg.split(" ");
            if (parts.length > 2) return Result.error(parts[2] + MessageConstant.ALREADY_EXISTS);
        }
        return Result.error(MessageConstant.UNKNOWN_ERROR);
    }

    @ExceptionHandler(Exception.class)
    public Result<?> handleOther(Exception ex) {
        log.error("未捕获异常 path={}: ", getRequestPath(), ex);
        return Result.error(MessageConstant.UNKNOWN_ERROR);
    }

}

package com.hxs.handler;

import com.hxs.constant.MessageConstant;
import com.hxs.exception.BaseException;
import com.hxs.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.sql.SQLIntegrityConstraintViolationException;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(BaseException.class)
    public Result<?> handleBase(BaseException ex) {
        log.error("业务异常: {}", ex.getMessage());
        return Result.error(ex.getMessage());
    }

    @ExceptionHandler(SQLIntegrityConstraintViolationException.class)
    public Result<?> handleSql(SQLIntegrityConstraintViolationException ex) {
        String msg = ex.getMessage();
        log.error("SQL 异常: {}", msg);
        if (msg != null && msg.contains("Duplicate entry")) {
            String[] parts = msg.split(" ");
            if (parts.length > 2) return Result.error(parts[2] + MessageConstant.ALREADY_EXISTS);
        }
        return Result.error(MessageConstant.UNKNOWN_ERROR);
    }

    @ExceptionHandler(Exception.class)
    public Result<?> handleOther(Exception ex) {
        log.error("未捕获异常: ", ex);
        return Result.error(MessageConstant.UNKNOWN_ERROR);
    }

}

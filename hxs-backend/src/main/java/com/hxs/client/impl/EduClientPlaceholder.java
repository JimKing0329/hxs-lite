package com.hxs.client.impl;

import com.hxs.client.EduClient;
import com.hxs.model.entity.ExecuteCourseItem;
import com.hxs.model.entity.StudentInfo;
import com.hxs.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * EduClient 占位实现 — TODO: 后续迁移旧版逻辑
 */
@Slf4j
@Component
public class EduClientPlaceholder implements EduClient {

    @Override
    public Map<String, String> login(String sid, String password) {
        log.warn("EduClient.login() 尚未实现");
        return Collections.emptyMap();
    }

    @Override
    public Result<StudentInfo> getStudentInfo() {
        throw new UnsupportedOperationException("EduClient.getStudentInfo() 尚未实现");
    }

    @Override
    public String getMajorCode() {
        throw new UnsupportedOperationException("EduClient.getMajorCode() 尚未实现");
    }

    @Override
    public List<ExecuteCourseItem> getExecutePlan(String majorCode) {
        throw new UnsupportedOperationException("EduClient.getExecutePlan() 尚未实现");
    }

    @Override
    public void setCookies(Map<String, String> cookies) {
        log.debug("setCookies: {}", cookies);
    }

}

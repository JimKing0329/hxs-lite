package com.hxs.client;

import com.hxs.model.entity.ExecuteCourseItem;
import com.hxs.model.entity.StudentInfo;
import com.hxs.result.Result;

import java.util.List;
import java.util.Map;

/**
 * 教务系统 HTTP 客户端接口
 */
public interface EduClient {

    Map<String, String> login(String sid, String password);

    Result<StudentInfo> getStudentInfo();

    String getMajorCode();

    List<ExecuteCourseItem> getExecutePlan(String majorCode);

    void setCookies(Map<String, String> cookies);

}

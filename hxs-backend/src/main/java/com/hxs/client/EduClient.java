package com.hxs.client;

/**
 * 教务系统 HTTP 客户端接口 — 只对外暴露登录能力
 * 各业务模块使用独立的模块 Client（EduCourseClient / EduExamClient 等）
 *
 * @see EduCourseClient  课表
 * @see EduExamClient    成绩/考试
 * @see EduClassroomClient  空教室
 * @see EduTextbookClient   教材
 * @see EduMajorClient     专业信息/执行计划
 * @see EduStudyClient      学习情况
 * @see EduUserClient       学生信息
 */
public interface EduClient {

    /**
     * 登录教务系统并返回已认证的 EduSession
     * @param sid 学号
     * @param password 密码（明文，登录过程中 RSA 加密）
     * @return 已登录的 EduSession，可直接用于后续业务请求
     */
    EduSession login(String sid, String password);

}

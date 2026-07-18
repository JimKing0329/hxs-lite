package com.hxs.service.user;

import com.hxs.client.EduSession;
import com.hxs.model.vo.ExamInfoVO;

import java.util.List;

/**
 * 考试安排服务 — 考试信息查询与刷新
 */
public interface ExamService {

    /**
     * 获取当前用户的考试安排列表
     *
     * @param sid  学生学号
     * @param year 学年
     * @param term 学期
     * @return 考试安排列表
     */
    List<ExamInfoVO> getExamSchedule(String sid, Integer year, Integer term);

    /**
     * 刷新考试安排（从教务系统重新拉取）
     *
     * @param sid     学生学号
     * @param year    学年
     * @param term    学期
     * @param session 教务系统会话
     * @return 最新考试安排列表
     */
    List<ExamInfoVO> updateExamSchedule(String sid, Integer year, Integer term, EduSession session);
}

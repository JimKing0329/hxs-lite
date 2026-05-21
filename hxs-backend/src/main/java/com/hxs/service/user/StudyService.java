package com.hxs.service.user;

import com.hxs.model.vo.StudySituationVO;

/**
 * 学习情况服务 — 学习情况查询与刷新
 */
public interface StudyService {

    /**
     * 获取当前用户的学习情况
     *
     * @return 学习情况数据（GPA、课程完成统计等）
     */
    StudySituationVO getStudySituation();

    /**
     * 刷新学习情况（从教务系统重新拉取）
     *
     * @return 最新学习情况数据
     */
    StudySituationVO updateStudySituation();
}

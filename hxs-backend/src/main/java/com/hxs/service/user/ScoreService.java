package com.hxs.service.user;

import com.hxs.client.EduSession;
import com.hxs.model.dto.ScoreDetailQueryDTO;
import com.hxs.model.vo.*;

import java.util.List;

/**
 * 成绩服务 — 成绩查询、刷新、挂科率排行、专业排名
 */
public interface ScoreService {

    /**
     * 获取当前用户的成绩列表
     *
     * @param year 学年（可选）
     * @param term 学期（可选）
     * @return 成绩列表
     */
    List<ScoreVO> getScores(Integer year, Integer term);

    /**
     * 刷新成绩（从教务系统拉取并持久化到数据库）
     *
     * @param sid 学生学号
     * @param session 教务系统会话
     */
    void updateScores(String sid, EduSession session);

    /**
     * 获取分项成绩详情
     */
    ScoreDetailVO getScoreDetail(ScoreDetailQueryDTO queryDTO);

    /**
     * 获取挂科率排行
     *
     * @param onlyExamined 是否只看已考课程
     * @param page         页码
     * @param num          每页条数
     * @return 挂科率排行列表
     */
    List<FailRateRankVO> getFailRateRank(boolean onlyExamined, Integer page, Integer num);

    /**
     * 获取同专业排名
     *
     * @param flag true=只看本学年, false=全部
     * @return 排名信息
     */
    RankingVO getRanking(Boolean flag);
}

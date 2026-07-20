package com.hxs.service.user.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.hxs.annotation.RetryOnSessionExpired;
import com.hxs.client.EduExamClient;
import com.hxs.client.EduSession;
import com.hxs.client.EduSessionManager;
import com.hxs.component.SystemDate;
import com.hxs.context.UserContext;
import com.hxs.mapper.ScoreDetailMapper;
import com.hxs.mapper.ScoreMapper;
import com.hxs.mapper.UserMapper;
import com.hxs.model.dto.RankingDTO;
import com.hxs.model.dto.ScoreDetailQueryDTO;
import com.hxs.model.entity.Score;
import com.hxs.model.entity.ScoreDetail;
import com.hxs.model.entity.User;
import com.hxs.model.vo.*;
import com.hxs.service.user.ScoreService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 成绩服务实现 — 成绩查询、刷新、挂科率排行、专业排名
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ScoreServiceImpl implements ScoreService {

    private final EduSessionManager sessionManager;
    private final ScoreMapper scoreMapper;
    private final ScoreDetailMapper scoreDetailMapper;
    private final UserMapper userMapper;
    @Resource(name = "termStartDate")
    private final SystemDate termSystemDate;

    @Override
    public List<ScoreVO> getScores(Integer year, Integer term) {
        // 年份或学期小于 0 时，使用 DateManager 中的当前学年/学期
        if ((year != null && year < 0) || (term != null && term < 0)) {
            year = termSystemDate.getYear();
            term = termSystemDate.getTerm();
        }
        log.info("查询成绩 userId={} year={} term={}", UserContext.getCurrentId(), year, term);
        String sid = UserContext.getCurrentId().toString();

        QueryWrapper<Score> wrapper = new QueryWrapper<>();
        wrapper.eq("sid", sid);
        if (year != null) {
            wrapper.eq("year", year);
        }
        if (term != null) {
            wrapper.eq("term", term);
        }
        List<Score> list = scoreMapper.selectList(wrapper);

        // 实体 → VO
        return list.stream().map(e -> ScoreVO.builder()
                .courseName(e.getCourseName())
                .grade(e.getGrade())
                .classId(e.getClassId())
                .year(e.getYear())
                .term(e.getTerm())
                .credit(e.getCredit())
                .gradePoint(e.getGradePoint())
                .teacherName(e.getTeacherName())
                .courseType(e.getCourseType())
                .collegeName(e.getCollegeName())
                .build()
        ).collect(Collectors.toList());
    }

    @Override
    @RetryOnSessionExpired
    public void updateScores(String sid, EduSession session) {
        log.info("刷新成绩 userId={}", sid);

        EduExamClient examClient = new EduExamClient(session);

        List<Score> scores = examClient.getStudentScore();
        if (scores.isEmpty()) {
            log.info("未查询到成绩 userId={}", sid);
            return;
        }

        // 清除旧数据
        scoreMapper.deleteBySid(sid);

        // 设置 sid 并持久化
        scores.forEach(score -> {score.setSid(sid);
            score.setTerm((score.getTerm() == 3 ? 1 : score.getTerm() == 12 ? 2 : 3));});
        scoreMapper.insertBatch(scores);
        log.info("成绩表更新成功 userId={} count={}", sid, scores.size());
    }

    @Override
    @RetryOnSessionExpired
    public ScoreDetailVO getScoreDetail(ScoreDetailQueryDTO queryDTO) {
        log.info("查询成绩详情 userId={} course={}", UserContext.getCurrentId(), queryDTO.getCourseName());
        String sid = UserContext.getCurrentId().toString();

        EduSession session = sessionManager.getOrCreateSession();
        EduExamClient examClient = new EduExamClient(session);

        EduExamClient.ScoreDetailVO detail = examClient.getScoreDetail(
                queryDTO.getYear(),
                queryDTO.getTerm(),
                queryDTO.getCourseName(),
                queryDTO.getClassId()
        );

        // 持久化成绩明细到 score_detail 表
        scoreDetailMapper.deleteByCondition(sid, queryDTO.getCourseName(),
                queryDTO.getClassId(), queryDTO.getYear(), queryDTO.getTerm());

        List<ScoreDetail> entities = detail.getItems().stream().map(item -> {
            ScoreDetail entity = new ScoreDetail();
            entity.setSid(sid);
            entity.setCourseName(queryDTO.getCourseName());
            entity.setClassId(queryDTO.getClassId());
            entity.setScoreColumn(item.getScoreColumn());
            entity.setScoreRatio(item.getScoreRatio());
            entity.setScore(item.getScore());
            entity.setYear(queryDTO.getYear());
            entity.setTerm(queryDTO.getTerm());
            return entity;
        }).collect(Collectors.toList());

        if (!entities.isEmpty()) {
            scoreDetailMapper.insertBatch(entities);
            log.info("成绩明细持久化成功 userId={} course={} count={}", sid, queryDTO.getCourseName(), entities.size());
        }

        // 转换为 VO 返回
        List<ScoreDetailItemVO> items = detail.getItems().stream().map(item -> ScoreDetailItemVO.builder()
                .scoreColumn(item.getScoreColumn())
                .scoreRatio(item.getScoreRatio())
                .score(item.getScore())
                .build()
        ).collect(Collectors.toList());

        return ScoreDetailVO.builder()
                .courseName(detail.getCourseName())
                .items(items)
                .build();
    }

    @Override
    public List<FailRateRankVO> getFailRateRank(boolean onlyExamined, Integer page, Integer num) {
        log.info("查询挂科率排行 onlyExamined={} page={} num={}", onlyExamined, page, num);
        String sid = UserContext.getCurrentId().toString();

        List<FailRateRankVO> list = scoreMapper.selectFailRate(onlyExamined, sid);

        // 内存分页（挂科率排行按课程聚合，数据量小，无需 DB 层分页）
        int fromIndex = (page - 1) * num;
        if (fromIndex >= list.size()) {
            return Collections.emptyList();
        }
        int toIndex = Math.min(fromIndex + num, list.size());
        return list.subList(fromIndex, toIndex);
    }

    @Override
    public RankingVO getRanking(Boolean flag) {
        log.info("查询专业排名 userId={} flag={}", UserContext.getCurrentId(), flag);
        String sid = UserContext.getCurrentId().toString();

        // 学号前4位 = 年级
        String prefix = sid.substring(0, 4);

        // 获取学生信息以确定专业
        User user = userMapper.selectById(sid);
        if (user == null) {
            log.warn("未找到学生信息 sid={}", sid);
            RankingVO empty = new RankingVO();
            empty.setRank(0);
            empty.setTotal(0);
            empty.setItems(new ArrayList<>());
            return empty;
        }

        // 查询同年级同专业的所有成绩
        Integer year = extractYearFromSid(prefix);
        List<RankingDTO> rankingDTOs = scoreMapper.selectMajorGrade(prefix, user.getMajorName(), year, flag);

        // 计算加权平均分并排序
        List<RankingItemVO> items = rankingDTOs.stream().map(dto -> RankingItemVO.builder()
                .sid(dto.getSid())
                .name("匿名同学")
                .score(String.format("%.3f", calculateWeightedScore(dto.getScores())))
                .build()
        ).sorted((a, b) -> b.getScore().compareTo(a.getScore()))
                .collect(Collectors.toList());

        RankingVO rank = new RankingVO();
        rank.setItems(items);
        rank.setTotal(items.size());

        // 确定当前用户排名
        int rankNumber = 1;
        for (RankingItemVO item : items) {
            if (item.getSid().equals(sid)) {
                rank.setRank(rankNumber);
                break;
            }
            rankNumber++;
        }
        return rank;
    }

    // ========== 工具方法 ==========

    /**
     * 从学年名称提取年份（如 "2023-2024" → 2023）
     */
    private Integer extractYear(String yearName) {
        try {
            if (yearName != null && yearName.contains("-")) {
                return Integer.parseInt(yearName.split("-")[0]);
            }
        } catch (NumberFormatException ignored) {
        }
        return 0;
    }

    /**
     * 从学期名称提取学期号（如 "1" → 1）
     */
    private Integer extractTerm(String termName) {
        try {
            if (termName != null) {
                return Integer.parseInt(termName.trim());
            }
        } catch (NumberFormatException ignored) {
        }
        return 0;
    }

    /**
     * 从学号前缀推断当前学年
     */
    private Integer extractYearFromSid(String prefix) {
        try {
            return Integer.parseInt(prefix);
        } catch (NumberFormatException e) {
            return 2024; // 默认值
        }
    }

    /**
     * 计算加权平均分
     */
    private Double calculateWeightedScore(List<ScoreVO> scores) {
        double totalCredit = 0.0;
        for (ScoreVO score : scores) {
            totalCredit += parseDouble(score.getCredit());
        }
        double totalScore = 0.0;
        for (ScoreVO score : scores) {
            totalScore += parseDouble(score.getGrade()) * parseDouble(score.getCredit());
        }
        return totalCredit > 0 ? totalScore / totalCredit : 0.0;
    }

    private Double parseDouble(String str) {
        try {
            return Double.parseDouble(str);
        } catch (NumberFormatException e) {
            return 75.0; // 默认中等分
        }
    }
}

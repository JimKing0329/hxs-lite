package com.hxs.controller.user;

import com.hxs.client.EduSessionManager;
import com.hxs.context.UserContext;
import com.hxs.model.dto.ScoreDetailQueryDTO;
import com.hxs.model.vo.*;
import com.hxs.result.Result;
import com.hxs.service.user.ScoreService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 成绩控制器 — 成绩查询、刷新、挂科率排行、专业排名
 *
 * <p>RESTful 路径：
 * <pre>
 *   GET  /scores?year=&term=            → 查询成绩列表
 *   PUT  /scores                        → 刷新成绩
 *   GET  /scores/detail?year=&term=&courseName=&classId=  → 分项成绩详情
 *   GET  /scores/fail-rate-rank?onlyExamined=&page=&num=  → 挂科率排行
 *   GET  /scores/ranking?flag=          → 专业排名
 * </pre>
 *
 * <p>整改对照（旧 → 新）：
 * <pre>
 *   GET  /exam/getScores        →  GET  /scores
 *   PUT  /exam/updateScoreTable →  PUT  /scores
 *   POST /exam/getScoreDetail   →  GET  /scores/detail
 *   GET  /exam/getFailRateRank  →  GET  /scores/fail-rate-rank
 *   GET  /exam/ranking          →  GET  /scores/ranking
 *   GET  /exam/getExamInfo      →  GET  /exams (拆分至 ExamController)
 *   PUT  /exam/updateExamInfo   →  PUT  /exams (拆分至 ExamController)
 * </pre>
 */
@RestController
@RequestMapping("/scores")
@Slf4j
@RequiredArgsConstructor
public class ScoreController {

    private final ScoreService scoreService;
    private final EduSessionManager sessionManager;

    /** 查询成绩列表 */
    @GetMapping
    public Result<List<ScoreVO>> getScores(@RequestParam(required = false) Integer year,
                                            @RequestParam(required = false) Integer term) {
        log.info("查询成绩 userId={} year={} term={}", UserContext.getCurrentId(), year, term);
        List<ScoreVO> scores = scoreService.getScores(year, term);
        return Result.success(scores);
    }

    /** 刷新成绩（从教务系统拉取并持久化） */
    @PutMapping
    public Result<Void> updateScores() {
        log.info("刷新成绩 userId={}", UserContext.getCurrentId());
        scoreService.updateScores(UserContext.getCurrentId().toString(), sessionManager.getOrCreateSession());
        return Result.success();
    }

    /** 查询分项成绩详情 */
    @GetMapping("/detail")
    public Result<ScoreDetailVO> getScoreDetail(@RequestParam Integer year,
                                                @RequestParam Integer term,
                                                @RequestParam String courseName,
                                                @RequestParam String classId) {
        log.info("查询成绩详情 userId={} course={}", UserContext.getCurrentId(), courseName);
        ScoreDetailQueryDTO dto = ScoreDetailQueryDTO.builder()
                .year(year)
                .term(term)
                .courseName(courseName)
                .classId(classId)
                .build();
        ScoreDetailVO detail = scoreService.getScoreDetail(dto);
        return Result.success(detail);
    }

    /** 查询挂科率排行 */
    @GetMapping("/fail-rate-rank")
    public Result<List<FailRateRankVO>> getFailRateRank(@RequestParam(defaultValue = "false") boolean onlyExamined,
                                                          @RequestParam(defaultValue = "1") Integer page,
                                                          @RequestParam(defaultValue = "20") Integer num) {
        log.info("查询挂科率排行 onlyExamined={} page={} num={}", onlyExamined, page, num);
        List<FailRateRankVO> ranks = scoreService.getFailRateRank(onlyExamined, page, num);
        return Result.success(ranks);
    }

    /** 查询专业排名 */
    @GetMapping("/ranking")
    public Result<RankingVO> getRanking(@RequestParam(defaultValue = "false") Boolean flag) {
        log.info("查询专业排名 userId={} flag={}", UserContext.getCurrentId(), flag);
        RankingVO ranking = scoreService.getRanking(flag);
        return Result.success(ranking);
    }
}

package com.hxs.controller.user;

import com.hxs.context.UserContext;
import com.hxs.model.vo.ExamInfoVO;
import com.hxs.result.Result;
import com.hxs.service.user.ExamService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 考试安排控制器 — 考试信息查询与刷新
 *
 * <p>RESTful 路径：
 * <pre>
 *   GET  /exams?year=2024&term=1  → 查询考试安排列表
 *   PUT  /exams?year=2024&term=1  → 刷新考试安排
 * </pre>
 *
 * <p>整改对照（旧 → 新）：
 * <pre>
 *   GET  /exam/getExamInfo     →  GET  /exams
 *   PUT  /exam/updateExamInfo  →  PUT  /exams
 * </pre>
 */
@RestController
@RequestMapping("/exams")
@Slf4j
@RequiredArgsConstructor
public class ExamController {

    private final ExamService examService;

    /** 查询考试安排 */
    @GetMapping
    public Result<List<ExamInfoVO>> getExamSchedule(@RequestParam(defaultValue = "2024") Integer year,
                                                     @RequestParam(defaultValue = "3") Integer term) {
        log.info("查询考试安排 userId={} year={} term={}", UserContext.getCurrentId(), year, term);
        List<ExamInfoVO> exams = examService.getExamSchedule(year, term);
        return Result.success(exams);
    }

    /** 刷新考试安排 */
    @PutMapping
    public Result<List<ExamInfoVO>> updateExamSchedule(@RequestParam(defaultValue = "2024") Integer year,
                                                        @RequestParam(defaultValue = "3") Integer term) {
        log.info("刷新考试安排 userId={} year={} term={}", UserContext.getCurrentId(), year, term);
        List<ExamInfoVO> exams = examService.updateExamSchedule(year, term);
        return Result.success(exams);
    }
}

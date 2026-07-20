package com.hxs.controller.user;

import com.hxs.client.EduSessionManager;
import com.hxs.component.SystemDate;
import com.hxs.context.UserContext;
import com.hxs.model.vo.ExamInfoVO;
import com.hxs.result.Result;
import com.hxs.service.user.ExamService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;

import java.util.List;

/**
 * 考试安排控制器 — 考试信息查询与刷新
 *
 * <p>RESTful 路径：
 * <pre>
 *   GET  /exams?year=&term=  → 查询考试安排列表（不传则默认当前学年学期）
 *   PUT  /exams?year=&term=  → 刷新考试安排
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
    @Resource(name = "termStartDate")
    private SystemDate termSystemDate;
    private final EduSessionManager sessionManager;

    /** 查询考试安排 */
    @GetMapping
    public Result<List<ExamInfoVO>> getExamSchedule(@RequestParam(required = false) Integer year,
                                                     @RequestParam(required = false) Integer term) {
        year = (year != null) ? year : termSystemDate.getYear();
        term = (term != null) ? term : termSystemDate.getTerm();
        String sid = UserContext.getCurrentId().toString();
        log.info("查询考试安排 userId={} year={} term={}", sid, year, term);
        List<ExamInfoVO> exams = examService.getExamSchedule(sid, year, term);
        return Result.success(exams);
    }

    /** 刷新考试安排 */
    @PutMapping
    public Result<List<ExamInfoVO>> updateExamSchedule(@RequestParam(required = false) Integer year,
                                                        @RequestParam(required = false) Integer term) {
        year = (year != null) ? year : termSystemDate.getYear();
        term = (term != null) ? term : termSystemDate.getTerm();
        String sid = UserContext.getCurrentId().toString();
        log.info("刷新考试安排 userId={} year={} term={}", sid, year, term);
        List<ExamInfoVO> exams = examService.updateExamSchedule(sid, year, term, sessionManager.getOrCreateSession());
        return Result.success(exams);
    }
}

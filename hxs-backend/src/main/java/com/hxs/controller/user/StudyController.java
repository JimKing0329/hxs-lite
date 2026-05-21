package com.hxs.controller.user;

import com.hxs.context.UserContext;
import com.hxs.model.vo.StudySituationVO;
import com.hxs.result.Result;
import com.hxs.service.user.StudyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 学习情况控制器 — 学习情况查询与刷新
 *
 * <p>RESTful 路径：
 * <pre>
 *   GET  /study/situation  → 查询当前用户的学习情况
 *   PUT  /study/situation  → 刷新学习情况（从教务系统重新拉取）
 * </pre>
 *
 * <p>整改对照（旧 → 新）：
 * <pre>
 *   GET /study/studySituation  →  GET  /study/situation
 *   PUT /study/studySituation  →  PUT  /study/situation
 * </pre>
 */
@RestController
@RequestMapping("/study")
@Slf4j
@RequiredArgsConstructor
public class StudyController {

    private final StudyService studyService;

    /** 查询当前用户的学习情况 */
    @GetMapping("/situation")
    public Result<StudySituationVO> getStudySituation() {
        log.info("查询学习情况 userId={}", UserContext.getCurrentId());
        StudySituationVO situation = studyService.getStudySituation();
        return Result.success(situation);
    }

    /** 刷新学习情况（从教务系统重新拉取） */
    @PutMapping("/situation")
    public Result<StudySituationVO> updateStudySituation() {
        log.info("刷新学习情况 userId={}", UserContext.getCurrentId());
        StudySituationVO situation = studyService.updateStudySituation();
        return Result.success(situation);
    }
}

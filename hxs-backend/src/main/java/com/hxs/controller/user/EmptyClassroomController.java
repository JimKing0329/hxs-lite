package com.hxs.controller.user;

import com.hxs.component.DateManager;
import com.hxs.model.vo.EmptyClassroomVO;
import com.hxs.result.Result;
import com.hxs.service.user.EmptyClassroomService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * 空教室控制器 — 查询指定时段空教室
 *
 * <p>RESTful 路径：
 * <pre>
 *   GET  /classrooms?weekday=1&startSession=1&endSession=13  → 查询空教室
 * </pre>
 *
 * <p>整改对照（旧 → 新）：
 * <pre>
 *   GET  /emptyClassroom/getEmptyClassroom  →  GET  /classrooms
 * </pre>
 */
@RestController
@RequestMapping("/classrooms")
@Slf4j
@RequiredArgsConstructor
public class EmptyClassroomController {

    private final EmptyClassroomService emptyClassroomService;
    private final DateManager termDateManager;

    /** 查询空教室 */
    @GetMapping
    public Result<List<EmptyClassroomVO>> getEmptyClassroom(
            @RequestParam Integer weekday,
            @RequestParam(required = false, defaultValue = "1") Integer startSession,
            @RequestParam(required = false, defaultValue = "13") Integer endSession) {

        LocalDate termStartDate = termDateManager.getTermStartDate();
        LocalDate now = LocalDate.now();
        int week = (int) ChronoUnit.WEEKS.between(termStartDate, now) + 1;
        log.info("查询空教室 week={} weekday={} session=[{},{}]", week, weekday, startSession, endSession);

        return Result.success(
                emptyClassroomService.getEmptyClassroom(week, weekday, startSession, endSession));
    }
}

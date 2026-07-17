package com.hxs.controller.user;

import com.hxs.component.SystemDate;
import com.hxs.context.UserContext;
import com.hxs.model.vo.CourseVO;
import com.hxs.model.vo.WeekCourseVO;
import com.hxs.result.Result;
import com.hxs.service.user.CourseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * 课表控制器 — 今日/明日/周课表查询与刷新
 *
 * <p>RESTful 路径：
 * <pre>
 *   GET  /courses/today     → 今日课表
 *   GET  /courses/tomorrow  → 明日课表
 *   GET  /courses/week      → 周课表
 *   PUT  /courses           → 刷新课表
 * </pre>
 *
 * <p>整改对照（旧 → 新）：
 * <pre>
 *   GET  /course/todayCourse        →  GET  /courses/today
 *   GET  /course/tomorrowCourse     →  GET  /courses/tomorrow
 *   GET  /course/weekCourse?week=   →  GET  /courses/week?week=
 *   PUT  /course/updateCourseTable  →  PUT  /courses
 * </pre>
 */
@RestController
@RequestMapping("/courses")
@Slf4j
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;
    @Resource(name = "courseTableDate")
    private SystemDate termSystemDate;

    /** 今日课表 */
    @GetMapping("/today")
    public Result<List<CourseVO>> getTodayCourse() {
        LocalDate now = LocalDate.now();
        LocalDate termStartDate = termSystemDate.getTermStartDate();
        int weekday = now.getDayOfWeek().getValue();
        now = now.minusDays(weekday - 1);
        long weeks = ChronoUnit.WEEKS.between(termStartDate, now) + 1;
        String sid = UserContext.getCurrentId().toString();
        log.info("获取今日课程 userId={} 第{}周 星期{}", sid, weeks, weekday);
        return Result.success(courseService.getOneDayCourse(weekday, weeks, sid));
    }

    /** 明日课表 */
    @GetMapping("/tomorrow")
    public Result<List<CourseVO>> getTomorrowCourse() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        LocalDate termStartDate = termSystemDate.getTermStartDate();
        int weekday = tomorrow.getDayOfWeek().getValue();
        tomorrow = tomorrow.minusDays(weekday - 1);
        long weeks = ChronoUnit.WEEKS.between(termStartDate, tomorrow) + 1;
        String sid = UserContext.getCurrentId().toString();
        log.info("获取明日课程 userId={} 第{}周 星期{}", sid, weeks, weekday);
        return Result.success(courseService.getOneDayCourse(weekday, weeks, sid));
    }

    /** 周课表 */
    @GetMapping("/week")
    public Result<WeekCourseVO> getWeekCourse(@RequestParam("week") long week) {
        LocalDate now = LocalDate.now();
        LocalDate termStartDate = termSystemDate.getTermStartDate();
        long currentWeek = ChronoUnit.WEEKS.between(termStartDate, now) + 1;
        week = week > 0 ? week : currentWeek;
        if (week <= 0) {
            week = 1;
        }
        log.info("获取周课程 userId={} 第{}周", UserContext.getCurrentId(), week);
        return Result.success(courseService.getWeekCourse(week));
    }

    /** 刷新课表 */
    @PutMapping
    public Result<?> updateCourseTable() {
        log.info("更新课程表 userId={}", UserContext.getCurrentId());
        courseService.updateCourseTable();
        return Result.success();
    }
}

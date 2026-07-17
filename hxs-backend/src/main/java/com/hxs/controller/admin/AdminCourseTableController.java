package com.hxs.controller.admin;

import com.hxs.result.Result;
import com.hxs.service.newcoursetable.NewCourseTableService;
import com.hxs.task.NewCourseTableTask;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@Slf4j
public class AdminCourseTableController {

    private final NewCourseTableService newCourseTableService;
    private final NewCourseTableTask newCourseTableTask;

    @PutMapping("/classes")
    public Result<Void> updateClasses() {
        log.info("管理员更新班级信息");
        newCourseTableService.updateClass();
        return Result.success();
    }

    @PutMapping("/course-table")
    public Result<Void> updateCourseTable() {
        log.info("管理员更新所有课程表");
        newCourseTableService.updateCourseTable();
        return Result.success();
    }

    /**
     * 获取课表定时任务启用状态
     */
    @GetMapping("/course-table-task/enabled")
    public Result<Boolean> getCourseTableTaskEnabled() {
        log.info("管理员查询课表定时任务启用状态");
        return Result.success(NewCourseTableTask.enabled);
    }

    /**
     * 更新课表定时任务启用状态
     */
    @PutMapping("/course-table-task/enabled")
    public Result<Void> updateCourseTableTaskEnabled(@RequestParam Boolean enabled) {
        log.info("管理员更新课表定时任务启用状态：enabled={}", enabled);
        newCourseTableTask.updateEnabled(enabled);
        return Result.success();
    }
}

package com.hxs.controller.admin;

import com.hxs.result.Result;
import com.hxs.service.newcoursetable.NewCourseTableService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@Slf4j
public class AdminCourseTableController {

    private final NewCourseTableService newCourseTableService;

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
}

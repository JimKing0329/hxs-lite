
package com.hxs.controller.newcoursetable;


import java.util.List;

import com.hxs.model.vo.ClassVO;
import com.hxs.model.vo.CourseTableVO;
import com.hxs.result.Result;
import com.hxs.service.newcoursetable.NewCourseTableService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/newCourseTable"})
public class CourseTableController {
    private static final Logger log = LoggerFactory.getLogger(CourseTableController.class);
    private Integer clickCount = 0;
    private final NewCourseTableService newCourseTableService;


    @GetMapping({"/allClass"})
    public Result<List<ClassVO>> getAllClass() {
        log.info("获取所有班级信息");
        return Result.success(this.newCourseTableService.getAllClass());
    }

    @GetMapping({"/courseTable"})
    public Result<CourseTableVO> getCourseTableByClassId(@RequestParam String className, @RequestParam String classId) {
        log.info("获取课程表  {}", className);
        log.info("查询次数 {}", this.clickCount = this.clickCount + 1);
        return Result.success(this.newCourseTableService.getCourseTableByClassId(classId));
    }

    public CourseTableController(NewCourseTableService newCourseTableService) {
        this.newCourseTableService = newCourseTableService;
    }
}

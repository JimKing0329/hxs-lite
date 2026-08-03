package com.hxs.controller.newcoursetable;

import java.util.List;

import com.hxs.model.vo.ClassVO;
import com.hxs.model.vo.CourseTableVO;
import com.hxs.result.Result;
import com.hxs.service.newcoursetable.NewCourseTableService;
import com.hxs.utils.ArticleFactory;
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
    private final NewCourseTableService newCourseTableService;
    private final ArticleFactory articleFactory;

    @GetMapping({"/allClass"})
    public Result<List<ClassVO>> getAllClass() {
        log.info("获取所有班级信息");
        return Result.success(this.newCourseTableService.getAllClass());
    }

    @GetMapping({"/courseTable"})
    public Result<CourseTableVO> getCourseTableByClassId(@RequestParam String className, @RequestParam String classId) {
        log.info("获取课程表  {}", className);
        // 持久化课表查询点击次数
        articleFactory.incrementClickCount("course_table_click_count");
        return Result.success(this.newCourseTableService.getCourseTableByClassId(classId));
    }

    public CourseTableController(NewCourseTableService newCourseTableService, ArticleFactory articleFactory) {
        this.newCourseTableService = newCourseTableService;
        this.articleFactory = articleFactory;
    }
}

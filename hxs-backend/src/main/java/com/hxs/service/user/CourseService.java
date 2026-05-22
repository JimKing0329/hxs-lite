package com.hxs.service.user;

import com.hxs.model.vo.CourseVO;
import com.hxs.model.vo.WeekCourseVO;

import java.util.List;

public interface CourseService {

    /**
     * 更新课程表（从教务系统爬取并持久化）
     */
    void updateCourseTable();

    /**
     * 查询某天课程
     */
    List<CourseVO> getOneDayCourse(int weekday, long weeks, String sid);

    /**
     * 查询整周课程
     */
    WeekCourseVO getWeekCourse(long week);
}

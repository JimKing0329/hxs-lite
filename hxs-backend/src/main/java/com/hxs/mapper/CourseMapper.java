package com.hxs.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hxs.model.entity.Course;
import com.hxs.model.vo.CourseVO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CourseMapper extends BaseMapper<Course> {

    List<CourseVO> queryTodayCourse(@Param("sid") String sid,
                                     @Param("weekday") int weekday,
                                     @Param("weeks") long weeks);

    List<CourseVO> queryWeekCourse(@Param("sid") String sid,
                                    @Param("week") long week);

    @Insert("<script>" +
            "INSERT INTO course(course_id, title, teacher, class_name, credit, weekday, " +
            "start_session, end_session, week_number, campus, place, evaluation_mode, " +
            "weekly_hours, total_hours, sid) VALUES " +
            "<foreach collection='list' item='item' separator=','>" +
            "(#{item.courseId}, #{item.title}, #{item.teacher}, #{item.className}, " +
            "#{item.credit}, #{item.weekday}, #{item.startSession}, #{item.endSession}, " +
            "#{item.weekNumber}, #{item.campus}, #{item.place}, #{item.evaluationMode}, " +
            "#{item.weeklyHours}, #{item.totalHours}, #{item.sid})" +
            "</foreach>" +
            "</script>")
    void insertBatch(@Param("list") List<Course> list);
}

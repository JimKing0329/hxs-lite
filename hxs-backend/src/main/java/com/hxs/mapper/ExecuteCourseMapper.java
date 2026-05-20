package com.hxs.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hxs.model.entity.ExecuteCourseItem;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ExecuteCourseMapper extends BaseMapper<ExecuteCourseItem> {

    @Insert("<script>" +
            "INSERT INTO execute_course (major_code, course_name, course_point, course_week, college_name, course_time, recommend_term, course_type) VALUES " +
            "<foreach collection='list' item='item' separator=','>" +
            "(#{item.majorCode}, #{item.courseName}, #{item.coursePoint}, #{item.courseWeek}, #{item.collegeName}, #{item.courseTime}, #{item.recommendTerm}, #{item.courseType})" +
            "</foreach>" +
            "</script>")
    void insertBatch(@Param("list") List<ExecuteCourseItem> list);

}

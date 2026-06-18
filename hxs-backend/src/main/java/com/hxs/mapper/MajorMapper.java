package com.hxs.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hxs.model.entity.MajorInfo;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MajorMapper extends BaseMapper<MajorInfo> {

    @Insert("<script>" +
            "INSERT INTO major_info(grade_id, grade, major_id, major_name, major_direction, " +
            "plan_id, college_id, major_code) VALUES " +
            "<foreach collection='list' item='item' separator=','>" +
            "(#{item.gradeId}, #{item.grade}, #{item.majorId}, #{item.majorName}, " +
            "#{item.majorDirection}, #{item.planId}, #{item.collegeId}, #{item.majorCode})" +
            "</foreach>" +
            "</script>")
    void insertBatch(@Param("list") List<MajorInfo> list);
}

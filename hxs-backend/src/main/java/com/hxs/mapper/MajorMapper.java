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
            "INSERT IGNORE INTO major_info(grade, major_id, major_name, college_id, major_code) VALUES " +
            "<foreach collection='list' item='item' separator=','>" +
            "(#{item.grade}, #{item.majorId}, #{item.majorName}, #{item.collegeId}, #{item.majorCode})" +
            "</foreach>" +
            "</script>")
    void insertBatch(@Param("list") List<MajorInfo> list);
}

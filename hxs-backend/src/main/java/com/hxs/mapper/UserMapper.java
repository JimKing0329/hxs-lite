package com.hxs.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hxs.model.entity.StudentInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserMapper extends BaseMapper<StudentInfo> {

    @Select("SELECT major_code FROM student_info WHERE sid = #{sid}")
    String queryMajorCodeByMajorId(String sid);

}

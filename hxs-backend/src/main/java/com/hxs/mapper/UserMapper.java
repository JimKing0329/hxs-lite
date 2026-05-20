package com.hxs.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hxs.model.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserMapper extends BaseMapper<User> {

    @Select("select m.major_code from student_info s\n" +
            "        join major_info m on s.major_code = m.major_id\n" +
            "        where s.sid = #{sid}\n" +
            "        and m.grade = substr(s.sid, 1, 4)")
    String queryMajorCodeByMajorId(String sid);

}

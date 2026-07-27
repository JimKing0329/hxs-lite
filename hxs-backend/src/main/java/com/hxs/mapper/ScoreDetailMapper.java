package com.hxs.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hxs.model.entity.ScoreDetail;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 成绩明细 Mapper — score_detail 表数据访问
 */
@Mapper
public interface ScoreDetailMapper extends BaseMapper<ScoreDetail> {

    /**
     * 查询指定学生在指定课程的成绩明细
     */
    @Select("SELECT * FROM score_detail WHERE sid = #{sid} AND course_name = #{courseName} " +
            "AND class_id = #{classId} AND year = #{year} AND term = #{term}")
    List<ScoreDetail> selectByCondition(@Param("sid") String sid,
                                        @Param("courseName") String courseName,
                                        @Param("classId") String classId,
                                        @Param("year") Integer year,
                                        @Param("term") Integer term);

    /**
     * 批量插入成绩明细
     */
    @Insert("<script>" +
            "INSERT INTO score_detail(sid, course_name, class_id, grade_column, " +
            "grade_ratio, grade, year, term) VALUES " +
            "<foreach collection='list' item='item' separator=','>" +
            "(#{item.sid}, #{item.courseName}, #{item.classId}, #{item.scoreColumn}, " +
            "#{item.scoreRatio}, #{item.score}, #{item.year}, #{item.term})" +
            "</foreach>" +
            "</script>")
    void insertBatch(@Param("list") List<ScoreDetail> list);

    /**
     * 删除指定学生在指定课程的成绩明细
     */
    @Delete("DELETE FROM score_detail WHERE sid = #{sid} AND course_name = #{courseName} " +
            "AND class_id = #{classId} AND year = #{year} AND term = #{term}")
    void deleteByCondition(@Param("sid") String sid,
                           @Param("courseName") String courseName,
                           @Param("classId") String classId,
                           @Param("year") Integer year,
                           @Param("term") Integer term);
}

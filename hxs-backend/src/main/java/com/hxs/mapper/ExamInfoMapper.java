package com.hxs.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hxs.model.entity.ExamInfo;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 考试安排 Mapper — exam_info 表数据访问
 */
@Mapper
public interface ExamInfoMapper extends BaseMapper<ExamInfo> {

    /**
     * 批量插入考试安排
     */
    @Insert("<script>" +
            "INSERT INTO exam_info(sid, year, term, course_id, title, time, location, campus, " +
            "seat, retake, exam_name, teacher, class_name, college, credit, exam_method, paper_id, remark) VALUES " +
            "<foreach collection='list' item='item' separator=','>" +
            "(#{item.sid}, #{item.year}, #{item.term}, #{item.courseId}, #{item.title}, " +
            "#{item.time}, #{item.location}, #{item.campus}, #{item.seat}, #{item.retake}, " +
            "#{item.examName}, #{item.teacher}, #{item.className}, #{item.college}, " +
            "#{item.credit}, #{item.examMethod}, #{item.paperId}, #{item.remark})" +
            "</foreach>" +
            "</script>")
    void insertBatch(@Param("list") List<ExamInfo> list);

    /**
     * 删除指定学生的考试安排
     */
    @Delete("DELETE FROM exam_info WHERE sid = #{sid}")
    void deleteBySid(@Param("sid") String sid);

    /**
     * 删除指定学生指定学年学期的考试安排
     */
    @Delete("DELETE FROM exam_info WHERE sid = #{sid} AND year = #{year} AND term = #{term}")
    void deleteBySidAndYearTerm(@Param("sid") String sid,
                                @Param("year") Integer year,
                                @Param("term") Integer term);
}

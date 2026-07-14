package com.hxs.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hxs.model.dto.RankingDTO;
import com.hxs.model.entity.Score;
import com.hxs.model.vo.FailRateRankVO;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 成绩 Mapper — score 表数据访问
 * <p>简单操作用注解实现，复杂聚合/JOIN 查询保留 XML</p>
 */
@Mapper
public interface ScoreMapper extends BaseMapper<Score> {

    /**
     * 批量插入成绩（使用 @Insert 注解，无需 XML）
     */
    @Insert("<script>" +
            "INSERT INTO score(sid, grade, grade_point, category_name, college_name, " +
            "teacher_name, class_id, major, course_name, year, term, credit, course_type) VALUES " +
            "<foreach collection='list' item='item' separator=','>" +
            "(#{item.sid}, #{item.grade}, #{item.gradePoint}, #{item.categoryName}, " +
            "#{item.collegeName}, #{item.teacherName}, #{item.classId}, #{item.major}, " +
            "#{item.courseName}, #{item.year}, #{item.term}, #{item.credit}, #{item.courseType})" +
            "</foreach>" +
            "</script>")
    void insertBatch(@Param("list") List<Score> list);

    /**
     * 挂科率排行统计（聚合查询，需 XML）
     *
     * @param onlyExamined 是否只看已考课程
     * @param sid          当前用户学号
     */
    List<FailRateRankVO> selectFailRate(@Param("onlyExamined") boolean onlyExamined,
                                        @Param("sid") String sid);

    /**
     * 同年级同专业成绩聚合查询（JOIN 查询，需 XML）
     *
     * @param sidPrefix 学号前4位（年级）
     * @param majorName 专业名称
     * @param year      学年
     * @param flag      是否只查本学年
     */
    List<RankingDTO> selectMajorGrade(@Param("sidPrefix") String sidPrefix,
                                      @Param("majorName") String majorName,
                                      @Param("year") Integer year,
                                      @Param("flag") Boolean flag);

    /**
     * 删除指定学生的成绩
     */
    @Delete("DELETE FROM score WHERE sid = #{sid}")
    void deleteBySid(@Param("sid") String sid);
}

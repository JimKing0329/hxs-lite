package com.hxs.model.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 成绩实体 — 映射 score 表
 *
 * <p>字段与 score 表严格一一对应</p>
 */
@Data
@TableName("score")
public class Score implements Serializable {

    @TableId
    private Long id;

    /** 学生ID */
    private String sid;

    /** 成绩等级（如 A/B/C） */
    private String grade;

    /** 绩点（如 3.5/4.0） */
    private String gradePoint;

    /** 课程类别名称 */
    private String categoryName;

    /** 学院名称 */
    private String collegeName;

    /** 教师姓名 */
    private String teacherName;

    /** 班级ID */
    private String classId;

    /** 专业名称 */
    private String major;

    /** 创建时间 */
    private LocalDateTime createdTime;

    /** 课程名 */
    private String courseName;

    /** 学年 */
    private Integer year;

    /** 学期 */
    private Integer term;

    /** 学分 */
    private String credit;

    /** 课程性质 */
    private String courseType;
}

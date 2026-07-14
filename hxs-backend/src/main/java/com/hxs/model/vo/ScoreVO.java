package com.hxs.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 成绩 VO — 成绩列表查询响应
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ScoreVO implements Serializable {

    /** 课程名称 */
    private String courseName;

    /** 成绩 */
    private String grade;

    /** 教学班编号 */
    private String classId;

    /** 学年 */
    private Integer year;

    /** 学期 */
    private Integer term;

    /** 学分 */
    private String credit;

    /** 绩点 */
    private String gradePoint;

    /** 教师姓名（老版本字段） */
    private String teacherName;

    /** 课程性质名称（老版本字段） */
    private String courseType;

    /** 学年名称（如 "2023-2024"） */
    private String yearName;

    /** 学期名称（如 "1"） */
    private String termName;

    /** 考试类型 */
    private String examType;

    /** 重修标记 */
    private String retakeMark;

    /** 开课学院 */
    private String collegeName;
}

package com.hxs.model.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 成绩明细实体 — 映射 score_detail 表
 *
 * <p>存储每门课程的分项成绩（平时成绩、期末成绩等），字段与 score_detail 表严格一一对应</p>
 */
@Data
@TableName("score_detail")
public class ScoreDetail implements Serializable {

    @TableId
    private Long id;

    /** 学号 */
    private String sid;

    /** 课程名称 */
    private String courseName;

    /** 班级ID */
    private String classId;

    /** 成绩列名（如"平时成绩(30%)"） */
    @TableField("grade_column")
    private String scoreColumn;

    /** 成绩占比（如"30%"） */
    @TableField("grade_ratio")
    private String scoreRatio;

    /** 成绩分数 */
    @TableField("grade")
    private String score;

    /** 学年 */
    private Integer year;

    /** 学期 */
    private Integer term;
}

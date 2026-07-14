package com.hxs.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 课程实体 — 映射 course 表
 */
@Data
@TableName("course")
public class Course {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private String courseId;

    private String title;

    private String teacher;

    private String className;

    private Float credit;

    private Integer weekday;

    private Integer startSession;

    private Integer endSession;

    private Integer weekNumber;

    private String campus;

    private String place;

    private String evaluationMode;

    private Integer weeklyHours;

    private Integer totalHours;

    private String sid;
}

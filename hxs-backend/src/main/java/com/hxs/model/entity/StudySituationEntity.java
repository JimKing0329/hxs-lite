package com.hxs.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 学生学习情况记录表实体 — 映射 study_situation 表
 */
@Data
@TableName("study_situation")
public class StudySituationEntity implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 学生ID */
    private Long sid;

    /** 平均学分绩点（GPA）描述 */
    private String gpa;

    /** 计划总课程描述 */
    private String planCourse;

    /** 计划内通过课程描述 */
    private String passPlanCourse;

    /** 计划内未通过课程描述 */
    private String failPlanCourse;

    /** 计划内未修课程描述 */
    private String unstudyPlanCourse;

    /** 计划内在读课程描述 */
    private String studyingPlanCourse;

    /** 计划外通过课程描述 */
    private String outPlanPassCourse;

    /** 计划外未通过课程描述 */
    private String outPlanFailCourse;
}

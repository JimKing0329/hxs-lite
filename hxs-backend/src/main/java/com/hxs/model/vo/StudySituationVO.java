package com.hxs.model.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 学习情况 VO — 学习情况查询响应
 *
 * <p>包含 GPA、计划课程完成情况等学习统计信息</p>
 */
@Data
public class StudySituationVO implements Serializable {

    /** 平均绩点 (GPA) */
    private String gpa;

    /** 计划内课程总数 */
    private String planCourse;

    /** 计划内已通过课程数 */
    private String passPlanCourse;

    /** 计划内未通过课程数 */
    private String failPlanCourse;

    /** 计划内未修课程数 */
    private String unstudyPlanCourse;

    /** 计划内修读中课程数 */
    private String studyingPlanCourse;

    /** 计划外已通过课程数 */
    private String outPlanPassCourse;

    /** 计划外未通过课程数 */
    private String outPlanFailCourse;
}

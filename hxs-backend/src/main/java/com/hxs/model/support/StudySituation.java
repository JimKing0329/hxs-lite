package com.hxs.model.support;

import lombok.Data;

@Data
public class StudySituation {
    private String gpa;
    private String planCourse;
    private String passPlanCourse;
    private String failPlanCourse;
    private String unstudyPlanCourse;
    private String studyingPlanCourse;
    private String outPlanPassCourse;
    private String outPlanFailCourse;
}

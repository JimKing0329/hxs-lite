package com.hxs.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 考试安排 VO — 考试信息查询响应
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ExamInfoVO implements Serializable {

    /** 课程名称 */
    private String courseName;

    /** 考试时间 */
    private String examTime;

    /** 考试地点 */
    private String examPlace;

    /** 考试形式 */
    private String examForm;

    /** 座位号 */
    private String seatNo;
}

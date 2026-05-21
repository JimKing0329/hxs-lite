package com.hxs.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 成绩详情查询 DTO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ScoreDetailQueryDTO implements Serializable {

    /** 课程名称 */
    private String courseName;

    /** 教学班编号 */
    private String classId;

    /** 学年 */
    private Integer year;

    /** 学期 */
    private Integer term;
}

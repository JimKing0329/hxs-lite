package com.hxs.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 成绩详情 VO — 分项成绩查询响应
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ScoreDetailVO implements Serializable {

    /** 课程名称 */
    private String courseName;

    /** 分项成绩列表 */
    private List<ScoreDetailItemVO> items;
}

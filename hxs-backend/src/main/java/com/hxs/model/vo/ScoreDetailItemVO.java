package com.hxs.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 分项成绩条目 VO — 返回字段与原代码严格一致
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ScoreDetailItemVO implements Serializable {

    /** 成绩列名（如"平时成绩(30%)"） */
    private String scoreColumn;

    /** 占比（如"30%"） */
    private String scoreRatio;

    /** 得分 */
    private String score;
}

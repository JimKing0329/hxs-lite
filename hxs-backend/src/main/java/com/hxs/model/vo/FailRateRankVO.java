package com.hxs.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 挂科率排名 VO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FailRateRankVO implements Serializable {

    /** 课程名称 */
    private String courseName;

    /** 总人数 */
    private Integer allCount;

    /** 挂科人数 */
    private Integer failCount;

    /** 挂科率 */
    private Double failRate;
}

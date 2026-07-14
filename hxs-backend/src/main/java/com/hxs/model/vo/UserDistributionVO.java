package com.hxs.model.vo;

import lombok.Data;

/**
 * 用户学院分布 VO
 */
@Data
public class UserDistributionVO {
    /** 学院名称 */
    private String college;
    /** 人数 */
    private Integer count;
}

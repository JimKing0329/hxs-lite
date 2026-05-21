package com.hxs.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 排名 VO — 专业排名查询响应
 */
@Data
public class RankingVO implements Serializable {

    /** 当前用户的排名 */
    private Integer rank;

    /** 总参与人数 */
    private Integer total;

    /** 排名列表 */
    private List<RankingItemVO> items;
}

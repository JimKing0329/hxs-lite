package com.hxs.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 排名条目 VO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RankingItemVO implements Serializable {

    /** 学号 */
    private String sid;

    /** 姓名（脱敏） */
    private String name;

    /** 加权平均分 */
    private String score;
}

package com.hxs.model.dto;

import com.hxs.model.vo.ScoreVO;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 排名查询 DTO — MyBatis 聚合结果映射
 */
@Data
public class RankingDTO implements Serializable {

    /** 学号 */
    private String sid;

    /** 姓名 */
    private String name;

    /** 成绩列表 */
    private List<ScoreVO> scores;
}

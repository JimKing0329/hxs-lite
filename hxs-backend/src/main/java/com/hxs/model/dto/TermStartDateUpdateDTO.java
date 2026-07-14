package com.hxs.model.dto;

import lombok.Builder;
import lombok.Data;

/**
 * 学期开始日期更新请求 DTO
 */
@Data
@Builder
public class TermStartDateUpdateDTO {
    private Long id;
    private String date;
    private String remark;
    private Integer year;
    private Integer term;
}

package com.hxs.component;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;

/**
 * 学期日期管理组件
 * 由 {@link com.hxs.config.DateFactory} 在应用启动时初始化
 */
@Data
@Slf4j
public class SystemDate {
    /** 学年 */
    private Integer year;
    /** 学期 */
    private Integer term;
    /** 学期开始日期，用于计算当前教学周 */
    private LocalDate termStartDate;
}

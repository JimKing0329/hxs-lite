package com.hxs.model.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * 系统日期配置表实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("system_dates")
public class SystemDate implements Serializable {

    @TableId
    private Integer id;

    /** 学期开始日期 */
    private LocalDate termStartDate;

    /** 日期备注 */
    private String remark;

    /** 学年 */
    private Integer year;

    /** 学期 */
    private Integer term;
}

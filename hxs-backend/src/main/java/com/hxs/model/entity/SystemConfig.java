package com.hxs.model.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 系统配置表实体 — 映射 system_config 表（key-value 结构）
 */
@Data
@TableName("system_config")
public class SystemConfig implements Serializable {

    /** 配置键（主键） */
    @TableId
    private String configKey;

    /** 配置值 */
    private String configValue;

    /** 备注 */
    private String remark;

    /** 更新时间 */
    private LocalDateTime updatedAt;
}

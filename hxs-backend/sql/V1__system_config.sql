-- ============================================
-- 系统配置表 — 用于存储微信公众号 mediaId 等动态配置
-- ============================================

CREATE TABLE IF NOT EXISTS system_config (
    config_key   VARCHAR(64)  NOT NULL PRIMARY KEY COMMENT '配置键',
    config_value VARCHAR(256) NOT NULL COMMENT '配置值',
    remark       VARCHAR(128) DEFAULT NULL COMMENT '备注',
    updated_at   DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统配置表';

-- 插入校历 mediaId 初始值（从 WechatServiceImpl 硬编码迁移而来）
INSERT INTO system_config (config_key, config_value, remark)
VALUES ('calender_media_id', 'POAR6TA2yHMntGxMC02clJgglwFlDdLNjhZGvVcKBKfUcE6N1ePQNkIep5IutlVS', '微信公众号校历图片 mediaId')
ON DUPLICATE KEY UPDATE config_value = config_value;

-- 校区地图 mediaId
INSERT INTO system_config (config_key, config_value, remark)
VALUES ('school_map_yh_media_id', '1eadfyaifja', '微信公众号裕华校区地图 mediaId')
ON DUPLICATE KEY UPDATE config_value = config_value;

INSERT INTO system_config (config_key, config_value, remark)
VALUES ('school_map_hq_media_id', '145whwuwhdah8w', '微信公众号红旗校区地图 mediaId')
ON DUPLICATE KEY UPDATE config_value = config_value;

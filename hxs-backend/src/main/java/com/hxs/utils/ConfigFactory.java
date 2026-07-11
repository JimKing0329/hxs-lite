package com.hxs.utils;

import com.hxs.mapper.SystemConfigMapper;
import com.hxs.model.entity.SystemConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 系统配置工厂 — 启动时加载 system_config 表到内存，提供快速读取
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ConfigFactory {

    private final SystemConfigMapper systemConfigMapper;

    private final Map<String, String> configMap = new HashMap<>();

    @PostConstruct
    public void init() {
        refresh();
    }

    /**
     * 从数据库重新加载所有配置到内存
     */
    public void refresh() {
        configMap.clear();
        List<SystemConfig> configs = systemConfigMapper.selectList(null);
        for (SystemConfig config : configs) {
            configMap.put(config.getConfigKey(), config.getConfigValue());
        }
        log.info("ConfigFactory 刷新完成, 配置项数: {}", configMap.size());
    }

    /**
     * 获取配置值
     * @param key 配置键
     * @return 配置值，不存在时返回 null
     */
    public String get(String key) {
        return configMap.get(key);
    }
}

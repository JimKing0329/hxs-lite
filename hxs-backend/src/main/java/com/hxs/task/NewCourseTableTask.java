package com.hxs.task;

import com.hxs.mapper.SystemConfigMapper;
import com.hxs.model.entity.SystemConfig;
import com.hxs.service.newcoursetable.NewCourseTableService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.scheduling.annotation.Scheduled;

import javax.annotation.PostConstruct;

/**
 * @author xin
 * @date 2026/7/17 10:09
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class NewCourseTableTask {
    private final NewCourseTableService newCourseTableService;
    private final SystemConfigMapper systemConfigMapper;

    /** 定时任务开关，key: new_course_table_task_enabled */
    private static final String CONFIG_KEY = "new_course_table_task_enabled";
    public static boolean enabled = false;

    /**
     * 应用启动时从 system_config 表读取启用状态
     */
    @PostConstruct
    public void init() {
        SystemConfig config = systemConfigMapper.selectById(CONFIG_KEY);
        enabled = config != null && "true".equalsIgnoreCase(config.getConfigValue());
        log.info("课表定时任务初始化完成，enabled={}", enabled);
    }

    /**
     * 更新启用状态（同时更新内存和数据库）
     * @param enabled 是否启用
     */
    public void updateEnabled(boolean enabled) {
        NewCourseTableTask.enabled = enabled;
        String value = enabled ? "true" : "false";

        SystemConfig config = systemConfigMapper.selectById(CONFIG_KEY);
        if (config != null) {
            config.setConfigValue(value);
            systemConfigMapper.updateById(config);
        } else {
            config = new SystemConfig();
            config.setConfigKey(CONFIG_KEY);
            config.setConfigValue(value);
            config.setRemark("课表定时任务开关");
            systemConfigMapper.insert(config);
        }
        log.info("课表定时任务状态更新为：enabled={}", enabled);
    }
    /**
     * 定时更新课表，每小时一次
     */
    @Scheduled(cron = "0 0 * * * ?")
    public void updateCourseTable() {
        if (!enabled) {
            return;
        }
        long start = System.currentTimeMillis();
        log.info("定时任务开始：更新课表信息");
        newCourseTableService.updateClass();
        newCourseTableService.updateCourseTable();
        log.info("定时任务完成，耗时 {} ms", System.currentTimeMillis() - start);
    }
}

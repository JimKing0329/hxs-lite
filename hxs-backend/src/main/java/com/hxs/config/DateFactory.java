package com.hxs.config;

import com.hxs.component.DateManager;
import com.hxs.mapper.SystemDateMapper;
import com.hxs.model.entity.SystemDate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * 学期日期工厂
 * 应用启动时从 system_dates 表读取当前学期配置，初始化 TermDateManager
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class DateFactory {

    private final SystemDateMapper systemDateMapper;

    /**
     * 从 system_dates 表 id=1 读取学期信息，
     * 用于计算当前教学周等场景
     */
    @Primary
    @Bean("termStartDate")
    public DateManager termDateManager() {
        SystemDate systemDate = systemDateMapper.selectById(1);
        DateManager manager = new DateManager();
        if (systemDate != null) {
            BeanUtils.copyProperties(systemDate, manager);
            log.info("学期日期初始化成功: year={}, term={}, termStartDate={}",
                    manager.getYear(), manager.getTerm(), manager.getTermStartDate());
        } else {
            log.warn("system_dates 表 id=1 记录为空，学期日期未初始化");
        }
        return manager;
    }

    @Bean("courseTableDate")
    public DateManager courseDate() {
        SystemDate systemDate = systemDateMapper.selectById(2);
        DateManager manager = new DateManager();
        if (systemDate != null) {
            BeanUtils.copyProperties(systemDate, manager);
            log.info("课程表日期: year={}, term={}, termStartDate={}",
                    manager.getYear(), manager.getTerm(), manager.getTermStartDate());
        } else {
            log.warn("system_dates 表 id=2 记录为空，学期日期未初始化");
        }
        return manager;
    }
}

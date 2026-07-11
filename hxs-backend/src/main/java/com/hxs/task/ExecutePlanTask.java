package com.hxs.task;

import com.hxs.mapper.ExecuteCourseMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ExecutePlanTask {

    private final ExecuteCourseMapper executeCourseMapper;
    /**
     * 清理执行计划历史数据,保证获取最新数据
     */
    @Scheduled(cron = "0 0 0 */15 * ?")
    private void cleanHistoryData(){
        log.info("开始清理执行计划历史数据...");
        executeCourseMapper.delete(null);
        log.info("清理执行计划历史数据完成 !!!");
    }
}

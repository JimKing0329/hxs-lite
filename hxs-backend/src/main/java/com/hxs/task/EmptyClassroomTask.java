package com.hxs.task;

import com.hxs.component.DateManager;
import com.hxs.service.user.EmptyClassroomService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * 空教室定时更新任务 — 每周日凌晨 3 点自动拉取新一周的空教室数据
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class EmptyClassroomTask {

    private final EmptyClassroomService emptyClassroomService;
    private final DateManager dateManager;

    /**
     * 每周日 03:00 执行，更新空教室信息并清理历史数据
     */
    @Scheduled(cron = "0 0 3 * * 0")
    public void updateEmptyClassroom() {
        long start = System.currentTimeMillis();
        log.info("定时任务开始：更新空教室信息");

        LocalDate now = LocalDate.now();
        LocalDate termStartDate = dateManager.getTermStartDate();
        int week = (int) ChronoUnit.WEEKS.between(termStartDate, now) + 2;

        emptyClassroomService.updateEmptyClassRoom(week);
        emptyClassroomService.deleteHistoryRecord();

        log.info("定时任务完成，耗时 {} ms", System.currentTimeMillis() - start);
    }
}

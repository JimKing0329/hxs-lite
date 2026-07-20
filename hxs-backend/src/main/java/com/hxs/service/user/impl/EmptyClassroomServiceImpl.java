package com.hxs.service.user.impl;

import com.hxs.annotation.RetryOnSessionExpired;
import com.hxs.client.EduClassroomClient;
import com.hxs.client.EduLoginClient;
import com.hxs.client.EduSession;
import com.hxs.component.SystemDate;
import com.hxs.mapper.EmptyClassroomMapper;
import com.hxs.model.entity.EmptyClassroom;
import com.hxs.model.support.EmptyClassRoomItem;
import com.hxs.model.vo.EmptyClassroomVO;
import com.hxs.properties.AdminProperties;
import com.hxs.service.user.EmptyClassroomService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmptyClassroomServiceImpl implements EmptyClassroomService {

    private final EmptyClassroomMapper emptyClassroomMapper;
    @Resource(name = "termStartDate")
    private SystemDate termSystemDate;
    private final AdminProperties adminProperties;

    @Override
    public List<EmptyClassroomVO> getEmptyClassroom(Integer week, Integer weekday,
                                                     Integer startSession, Integer endSession) {
        List<Integer> sessionList = new ArrayList<>();
        for (int i = startSession; i <= endSession; i++) {
            sessionList.add(i);
        }
        return emptyClassroomMapper.getEmptyClassroom(week, weekday, sessionList, sessionList.size());
    }

    @Override
    @Transactional
    @RetryOnSessionExpired
    public void updateEmptyClassRoom(Integer week) {
        long startTime = System.currentTimeMillis();
        int year = termSystemDate.getYear();
        int term = termSystemDate.getTerm();
        int encodedTerm = term * term * 3;
        log.info("管理员更新空教室: {}学年第{}学期 第{}周", year, term, week);

        // 管理员登录教务系统
        EduSession session = EduLoginClient.login(adminProperties.getSid(), adminProperties.getPassword());
        EduClassroomClient client = new EduClassroomClient(session);

        // 先删除本周旧数据
        emptyClassroomMapper.deleteAvailabilityByWeek(week);
        emptyClassroomMapper.deleteClassroomByWeek(week);

        for (int weekday = 1; weekday <= 7; weekday++) {
            for (int sessionNum = 1; sessionNum <= 13; sessionNum++) {
                List<EmptyClassRoomItem> items = client.getEmptyClassroom(
                        year, encodedTerm, 1 << (week - 1), weekday, 1 << (sessionNum - 1));

                if (items.isEmpty()) continue;

                List<EmptyClassroom> classrooms = new ArrayList<>();
                for (EmptyClassRoomItem item : items) {
                    EmptyClassroom ec = new EmptyClassroom();
                    ec.setClassId(item.getClassId());
                    ec.setClassName(item.getClassName());
                    ec.setCampusName(item.getCampusName());
                    ec.setClassCategory(item.getRoomType());
                    ec.setBuilding(extractBuilding(item.getClassName()));
                    ec.setWeekNumber(week);
                    ec.setWeekday(weekday);
                    classrooms.add(ec);
                }

                // 批量插入教室
                emptyClassroomMapper.insertBatch(classrooms);

                // 插入每个教室的空闲节次
                for (EmptyClassroom ec : classrooms) {
                    emptyClassroomMapper.insertAvailability(ec.getId(), List.of(sessionNum));
                }

                // 限速
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ignored) {
                    Thread.currentThread().interrupt();
                }
            }
        }

        session.close();
        log.info("空教室更新完成，耗时 {} 秒", (System.currentTimeMillis() - startTime) / 1000);
    }

    /** 从教室名称提取楼栋信息 */
    private String extractBuilding(String className) {
        if (className == null) return "";
        if (className.contains("公教楼")) return "公教楼";
        if (className.contains("综合楼")) return "综合楼";
        return "";
    }

    @Override
    public void deleteHistoryRecord() {
        log.info("清理历史空教室数据");
        emptyClassroomMapper.deleteAllAvailability();
        emptyClassroomMapper.deleteAllClassroom();
        log.info("历史空教室数据清理完成");
    }
}

package com.hxs.service.user.impl;

import com.hxs.annotation.RetryOnSessionExpired;
import com.hxs.client.EduClassroomClient;
import com.hxs.client.EduLoginClient;
import com.hxs.client.EduSession;
import com.hxs.component.SystemDate;
import com.hxs.mapper.EmptyClassroomMapper;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

        Map<EmptyClassRoomItem, List<Integer>> emptySessionMap = new HashMap<>(284);

        for (int weekday = 1; weekday <= 7; weekday++) {
            for (int sessionNum = 1; sessionNum <= 13; sessionNum++) {
                // 1.获取本周星期 weekday 第 sessionNum 节的空教室信息
                List<EmptyClassRoomItem> items = client.getEmptyClassroom(
                        year, encodedTerm, 1 << (week - 1), weekday, 1 << (sessionNum - 1));

                for (EmptyClassRoomItem classroom : items) {
                    classroom.setWeekday(weekday);
                    classroom.setWeekNumber(week);
                    // 2.处理教室空闲节次数据
                    List<Integer> emptySessions = emptySessionMap.getOrDefault(classroom, new ArrayList<>());
                    emptySessions.add(sessionNum);
                    emptySessionMap.put(classroom, emptySessions);
                }

                // 限速
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ignored) {
                    Thread.currentThread().interrupt();
                }
            }

            // 3.批量插入教室数据
            if (!emptySessionMap.isEmpty()) {
                // 先保存 sessions 信息到临时 Map（以 classId 为 key），避免 insertClassroom 后 id 变化导致 get 失败
                Map<String, List<Integer>> sessionsByClassId = new HashMap<>();
                for (Map.Entry<EmptyClassRoomItem, List<Integer>> entry : emptySessionMap.entrySet()) {
                    sessionsByClassId.put(entry.getKey().getClassId(), entry.getValue());
                }

                List<EmptyClassRoomItem> roomList = new ArrayList<>(emptySessionMap.keySet());
                emptyClassroomMapper.insertClassroom(roomList);

                for (EmptyClassRoomItem room : roomList) {
                    List<Integer> emptySessions = sessionsByClassId.get(room.getClassId());
                    if (emptySessions != null && !emptySessions.isEmpty()) {
                        emptyClassroomMapper.insertEmptySession(room.getId(), emptySessions);
                    }
                }

                // 4.清空map
                emptySessionMap.clear();
            }
        }

        session.close();
        log.info("空教室更新完成，耗时 {} 秒", (System.currentTimeMillis() - startTime) / 1000);
    }

    @Override
    public void deleteHistoryRecord() {
        log.info("清理历史空教室数据");
        emptyClassroomMapper.deleteAllAvailability();
        emptyClassroomMapper.deleteAllClassroom();
        log.info("历史空教室数据清理完成");
    }

}

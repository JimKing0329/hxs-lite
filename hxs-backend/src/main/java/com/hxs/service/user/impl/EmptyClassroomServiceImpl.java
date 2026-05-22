package com.hxs.service.user.impl;

import com.hxs.mapper.EmptyClassroomMapper;
import com.hxs.model.vo.EmptyClassroomVO;
import com.hxs.service.user.EmptyClassroomService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmptyClassroomServiceImpl implements EmptyClassroomService {

    private final EmptyClassroomMapper emptyClassroomMapper;

    @Override
    public List<EmptyClassroomVO> getEmptyClassroom(Integer week, Integer weekday,
                                                     Integer startSession, Integer endSession) {
        List<Integer> sessionList = new ArrayList<>();
        for (int i = startSession; i <= endSession; i++) {
            sessionList.add(i);
        }
        return emptyClassroomMapper.getEmptyClassroom(week, weekday, sessionList, sessionList.size());
    }
}

package com.hxs.service.user;

import com.hxs.model.vo.EmptyClassroomVO;

import java.util.List;

public interface EmptyClassroomService {

    /**
     * 查询空教室
     */
    List<EmptyClassroomVO> getEmptyClassroom(Integer week, Integer weekday,
                                              Integer startSession, Integer endSession);

    /**
     * 更新空教室信息（管理员操作，从教务系统拉取）
     */
    void updateEmptyClassRoom(Integer week);
}

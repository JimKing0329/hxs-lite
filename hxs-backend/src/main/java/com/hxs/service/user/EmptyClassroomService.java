package com.hxs.service.user;

import com.hxs.model.vo.EmptyClassroomVO;

import java.util.List;

public interface EmptyClassroomService {

    /**
     * 查询空教室
     *
     * @param week         教学周
     * @param weekday      星期几（1-7）
     * @param startSession 开始节次
     * @param endSession   结束节次
     * @return 空教室列表
     */
    List<EmptyClassroomVO> getEmptyClassroom(Integer week, Integer weekday,
                                              Integer startSession, Integer endSession);
}

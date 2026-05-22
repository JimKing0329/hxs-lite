package com.hxs.mapper;

import com.hxs.model.vo.EmptyClassroomVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface EmptyClassroomMapper {

    List<EmptyClassroomVO> getEmptyClassroom(Integer week, Integer weekday,
                                              List<Integer> sessionList, int size);
}

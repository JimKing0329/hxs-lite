package com.hxs.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hxs.model.entity.EmptyClassroom;
import com.hxs.model.vo.EmptyClassroomVO;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface EmptyClassroomMapper extends BaseMapper<EmptyClassroom> {

    List<EmptyClassroomVO> getEmptyClassroom(Integer week, Integer weekday,
                                              List<Integer> sessionList, int size);

    @Insert("<script>" +
            "INSERT INTO empty_classroom(class_id, class_name, campus_name, class_category, " +
            "building, week_number, weekday) VALUES " +
            "<foreach collection='list' item='item' separator=','>" +
            "(#{item.classId}, #{item.className}, #{item.campusName}, #{item.classCategory}, " +
            "#{item.building}, #{item.weekNumber}, #{item.weekday})" +
            "</foreach>" +
            "</script>")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insertBatch(@Param("list") List<EmptyClassroom> list);

    @Insert("<script>" +
            "INSERT INTO classroom_availability(classroom_id, empty_session) VALUES " +
            "<foreach collection='sessions' item='s' separator=','>" +
            "(#{classroomId}, #{s})" +
            "</foreach>" +
            "</script>")
    void insertAvailability(@Param("classroomId") Long classroomId,
                            @Param("sessions") List<Integer> sessions);

    @Delete("DELETE FROM classroom_availability WHERE classroom_id IN " +
            "(SELECT id FROM empty_classroom WHERE week_number = #{week})")
    void deleteAvailabilityByWeek(@Param("week") Integer week);

    @Delete("DELETE FROM empty_classroom WHERE week_number = #{week}")
    void deleteClassroomByWeek(@Param("week") Integer week);

    @Delete("DELETE FROM empty_classroom")
    void deleteAllClassroom();

    @Delete("DELETE FROM classroom_availability")
    void deleteAllAvailability();
}

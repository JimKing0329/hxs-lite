package com.hxs.service.newcoursetable;

import com.hxs.model.vo.ClassVO;
import com.hxs.model.vo.CourseTableVO;

import java.util.List;

public interface NewCourseTableService {

    void updateClass();

    void updateCourseTable();

    List<ClassVO> getAllClass();

    CourseTableVO getCourseTableByClassId(String classId);
}

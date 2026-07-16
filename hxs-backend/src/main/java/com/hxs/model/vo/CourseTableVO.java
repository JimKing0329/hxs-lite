package com.hxs.model.vo;

import com.hxs.model.entity.MainCourse;
import com.hxs.model.entity.OtherCourse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseTableVO {
    private List<MainCourse> mainCourseList;
    private List<OtherCourse> otherCourseList;
}

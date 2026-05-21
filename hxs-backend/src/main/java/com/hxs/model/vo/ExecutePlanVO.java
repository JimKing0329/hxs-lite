package com.hxs.model.vo;

import com.hxs.model.entity.ExecuteCourse;
import lombok.Data;

import java.util.List;

@Data
public class ExecutePlanVO {
    private Integer year;
    private Integer term;
    private List<ExecuteCourse> executeCourseList;
}

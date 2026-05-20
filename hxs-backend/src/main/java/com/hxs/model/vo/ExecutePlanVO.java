package com.hxs.model.vo;

import com.hxs.model.entity.ExecuteCourseItem;
import lombok.Data;

import java.util.List;

@Data
public class ExecutePlanVO {
    private Integer year;
    private Integer term;
    private List<ExecuteCourseItem> executeCourseList;
}

package com.hxs.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CourseVO implements Serializable {

    private String courseId;

    private String title;

    private String teacher;

    private String campus;

    private String place;

    private Integer weekday;

    private Integer startSession;

    private Integer endSession;
}

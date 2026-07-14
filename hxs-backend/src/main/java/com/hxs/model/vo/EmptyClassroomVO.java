package com.hxs.model.vo;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@Data
@Builder
public class EmptyClassroomVO implements Serializable {

    private Long id;

    private String classId;

    private String className;

    private String campusName;

    private String classCategory;

    private String building;
}

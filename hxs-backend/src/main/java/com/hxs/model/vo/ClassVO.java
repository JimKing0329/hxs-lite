package com.hxs.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClassVO {
    private Long id;
    private String grade;
    private String classId;
    private String majorId;
    private String college;
    private String className;
    private String campusName;
    private String majorName;
}

package com.hxs.model.support;

import com.alibaba.fastjson2.annotation.JSONField;
import lombok.Data;

import java.util.List;

@Data
public class CourseTableItem {
    @JSONField(name = "kch_id") private String courseId;
    @JSONField(name = "kcmc") private String title;
    @JSONField(name = "xm") private String teacher;
    @JSONField(name = "jxbmc") private String className;
    @JSONField(name = "xf") private Float credit;
    @JSONField(name = "xqj") private Integer weekday;
    @JSONField(name = "jc") private String sessions;
    @JSONField(name = "zcd") private String weeks;
    @JSONField(name = "khfsmc") private String evaluationMode;
    @JSONField(name = "xqmc") private String campus;
    @JSONField(name = "cdmc") private String place;
    @JSONField(name = "zhxs") private Integer weeklyHours;
    @JSONField(name = "zxs") private Integer totalHours;
    private List<Integer> weekList;
    private Integer startSession;
    private Integer endSession;
    private Integer weekNumber;
}

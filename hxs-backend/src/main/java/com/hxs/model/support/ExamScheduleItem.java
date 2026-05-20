package com.hxs.model.support;

import com.alibaba.fastjson2.annotation.JSONField;
import lombok.Data;

@Data
public class ExamScheduleItem {
    @JSONField(name = "kcmc") private String courseName;
    @JSONField(name = "kssj") private String examTime;
    @JSONField(name = "ksdd") private String examPlace;
    @JSONField(name = "ksxs") private String examForm;
    @JSONField(name = "zwh") private String seatNo;
}

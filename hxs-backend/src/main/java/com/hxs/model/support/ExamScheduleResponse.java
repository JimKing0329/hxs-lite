package com.hxs.model.support;

import com.alibaba.fastjson2.annotation.JSONField;
import lombok.Data;

import java.util.List;

@Data
public class ExamScheduleResponse {
    @JSONField(name = "items")
    private List<ExamScheduleItem> items;
}

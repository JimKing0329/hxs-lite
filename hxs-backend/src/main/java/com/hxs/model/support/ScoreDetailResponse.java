package com.hxs.model.support;

import com.alibaba.fastjson2.annotation.JSONField;
import lombok.Data;

import java.util.List;

@Data
public class ScoreDetailResponse {
    @JSONField(name = "items")
    private List<ScoreDetail> items;
}

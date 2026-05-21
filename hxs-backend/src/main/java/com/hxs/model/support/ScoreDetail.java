package com.hxs.model.support;

import com.alibaba.fastjson2.annotation.JSONField;
import lombok.Data;

@Data
public class ScoreDetail {
    @JSONField(name = "xmblmc")
    private String scoreColumn;
    private String scoreRatio;
    @JSONField(name = "xmcj")
    private String score;
}

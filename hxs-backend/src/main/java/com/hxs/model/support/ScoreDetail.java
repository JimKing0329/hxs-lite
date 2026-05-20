package com.hxs.model.support;

import com.alibaba.fastjson2.annotation.JSONField;
import lombok.Data;

@Data
public class ScoreDetail {
    @JSONField(name = "kcmc") private String courseName;
    @JSONField(name = "xm") private String name;
    @JSONField(name = "xbmc") private String gender;
    @JSONField(name = "xmcj") private String scoreItem;
    @JSONField(name = "bfz") private String gradeRatio;
    @JSONField(name = "cj") private String score;
    private String gradeColumn;
}

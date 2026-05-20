package com.hxs.model.support;

import com.alibaba.fastjson2.annotation.JSONField;
import lombok.Data;

@Data
public class ScoreItem {
    @JSONField(name = "xnmmc") private String yearName;
    @JSONField(name = "xqmmc") private String termName;
    @JSONField(name = "kcmc") private String courseName;
    @JSONField(name = "xf") private String credit;
    @JSONField(name = "cj") private String score;
    @JSONField(name = "jd") private String gradePoint;
    @JSONField(name = "kcbj") private String retakeMark;
    @JSONField(name = "ksxz") private String examType;
    @JSONField(name = "kkbmmc") private String collegeName;
    @JSONField(name = "jxb_id") private String classId;
}

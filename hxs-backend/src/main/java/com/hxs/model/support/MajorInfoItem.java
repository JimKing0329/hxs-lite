package com.hxs.model.support;

import com.alibaba.fastjson2.annotation.JSONField;
import lombok.Data;

@Data
public class MajorInfoItem {
    @JSONField(name = "njdm_id") private String gradeId;
    @JSONField(name = "nj") private String grade;
    @JSONField(name = "zyh_id") private String majorId;
    @JSONField(name = "zymc") private String majorName;
    @JSONField(name = "zyfxmc") private String majorDirection;
    @JSONField(name = "jxzxjhxx_id") private String planId;
    @JSONField(name = "zsjg_id") private String collegeId;
}

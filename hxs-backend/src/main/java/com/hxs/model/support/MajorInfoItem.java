package com.hxs.model.support;

import com.alibaba.fastjson2.annotation.JSONField;
import lombok.Data;

@Data
public class MajorInfoItem {
    @JSONField(name = "njdm") private Integer grade;
    @JSONField(name = "jg_id") private String collegeId;
    @JSONField(name = "zymc") private String majorName;
    @JSONField(name = "zyh") private String majorId;
    @JSONField(name = "jxzxjhxx_id") private String majorCode;
}

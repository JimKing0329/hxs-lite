package com.hxs.model.entity;

import com.alibaba.fastjson2.annotation.JSONField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("student_info")
public class StudentInfo {

    @TableId
    private String sid;

    @JSONField(name = "xm")
    private String name;

    @JSONField(name = "zsjg_id")
    private String collegeId;

    @JSONField(name = "zyh_id")
    private String majorId;

    @JSONField(name = "zyfxmc")
    private String majorName;

    @JSONField(name = "nj")
    private String grade;

    private String majorCode;
    private String jw;
    private String jsessionId;
    private String password;
    private String lastLogin;
    private String openId;
    private String bindingKey;

}

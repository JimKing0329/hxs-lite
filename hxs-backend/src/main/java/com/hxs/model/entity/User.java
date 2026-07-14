package com.hxs.model.entity;

import com.alibaba.fastjson2.annotation.JSONField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("user")
public class User {

    // ── 基础信息 ──
    @JSONField(name = "xh")
    @TableId(value = "sid")
    private String sid;

    @JSONField(name = "xm")
    private String name;

    @JSONField(name = "zsjg_id")
    private String collegeName;

    @JSONField(name = "zyh_id")
    private String majorName;

    @JSONField(name = "bh_id")
    private String className;

    @JSONField(name = "xjztdm")
    private String status;

    @JSONField(name = "rxrq")
    private String enrollmentDate;

    @JSONField(name = "ksh")
    private String candidateNumber;

    @JSONField(name = "byzx")
    private String graduationSchool;

    @JSONField(name = "jg")
    private String domicile;

    @JSONField(name = "yzbm")
    private String postalCode;

    @JSONField(name = "zzmmm")
    private String politicsStatus;

    @JSONField(name = "mzm")
    private String nationality;

    @JSONField(name = "pyccdm")
    private String education;

    @JSONField(name = "sjhm")
    private String phoneNumber;

    @JSONField(name = "gddh")
    private String parentsNumber;

    @JSONField(name = "dzyx")
    private String email;

    @JSONField(name = "csrq")
    private String birthday;

    @JSONField(name = "bdh")
    private String registrationNumber;

    @JSONField(name = "tz")
    private String weight;

    @JSONField(name = "sg")
    private String height;

    @JSONField(name = "xbm")
    private String gender;

    // ── 内部字段 ──
    private String majorCode;
    private String jw;
    private String jsessionId;
    private String password;
    private LocalDateTime lastLogin;
    private String openId;
    private String bindingKey;

}

package com.hxs.model.entity;

import com.alibaba.fastjson2.annotation.JSONField;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 专业信息实体 — 映射 major_info 表
 */
@Data
@TableName("major_info")
public class MajorInfo implements Serializable {

    @JSONField(name = "njdm")
    private Integer grade;

    @JSONField(name = "jg_id")
    private String collegeId;

    @JSONField(name = "zymc")
    private String majorName;

    @JSONField(name = "zyh")
    private String majorId;

    @JSONField(name = "jxzxjhxx_id")
    private String majorCode;
}

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

    @TableId(type = IdType.AUTO)
    private Long id;

    @JSONField(name = "njdm_id")
    private String gradeId;

    @JSONField(name = "nj")
    private Integer grade;

    @JSONField(name = "zyh_id")
    private String majorId;

    @JSONField(name = "zymc")
    private String majorName;

    @JSONField(name = "zyfxmc")
    private String majorDirection;

    @JSONField(name = "jxzxjhxx_id")
    private String planId;

    @JSONField(name = "zsjg_id")
    private String collegeId;

    /** 专业代码（major_code，用于关联 student_info） */
    private String majorCode;
}

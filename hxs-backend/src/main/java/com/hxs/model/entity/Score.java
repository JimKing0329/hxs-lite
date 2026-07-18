package com.hxs.model.entity;

import com.alibaba.fastjson2.annotation.JSONField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 成绩实体 — 映射 score 表
 */
@Data
@TableName("score")
public class Score implements Serializable {

    @TableId
    private Long id;

    private String sid;

    @JSONField(name = "cj")
    private String grade;

    @JSONField(name = "kcmc")
    private String courseName;

    @JSONField(name = "jd")
    private String gradePoint;

    @JSONField(name = "kclbmc")
    private String categoryName;

    @JSONField(name = "kkbmmc")
    private String collegeName;

    @JSONField(name = "jsxm")
    private String teacherName;

    //教学班编号
    @JSONField(name = "jxb_id")
    private String classId;

    @JSONField(name = "kcbj")
    private String major;

    @JSONField(name = "xnm")
    private Integer year;

    @JSONField(name = "xqm")
    private Integer term;

    @JSONField(name = "xf")
    private String credit;

    @JSONField(name = "kcxzmc")
    private String courseType;
}

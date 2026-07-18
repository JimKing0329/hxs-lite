package com.hxs.model.entity;


import com.alibaba.fastjson2.annotation.JSONField;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 考试安排实体 — 映射 exam_info 表
 */
@Data
@TableName("exam_info")
public class ExamInfo implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 学生学号 */
    private String sid;

    /** 学年 */
    private Integer year;

    /** 学期 */
    private Integer term;

    @JSONField(name = "kch")
    private String courseId;          // 课程代码

    @JSONField(name = "kcmc")
    private String title;             // 课程名称

    @JSONField(name = "kssj")
    private String time;              // 考试时间

    @JSONField(name = "cdmc")
    private String location;         // 考试地点

    @JSONField(name = "cdxqmc")
    private String campus;           // 考试校区

    @JSONField(name = "zwh")
    private String seat;             // 考试座号

    @JSONField(name = "cxbj")
    private String retake;           // 重修标记

    @JSONField(name = "ksmc")
    private String examName;         // 考试批次名

    @JSONField(name = "jsxx")
    private String teacher;          // 任课教师(含教师id)

    @JSONField(name = "jxbmc")
    private String className;        // 教学班名称

    @JSONField(name = "kkxy")
    private String college;         // 开课学院

    @JSONField(name = "xf")
    private Float credit;            // 课程学分数

    @JSONField(name = "ksfs")
    private String examMethod;       // 考试方式, ep: 笔试 & 开卷 & 机考

    @JSONField(name = "sjbh")
    private String paperId;         // 试卷编号

    @JSONField(name = "bz1")
    private String remark;           // 备注, ep: 免监考班级
}

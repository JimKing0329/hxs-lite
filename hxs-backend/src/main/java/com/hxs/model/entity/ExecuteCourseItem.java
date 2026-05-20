package com.hxs.model.entity;

import com.alibaba.fastjson2.annotation.JSONField;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("execute_course")
public class ExecuteCourseItem {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String majorCode;

    /**
     * 课程名称
     */
    @JSONField(name = "kcmc")
    private String courseName;

    /**
     * 课程学分
     */
    @JSONField(name = "xf")
    private String coursePoint;

    /**
     * 课程教学周
     */
    @JSONField(name = "qsjsz")
    private String courseWeek;

    /**
     * 课程所属学院名称
     */
    @JSONField(name = "kkbmmc")
    private String collegeName;

    /**
     * 课程学时
     */
    @JSONField(name = "zxs")
    private String courseTime;

    /**
     * 课程推荐学期
     */
    @JSONField(name = "yyxdxnxqmc")
    private String recommendTerm;

    /**
     * 课程性质
     */

    @JSONField(name = "kcxzmc")
    private String courseType;

}

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

    @JSONField(name = "kcmc")
    private String courseName;

    @JSONField(name = "xf")
    private String coursePoint;

    @JSONField(name = "zxs")
    private String courseWeek;

    @JSONField(name = "kkxymc")
    private String collegeName;

    @JSONField(name = "zxs")
    private String courseTime;

    @JSONField(name = "jyxdxq")
    private String recommendTerm;

    @JSONField(name = "kclbmc")
    private String courseType;

}

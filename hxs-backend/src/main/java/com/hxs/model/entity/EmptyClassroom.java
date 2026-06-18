package com.hxs.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 空教室实体 — 映射 empty_classroom 表
 */
@Data
@TableName("empty_classroom")
public class EmptyClassroom implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String classId;
    private String className;
    private String campusName;
    private String classCategory;
    private String building;
    private Integer weekNumber;
    private Integer weekday;
}

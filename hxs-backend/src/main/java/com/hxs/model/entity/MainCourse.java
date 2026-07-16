package com.hxs.model.entity;

import com.alibaba.fastjson2.annotation.JSONField;
import com.baomidou.mybatisplus.annotation.IdType;
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
@TableName("main_course")
public class MainCourse {
    @JSONField(serialize = false, deserialize = false)
    @TableId(type = IdType.AUTO)
    private Long id;
    private String courseName;
    private String weeks;
    private String weekDay;
    private Integer startSession;
    private Integer endSession;
    private String classId;
}

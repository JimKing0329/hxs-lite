package com.hxs.model.entity;

import com.alibaba.fastjson2.annotation.JSONField;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("classes")
public class NewCourseTable {

    @JSONField(serialize = false, deserialize = false)
    @TableId(type = IdType.AUTO)
    private Long id;

    @JSONField(name = "njdm")
    private String grade;

    @JSONField(name = "bh_id")
    private String classId;

    @JSONField(name = "zyh_id")
    private String majorId;

    @JSONField(name = "jgmc")
    private String college;

    @JSONField(name = "bjmc")
    private String className;

    @JSONField(name = "xqmc")
    private String campusName;

    @JSONField(name = "zymc")
    private String majorName;
}

package com.hxs.model.support;

import com.alibaba.fastjson2.annotation.JSONField;
import lombok.Data;

@Data
public class EmptyClassRoomItem {
    @JSONField(name = "cd_id") private String classId;
    @JSONField(name = "cdmc") private String className;
    @JSONField(name = "xqmc") private String campusName;
    @JSONField(name = "cdlbmc") private String roomType;
    @JSONField(name = "cdjyz") private String seatCount;
}

package com.hxs.model.support;

import com.alibaba.fastjson2.annotation.JSONField;
import lombok.Data;

import java.util.Objects;

@Data
public class EmptyClassRoomItem {
    @JSONField(name = "cd_id") private String classId;
    @JSONField(name = "cdmc") private String className;
    @JSONField(name = "xqmc") private String campusName;
    @JSONField(name = "cdlbmc") private String classCategory;
    @JSONField(name = "jxlmc") private String building;

    private Long id;
    private Integer weekday;
    private Integer weekNumber;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EmptyClassRoomItem that = (EmptyClassRoomItem) o;
        return Objects.equals(classId, that.classId) &&
                Objects.equals(className, that.className) &&
                Objects.equals(campusName, that.campusName) &&
                Objects.equals(classCategory, that.classCategory) &&
                Objects.equals(building, that.building) &&
                Objects.equals(weekNumber, that.weekNumber) &&
                Objects.equals(weekday, that.weekday);
    }

    @Override
    public int hashCode() {
        return Objects.hash(classId, className, campusName, classCategory, building, weekNumber, weekday);
    }
}

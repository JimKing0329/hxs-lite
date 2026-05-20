package com.hxs.service.user;

import com.hxs.model.dto.UserLoginDTO;
import com.hxs.model.entity.StudentInfo;
import com.hxs.model.vo.ExecutePlanVO;

public interface UserService {
    StudentInfo login(UserLoginDTO dto);
    StudentInfo getStudentInfo();
    ExecutePlanVO getExecutePlan();
    void unbind();
    void updateMajorCode();
}

package com.hxs.service.user;

import com.hxs.model.dto.UserLoginDTO;
import com.hxs.model.entity.User;
import com.hxs.model.vo.ExecutePlanVO;

public interface UserService {
    User login(UserLoginDTO dto);
    User getStudentInfo();
    ExecutePlanVO getExecutePlan();
    void unbind();
    void updateMajorCode();
}

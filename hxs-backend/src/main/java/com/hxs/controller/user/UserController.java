package com.hxs.controller.user;

import com.hxs.context.UserContext;
import com.hxs.model.entity.User;
import com.hxs.model.vo.ExecutePlanVO;
import com.hxs.result.Result;
import com.hxs.service.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 用户控制器 — 用户信息、执行计划、解绑、专业代码
 *
 * RESTful 路径：
 *   GET    /user/student-info    → 学生信息
 *   GET    /user/execute-plan    → 执行计划
 *   PUT    /user/major-code      → 更新专业代码
 *   PUT    /user/unbind          → 解除微信绑定
 */
@RestController
@RequestMapping("/user")
@Slf4j
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/student-info")
    public Result<User> getStudentInfo() {
        log.info("获取用户信息: {}", UserContext.getCurrentId());
        User info = userService.getStudentInfo();
        info.setPassword(null);
        return Result.success(info);
    }

    @GetMapping("/execute-plan")
    public Result<ExecutePlanVO> getExecutePlan() {
        log.info("获取执行计划: {}", UserContext.getCurrentId());
        return Result.success(userService.getExecutePlan());
    }

    @PutMapping("/major-code")
    public Result<?> updateMajorCode() {
        log.info("更新专业代码: {}", UserContext.getCurrentId());
        userService.updateMajorCode();
        return Result.success();
    }

    @PutMapping("/unbind")
    public Result<?> unbind() {
        log.info("解绑: {}", UserContext.getCurrentId());
        userService.unbind();
        return Result.success();
    }

}

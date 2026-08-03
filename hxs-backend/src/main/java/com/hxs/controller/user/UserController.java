package com.hxs.controller.user;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.hxs.context.UserContext;
import com.hxs.mapper.UserMapper;
import com.hxs.model.entity.User;
import com.hxs.model.vo.ExecutePlanVO;
import com.hxs.result.Result;
import com.hxs.service.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 用户控制器 — 用户信息、执行计划、解绑、专业代码
 *
 * RESTful 路径：
 *   GET    /user/student-info              → 学生信息
 *   GET    /user/execute-plan              → 执行计划
 *   PUT    /user/major-code                → 更新专业代码
 *   PUT    /user/unbind                    → 解除微信绑定
 *   GET    /user/support-modal/status      → 查询支持弹窗状态
 *   POST   /user/support-modal/close       → 关闭支持弹窗
 */
@RestController
@RequestMapping("/user")
@Slf4j
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserMapper userMapper;

    // 支持弹窗冷却时间：7天
    private static final int SUPPORT_MODAL_COOLDOWN_DAYS = 7;

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

    /**
     * 查询支持弹窗是否应该显示
     * @return shouldShow: true 表示应该显示
     */
    @GetMapping("/support-modal/status")
    public Result<Map<String, Boolean>> getSupportModalStatus() {
        String sid = UserContext.getCurrentId().toString();
        log.info("查询支持弹窗状态: {}", sid);

        User user = userMapper.selectById(sid);
        boolean shouldShow = true;

        if (user != null && user.getSupportModalClosedAt() != null) {
            LocalDateTime closedAt = user.getSupportModalClosedAt();
            LocalDateTime cooldownEnd = closedAt.plusDays(SUPPORT_MODAL_COOLDOWN_DAYS);
            if (LocalDateTime.now().isBefore(cooldownEnd)) {
                shouldShow = false;
            }
        }

        Map<String, Boolean> result = new HashMap<>();
        result.put("shouldShow", shouldShow);
        return Result.success(result);
    }

    /**
     * 记录支持弹窗关闭
     */
    @PostMapping("/support-modal/close")
    public Result<?> closeSupportModal() {
        String sid = UserContext.getCurrentId().toString();
        log.info("关闭支持弹窗: {}", sid);

        UpdateWrapper<User> wrapper = new UpdateWrapper<>();
        wrapper.eq("sid", sid);
        wrapper.set("support_modal_closed_at", LocalDateTime.now());
        userMapper.update(null, wrapper);

        return Result.success();
    }

}

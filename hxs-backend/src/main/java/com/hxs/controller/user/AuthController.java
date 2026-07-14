package com.hxs.controller.user;

import com.hxs.constant.JwtClaimsConstant;
import com.hxs.context.UserContext;
import com.hxs.model.dto.UserLoginDTO;
import com.hxs.model.entity.User;
import com.hxs.model.vo.UserLoginVO;
import com.hxs.properties.JwtProperties;
import com.hxs.result.Result;
import com.hxs.service.user.UserService;
import com.hxs.utils.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 认证控制器 — login / logout
 */
@RestController
@RequestMapping("/user")
@Slf4j
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final JwtProperties jwtProperties;

    /** POST /user/login */
    @PostMapping("/login")
    public Result<UserLoginVO> login(@RequestBody UserLoginDTO dto) {
        log.info("用户登录: {}", dto.getSid());
        User user = userService.login(dto);

        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.USER_ID, user.getSid());
        claims.put(JwtClaimsConstant.JW, user.getJw());
        claims.put(JwtClaimsConstant.JSESSION_ID, user.getJsessionId());

        String token = JwtUtil.createJWT(jwtProperties.getUserSecretKey(), jwtProperties.getUserTtl(), claims);

        return Result.success(UserLoginVO.builder().token(token).build());
    }

    /** POST /user/logout */
    @PostMapping("/logout")
    public Result<?> logout() {
        log.info("用户注销: {}", UserContext.getCurrentId());
        UserContext.removeCurrentId();
        return Result.success();
    }

}

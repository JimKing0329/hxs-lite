package com.hxs.service.user.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.hxs.client.*;
import com.hxs.component.TermDateManager;
import com.hxs.constant.MessageConstant;
import com.hxs.context.UserContext;
import com.hxs.exception.MessageEmptyException;
import com.hxs.mapper.ExecuteCourseMapper;
import com.hxs.mapper.UserMapper;
import com.hxs.model.dto.UserLoginDTO;
import com.hxs.model.entity.ExecuteCourse;
import com.hxs.model.entity.User;
import com.hxs.model.vo.ExecutePlanVO;
import com.hxs.service.user.UserService;
import com.hxs.utils.AesUtil;
import com.hxs.utils.StringParseUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final EduClient eduClient;
    private final EduSessionManager sessionManager;
    private final UserMapper userMapper;
    private final ExecuteCourseMapper executeCourseMapper;
    private final TermDateManager termDateManager;

    @Override
    public User login(UserLoginDTO dto) {
        long start = System.currentTimeMillis();

        // 1. 登录获取已认证的 EduSession（复用登录时的 HttpClient 连接）
        EduSession loginSession = eduClient.login(dto.getSid(), dto.getPassword());
        Map<String, String> cookie = loginSession.getCookies();

        // 2. 用同一个 Session 创建模块 Client（复用同一个 HttpClient，无需重建 TCP/SSL 连接）
        EduUserClient userClient = new EduUserClient(loginSession);
        EduMajorClient majorClient = new EduMajorClient(loginSession);

        User info = userMapper.selectById(dto.getSid());
        boolean exist = info != null;

        if (!exist) {
            info = userClient.getStudentInfo().getData();
            info.setMajorCode(StringParseUtil.extractParenthesesContent(majorClient.getMajorCode()));
        }

        try {
            info.setPassword(AesUtil.encrypt(dto.getPassword()));
        } catch (Exception e) {
            log.error("密码加密失败", e);
            throw new RuntimeException(MessageConstant.PASSWORD_ENCRYPT_ERROR, e);
        }

        info.setSid(dto.getSid());
        info.setJw(cookie.get("jw"));
        info.setJsessionId(cookie.get("JSESSIONID"));

        if (exist) {
            if (!StringUtils.hasText(info.getBindingKey())) {
                info.setBindingKey(UUID.randomUUID().toString());
            }
            userMapper.updateById(info);
        } else {
            info.setBindingKey(UUID.randomUUID().toString());
            userMapper.insert(info);
        }

        // 关闭 session（不再需要）
        loginSession.close();

        log.info("登录耗时 {} ms", System.currentTimeMillis() - start);
        return info;
    }

    @Override
    public User getStudentInfo() {
        return userMapper.selectById(UserContext.getCurrentId());
    }

    @Override
    public ExecutePlanVO getExecutePlan() {
        ExecutePlanVO vo = new ExecutePlanVO();
        //TODO 修改termDate
        vo.setYear(2025);
        vo.setTerm(1);
//        vo.setYear(termDateManager.getYear());
//        vo.setTerm(termDateManager.getTerm());

        String majorCode = userMapper.queryMajorCodeByMajorId(UserContext.getCurrentId().toString());
        if (majorCode == null) {
            throw new MessageEmptyException(MessageConstant.MAJOR_CODE_NULL);
        }

        List<ExecuteCourse> items = executeCourseMapper
                .selectList(new QueryWrapper<ExecuteCourse>().eq("major_code", majorCode));

        if (!items.isEmpty()) {
            vo.setExecuteCourseList(items);
            return vo;
        }

        // 按需创建模块 Client（通过 SessionManager 恢复当前用户的 Session）
        EduSession session = sessionManager.getOrCreateSession();
        EduMajorClient majorClient = new EduMajorClient(session);
        List<ExecuteCourse> planList = majorClient.getExecutePlan(majorCode);
        if(CollectionUtils.isEmpty(planList)){
            throw new RuntimeException("执行计划列表为空 code ：" + majorCode);
        }
        planList.forEach(i -> i.setMajorCode(majorCode));
        executeCourseMapper.insertBatch(planList);

        vo.setExecuteCourseList(planList);
        return vo;
    }

    @Override
    public void unbind() {
        User user = userMapper.selectById(UserContext.getCurrentId());
        user.setOpenId("");
        userMapper.updateById(user);
    }

    @Override
    public void updateMajorCode() {
        EduSession session = sessionManager.getOrCreateSession();
        EduMajorClient majorClient = new EduMajorClient(session);
        String major = majorClient.getMajorCode();
        String code = StringParseUtil.extractParenthesesContent(major);
        User user = User.builder()
                .majorCode(code).sid(UserContext.getCurrentId().toString()).build();
        userMapper.updateById(user);
    }

}

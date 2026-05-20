package com.hxs.service.user.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.hxs.client.EduClient;
import com.hxs.component.TermDateManager;
import com.hxs.constant.MessageConstant;
import com.hxs.context.UserContext;
import com.hxs.exception.MessageEmptyException;
import com.hxs.mapper.ExecuteCourseMapper;
import com.hxs.mapper.UserMapper;
import com.hxs.model.dto.UserLoginDTO;
import com.hxs.model.entity.ExecuteCourseItem;
import com.hxs.model.entity.StudentInfo;
import com.hxs.model.vo.ExecutePlanVO;
import com.hxs.service.user.UserService;
import com.hxs.utils.AesUtil;
import com.hxs.utils.StringParseUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final EduClient eduClient;
    private final UserMapper userMapper;
    private final ExecuteCourseMapper executeCourseMapper;
    private final TermDateManager termDateManager;

    @Override
    public StudentInfo login(UserLoginDTO dto) {
        long start = System.currentTimeMillis();

        Map<String, String> cookie = eduClient.login(dto.getSid(), dto.getPassword());

        StudentInfo info = userMapper.selectById(dto.getSid());
        boolean exist = info != null;

        if (!exist) {
            info = eduClient.getStudentInfo().getData();
            info.setMajorCode(StringParseUtil.extractParenthesesContent(eduClient.getMajorCode()));
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

        log.info("登录耗时 {} ms", System.currentTimeMillis() - start);
        return info;
    }

    @Override
    public StudentInfo getStudentInfo() {
        return userMapper.selectById(UserContext.getCurrentId());
    }

    @Override
    public ExecutePlanVO getExecutePlan() {
        ExecutePlanVO vo = new ExecutePlanVO();
        vo.setYear(termDateManager.getYear());
        vo.setTerm(termDateManager.getTerm());

        String majorCode = userMapper.queryMajorCodeByMajorId(UserContext.getCurrentId().toString());
        if (majorCode == null) {
            throw new MessageEmptyException(MessageConstant.MAJOR_CODE_NULL);
        }

        List<ExecuteCourseItem> items = executeCourseMapper
                .selectList(new QueryWrapper<ExecuteCourseItem>().eq("major_code", majorCode));

        if (!items.isEmpty()) {
            vo.setExecuteCourseList(items);
            return vo;
        }

        List<ExecuteCourseItem> planList = eduClient.getExecutePlan(majorCode);
        planList.forEach(i -> i.setMajorCode(majorCode));
        executeCourseMapper.insertBatch(planList);

        vo.setExecuteCourseList(planList);
        return vo;
    }

    @Override
    public void unbind() {
        StudentInfo user = userMapper.selectById(UserContext.getCurrentId());
        user.setOpenId("");
        userMapper.updateById(user);
    }

    @Override
    public void updateMajorCode() {
        String major = eduClient.getMajorCode();
        String code = StringParseUtil.extractParenthesesContent(major);
        StudentInfo user = StudentInfo.builder()
                .majorCode(code).sid(UserContext.getCurrentId().toString()).build();
        userMapper.updateById(user);
    }

}

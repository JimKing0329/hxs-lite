package com.hxs.service.admin.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.hxs.client.EduLoginClient;
import com.hxs.client.EduMajorClient;
import com.hxs.client.EduSession;
import com.hxs.constant.MessageConstant;
import com.hxs.exception.LoginFailException;
import com.hxs.mapper.MajorMapper;
import com.hxs.mapper.UserMapper;
import com.hxs.model.dto.UserLoginDTO;
import com.hxs.model.entity.MajorInfo;
import com.hxs.model.entity.User;
import com.hxs.model.support.MajorInfoItem;
import com.hxs.model.vo.UserDistributionVO;
import com.hxs.properties.AdminProperties;
import com.hxs.service.admin.AdminService;
import com.hxs.utils.StringParseUtil;
import com.hxs.utils.WechatClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 管理员服务实现
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final UserMapper userMapper;
    private final MajorMapper majorMapper;
    private final WechatClient wechatClient;
    private final AdminProperties adminProperties;

    @Override
    public User login(UserLoginDTO dto) {
        User user = userMapper.selectOne(new QueryWrapper<User>()
                .eq("sid", dto.getSid())
                .eq("password", dto.getPassword()));
        if (user == null) {
            throw new LoginFailException(MessageConstant.USERNAME_OR_PASSWORD_ERROR);
        }
        user.setLastLogin(LocalDateTime.now());
        userMapper.updateById(user);
        return user;
    }

    @Override
    public List<UserDistributionVO> getUserDistribution() {
        return userMapper.selectCountGroupByCollege();
    }

    @Override
    public Long getUserCount() {
        return userMapper.selectCount(null) - 1;
    }

    @Override
    public Long getTodayLoginCount() {
        return userMapper.selectCount(new QueryWrapper<User>()
                .ge("last_login", LocalDateTime.now().minusHours(24)));
    }

    @Override
    public Long getSevenDayLoginCount() {
        return userMapper.selectCount(new QueryWrapper<User>()
                .ge("last_login", LocalDateTime.now().minusDays(7))
                .le("last_login", LocalDateTime.now()));
    }

    @Override
    @Transactional
    public Integer updateMajorInfo() {
        log.info("管理员更新专业信息");

        // 管理员登录教务系统
        EduSession session = EduLoginClient.login(adminProperties.getSid(), adminProperties.getPassword());
        EduMajorClient majorClient = new EduMajorClient(session);

        // 拉取专业信息，过滤近 4 年的数据并去重
        int minGrade = LocalDate.now().getYear() - 4;
        List<MajorInfo> majorList = majorClient.getMajorInfo().stream()
                .filter(item -> item.getGrade() != null && item.getGrade() >= minGrade)
                .distinct()
                .map(this::toEntity)
                .toList();

        // 先删后插（空列表时跳过插入，避免 SQL 语法错误）
        majorMapper.delete(null);
        if (!majorList.isEmpty()) {
            majorMapper.insertBatch(majorList);
        }

        session.close();
        log.info("专业信息更新成功，共 {} 条", majorList.size());
        return majorList.size();
    }

    /** MajorInfoItem → MajorInfo Entity */
    private MajorInfo toEntity(MajorInfoItem item) {
        MajorInfo entity = new MajorInfo();
        entity.setGrade(item.getGrade());
        entity.setMajorId(item.getMajorId());
        entity.setMajorName(item.getMajorName());
        entity.setCollegeId(item.getCollegeId());
        entity.setMajorCode(StringParseUtil.extractParenthesesContent(item.getMajorName()));
        return entity;
    }

    @Override
    public void updateMenu(String type) {
        wechatClient.updateMenu(type);
    }
}

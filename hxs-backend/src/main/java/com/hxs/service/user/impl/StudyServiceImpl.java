package com.hxs.service.user.impl;

import com.hxs.client.EduSession;
import com.hxs.client.EduSessionManager;
import com.hxs.client.EduStudyClient;
import com.hxs.context.UserContext;
import com.hxs.model.support.StudySituation;
import com.hxs.model.vo.StudySituationVO;
import com.hxs.service.user.StudyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

/**
 * 学习情况服务实现 — 通过 EduStudyClient 从教务系统获取学习情况
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class StudyServiceImpl implements StudyService {

    private final EduSessionManager sessionManager;

    @Override
    public StudySituationVO getStudySituation() {
        return fetchStudySituation();
    }

    @Override
    public StudySituationVO updateStudySituation() {
        return fetchStudySituation();
    }

    /**
     * 从教务系统获取学习情况并转换为 VO
     */
    private StudySituationVO fetchStudySituation() {
        log.info("获取学习情况 userId={}", UserContext.getCurrentId());

        // 获取当前用户的独享 Session（天然用户隔离）
        EduSession session = sessionManager.getOrCreateSession();

        // 创建学习情况模块 Client 并获取数据
        EduStudyClient studyClient = new EduStudyClient(session);
        StudySituation situation = studyClient.getStudySituation();

        // 转换为 VO
        StudySituationVO vo = new StudySituationVO();
        BeanUtils.copyProperties(situation, vo);
        return vo;
    }
}

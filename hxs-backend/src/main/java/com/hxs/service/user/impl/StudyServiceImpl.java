package com.hxs.service.user.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.hxs.annotation.RetryOnSessionExpired;
import com.hxs.client.EduSession;
import com.hxs.client.EduSessionManager;
import com.hxs.client.EduStudyClient;
import com.hxs.context.UserContext;
import com.hxs.mapper.StudySituationMapper;
import com.hxs.model.entity.StudySituationEntity;
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
    private final StudySituationMapper studySituationMapper;

    @Override
    @RetryOnSessionExpired
    public StudySituationVO getStudySituation() {
        Long userId = UserContext.getCurrentId();
        // 优先从本地库查询，避免每次请求教务系统
        StudySituationEntity cached = studySituationMapper.selectOne(
                new QueryWrapper<StudySituationEntity>().eq("sid", userId));
        if (cached != null) {
            log.info("从本地库获取学习情况 userId={}", userId);
            return entityToVO(cached);
        }
        // 本地无记录则从教务系统拉取
        return fetchAndSave();
    }

    @Override
    @RetryOnSessionExpired
    public StudySituationVO updateStudySituation() {
        return fetchAndSave();
    }

    /**
     * 从教务系统拉取学习情况，入库并返回 VO
     */
    private StudySituationVO fetchAndSave() {
        Long userId = UserContext.getCurrentId();
        log.info("从教务系统拉取学习情况 userId={}", userId);

        EduSession session = sessionManager.getOrCreateSession();
        EduStudyClient studyClient = new EduStudyClient(session);
        StudySituation situation = studyClient.getStudySituation();

        // 入库：先删旧再插新
        studySituationMapper.delete(new QueryWrapper<StudySituationEntity>().eq("sid", userId));

        StudySituationEntity entity = new StudySituationEntity();
        BeanUtils.copyProperties(situation, entity);
        entity.setSid(userId);
        studySituationMapper.insert(entity);
        log.info("学习情况入库成功 userId={}", userId);

        // 转换为 VO
        StudySituationVO vo = new StudySituationVO();
        BeanUtils.copyProperties(situation, vo);
        return vo;
    }

    /** Entity → VO */
    private StudySituationVO entityToVO(StudySituationEntity entity) {
        StudySituationVO vo = new StudySituationVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }

}

package com.hxs.service.user.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.hxs.annotation.RetryOnSessionExpired;
import com.hxs.client.EduExamClient;
import com.hxs.client.EduSession;
import com.hxs.component.SystemDate;
import com.hxs.mapper.ExamInfoMapper;
import com.hxs.model.entity.ExamInfo;
import com.hxs.model.vo.ExamInfoVO;
import com.hxs.service.user.ExamService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 考试安排服务实现 — 查询从数据库读，更新从教务系统拉取并持久化
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ExamServiceImpl implements ExamService {

    private final ExamInfoMapper examInfoMapper;
    private final SystemDate termSystemDate;

    @Override
    public List<ExamInfoVO> getExamSchedule(String sid, Integer year, Integer term) {
        log.info("查询考试安排 userId={} year={} term={}", sid, year, term);

        // 从数据库查询
        QueryWrapper<ExamInfo> wrapper = new QueryWrapper<>();
        wrapper.eq("sid", sid)
               .eq("year", year)
               .eq("term", term);

        List<ExamInfo> list = examInfoMapper.selectList(wrapper);

        return list.stream().map(this::toVO).collect(Collectors.toList());
    }

    @Override
    @RetryOnSessionExpired
    public List<ExamInfoVO> updateExamSchedule(String sid, Integer year, Integer term, EduSession session) {
        log.info("刷新考试安排 userId={} year={} term={}", sid, year, term);

        Integer paramTerm = term * term * 3;
        EduExamClient examClient = new EduExamClient(session);

        List<ExamInfo> items = examClient.getExamSchedule(year, paramTerm);
        if (items.isEmpty()) {
            log.info("教务系统未查询到考试安排 userId={}", sid);
            // 清除旧数据
            examInfoMapper.deleteBySidAndYearTerm(sid, year, term);
            return Collections.emptyList();
        }

        // 清除旧数据
        examInfoMapper.deleteBySidAndYearTerm(sid, year, term);

        // 设置 sid、year、term 并持久化
        items.forEach(item -> {
            item.setSid(sid);
            item.setYear(year);
            item.setTerm(term);
        });
        examInfoMapper.insertBatch(items);
        log.info("考试安排更新成功 userId={} count={}", sid, items.size());

        return items.stream().map(this::toVO).collect(Collectors.toList());
    }

    /**
     * 实体转 VO
     */
    private ExamInfoVO toVO(ExamInfo item) {
        return ExamInfoVO.builder()
                .courseName(item.getTitle())
                .examTime(item.getTime())
                .examPlace(item.getLocation())
                .examForm(item.getExamMethod())
                .seatNo(item.getSeat())
                .build();
    }
}

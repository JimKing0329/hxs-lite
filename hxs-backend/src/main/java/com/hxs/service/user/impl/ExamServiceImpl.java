package com.hxs.service.user.impl;

import com.hxs.client.EduExamClient;
import com.hxs.client.EduSession;
import com.hxs.client.EduSessionManager;
import com.hxs.context.UserContext;
import com.hxs.model.support.ExamScheduleItem;
import com.hxs.model.vo.ExamInfoVO;
import com.hxs.service.user.ExamService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 考试安排服务实现 — 通过 EduExamClient 从教务系统获取考试安排
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ExamServiceImpl implements ExamService {

    private final EduSessionManager sessionManager;

    @Override
    public List<ExamInfoVO> getExamSchedule(Integer year, Integer term) {
        return fetchExamSchedule(year, term);
    }

    @Override
    public List<ExamInfoVO> updateExamSchedule(Integer year, Integer term) {
        return fetchExamSchedule(year, term);
    }

    /**
     * 从教务系统获取考试安排并转换为 VO
     */
    private List<ExamInfoVO> fetchExamSchedule(Integer year, Integer term) {
        log.info("获取考试安排 userId={} year={} term={}", UserContext.getCurrentId(), year, term);

        EduSession session = sessionManager.getOrCreateSession();
        EduExamClient examClient = new EduExamClient(session);

        List<ExamScheduleItem> items = examClient.getExamSchedule(year, term);

        return items.stream().map(item -> ExamInfoVO.builder()
                .courseName(item.getCourseName())
                .examTime(item.getExamTime())
                .examPlace(item.getExamPlace())
                .examForm(item.getExamForm())
                .seatNo(item.getSeatNo())
                .build()
        ).collect(Collectors.toList());
    }
}

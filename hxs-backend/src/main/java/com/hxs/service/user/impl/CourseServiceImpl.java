package com.hxs.service.user.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.hxs.annotation.RetryOnSessionExpired;
import com.hxs.client.EduCourseClient;
import com.hxs.client.EduSession;
import com.hxs.client.EduSessionManager;
import com.hxs.component.SystemDate;
import com.hxs.context.UserContext;
import com.hxs.mapper.CourseMapper;
import com.hxs.mapper.UserMapper;
import com.hxs.model.entity.Course;
import com.hxs.model.entity.User;
import com.hxs.model.support.CourseTableItem;
import com.hxs.model.vo.CourseVO;
import com.hxs.model.vo.WeekCourseVO;
import com.hxs.service.user.CourseService;
import com.hxs.utils.StringParseUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class CourseServiceImpl implements CourseService {

    private final EduSessionManager sessionManager;
    private final CourseMapper courseMapper;
    private final UserMapper userMapper;
    @Resource(name = "courseTableDate")
    private final SystemDate termSystemDate;

    @Override
    @Transactional
    @RetryOnSessionExpired
    public void updateCourseTable() {
        String sid = UserContext.getCurrentId().toString();
        int year = termSystemDate.getYear();
        int term = termSystemDate.getTerm();
        term = term * term * 3;
        log.info("刷新课表开始 userId={} year={} term={}", sid, year, term);

        EduSession session = sessionManager.getOrCreateSession();
        EduCourseClient courseClient = new EduCourseClient(session);
        List<CourseTableItem> courseTable = courseClient.getCourseTable(year, term);

        // 先删除该学生原有课程表
        courseMapper.delete(new QueryWrapper<Course>().eq("sid", sid));

        // 按周展开并保存
        List<Course> allCourse = new ArrayList<>();
        for (CourseTableItem item : courseTable) {
            // 先解析节次（在 copy 之前设置到 source，避免被 null 覆盖）
            List<Integer> sessions = StringParseUtil.parseSessionList(item.getSessions());
            item.setStartSession(sessions.get(0));
            item.setEndSession(sessions.get(sessions.size() - 1));

            for (Integer week : item.getWeekList()) {
                Course newItem = new Course();
                BeanUtils.copyProperties(item, newItem);
                newItem.setWeekNumber(week);
                newItem.setSid(sid);
                newItem.setStartSession(sessions.get(0));
                newItem.setEndSession(sessions.get(sessions.size() - 1));
                allCourse.add(newItem);
            }
        }

        courseMapper.insertBatch(allCourse);
        log.info("刷新课表完成 userId={} 共 {} 条记录", sid, allCourse.size());
    }

    @Override
    public List<CourseVO> getOneDayCourse(int weekday, long weeks, String sid) {
        userMapper.updateById(User.builder()
                .lastLogin(LocalDateTime.now())
                .sid(sid)
                .build());

        return courseMapper.queryTodayCourse(sid, weekday, weeks);
    }

    @Override
    public WeekCourseVO getWeekCourse(long week) {
        List<CourseVO> courseVOS = courseMapper.queryWeekCourse(
                UserContext.getCurrentId().toString(), week);

        Map<Integer, List<CourseVO>> map = new HashMap<>();
        for (int i = 1; i <= 7; i++) {
            map.put(i, new ArrayList<>());
        }
        courseVOS.forEach(courseVO -> map.get(courseVO.getWeekday()).add(courseVO));

        return WeekCourseVO.builder()
                .week((int) week)
                .weekCourse(map)
                .build();
    }
}

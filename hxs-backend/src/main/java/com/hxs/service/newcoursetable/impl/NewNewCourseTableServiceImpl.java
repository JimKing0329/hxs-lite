package com.hxs.service.newcoursetable.impl;

import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.toolkit.Db;
import com.hxs.client.EduLoginClient;
import com.hxs.client.EduNewCourseTableClient;
import com.hxs.client.EduSession;
import com.hxs.mapper.MainCourseMapper;
import com.hxs.mapper.NewCourseTableMapper;
import com.hxs.mapper.OtherCourseMapper;
import com.hxs.model.entity.MainCourse;
import com.hxs.model.entity.NewCourseTable;
import com.hxs.model.entity.OtherCourse;
import com.hxs.model.vo.ClassVO;
import com.hxs.model.vo.CourseTableVO;
import com.hxs.properties.AdminProperties;
import com.hxs.service.newcoursetable.NewCourseTableService;
import com.hxs.utils.StringParseUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class NewNewCourseTableServiceImpl implements NewCourseTableService {

    private final NewCourseTableMapper newCourseTableMapper;
    private final MainCourseMapper mainCourseMapper;
    private final OtherCourseMapper otherCourseMapper;
    private final AdminProperties adminProperties;

    @Override
    @Transactional
    public void updateClass() {
        int[] yearTerm = getCurrentYearTerm();
        int year = yearTerm[0];
        int term = yearTerm[1];

        EduSession session = EduLoginClient.login(adminProperties.getSid(), adminProperties.getPassword());
        EduNewCourseTableClient client = new EduNewCourseTableClient(session);

        List<NewCourseTable> allClass = client.getAllClass(year, term).stream()
                .filter(c -> Integer.parseInt(c.getGrade()) > year - 4)
                .collect(Collectors.toList());

        newCourseTableMapper.delete(null);
        Db.saveBatch(allClass);
        log.info("更新班级成功，共 {} 个班级", allClass.size());
    }

    @Override
    @Transactional
    public void updateCourseTable() {
        List<NewCourseTable> classList = newCourseTableMapper.selectList(null);
        int[] yearTerm = getCurrentYearTerm();
        int year = yearTerm[0];
        int term = yearTerm[1];

        EduSession session = EduLoginClient.login(adminProperties.getSid(), adminProperties.getPassword());
        EduNewCourseTableClient client = new EduNewCourseTableClient(session);

        for (NewCourseTable classInfo : classList) {
            JSONObject root = client.updateClassCourseTable(classInfo, year, term);

            List<MainCourse> mainCourses = new ArrayList<>();
            List<OtherCourse> otherCourses = new ArrayList<>();

            // kbList → MainCourse
            List<JSONObject> rawMain = root.getList("kbList", JSONObject.class);
            if (rawMain != null) {
                for (JSONObject raw : rawMain) {
                    List<Integer> sessions = StringParseUtil.parseSessionList(raw.getString("jc"));
                    if (sessions.size() < 2) continue;
                    mainCourses.add(MainCourse.builder()
                            .courseName(raw.getString("kcmc"))
                            .weeks(raw.getString("zcd"))
                            .weekDay(raw.getString("xqjmc"))
                            .startSession(sessions.get(0))
                            .endSession(sessions.get(sessions.size() - 1))
                            .classId(classInfo.getClassId())
                            .build());
                }
            }

            // sjkList → OtherCourse
            List<JSONObject> rawOther = root.getList("sjkList", JSONObject.class);
            if (rawOther != null) {
                for (JSONObject raw : rawOther) {
                    List<String> stringList = StringParseUtil.courseParser(raw.getString("sjkcgs"));
                    otherCourses.add(OtherCourse.builder()
                            .courseName(stringList.get(0))
                            .weeks(stringList.get(1))
                            .classId(classInfo.getClassId())
                            .build());
                }
            }

            if (!mainCourses.isEmpty()) {
                mainCourseMapper.delete(new QueryWrapper<MainCourse>().eq("class_id", classInfo.getClassId()));
                Db.saveBatch(mainCourses);
            }

            if (!otherCourses.isEmpty()) {
                otherCourseMapper.delete(new QueryWrapper<OtherCourse>().eq("class_id", classInfo.getClassId()));
                Db.saveBatch(otherCourses);
            }

            log.info("更新课程成功 {}", classInfo.getClassName());

            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("课程表更新被中断", e);
            }
        }
    }

    @Override
    public List<ClassVO> getAllClass() {
        return newCourseTableMapper.selectList(null).stream()
                .map(c -> ClassVO.builder()
                        .id(c.getId())
                        .classId(c.getClassId())
                        .className(c.getClassName())
                        .campusName(c.getCampusName())
                        .college(c.getCollege())
                        .grade(c.getGrade())
                        .majorId(c.getMajorId())
                        .majorName(c.getMajorName())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public CourseTableVO getCourseTableByClassId(String classId) {
        return CourseTableVO.builder()
                .mainCourseList(mainCourseMapper.selectList(new QueryWrapper<MainCourse>().eq("class_id", classId)))
                .otherCourseList(otherCourseMapper.selectList(new QueryWrapper<OtherCourse>().eq("class_id", classId)))
                .build();
    }

    /**
     * 根据当前日期计算学年和学期
     * @return [year, term]，term 已经过转换（1→3, 2→12）
     */
    private int[] getCurrentYearTerm() {
        int currentYear = LocalDate.now().getYear();
        int term = LocalDate.now().getMonthValue() > 6 ? 1 : 2;
        int year = term == 1 ? currentYear : currentYear - 1;
        term = term * term * 3;
        return new int[]{year, term};
    }
}

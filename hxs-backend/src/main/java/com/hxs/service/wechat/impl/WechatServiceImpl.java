package com.hxs.service.wechat.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.hxs.client.EduLoginClient;
import com.hxs.client.EduSession;
import com.hxs.component.DateManager;
import com.hxs.constant.CampusConstant;
import com.hxs.constant.WechatMessageConstant;
import com.hxs.mapper.ScoreMapper;
import com.hxs.mapper.UserMapper;
import com.hxs.model.entity.Score;
import com.hxs.model.entity.User;
import com.hxs.model.third.WechatMessage;
import com.hxs.model.vo.CourseVO;
import com.hxs.model.vo.EmptyClassroomVO;
import com.hxs.service.user.CourseService;
import com.hxs.service.user.EmptyClassroomService;
import com.hxs.service.user.ScoreService;
import com.hxs.service.wechat.WechatService;
import com.hxs.utils.AesUtil;
import com.hxs.utils.ArticleFactory;
import com.hxs.utils.ConfigFactory;
import com.thoughtworks.xstream.XStream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

/**
 * 微信公众号消息处理实现
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class WechatServiceImpl implements WechatService {

    private final UserMapper userMapper;
    private final CourseService courseService;
    private final ScoreMapper scoreMapper;
    private final ScoreService scoreService;
    private final EmptyClassroomService emptyClassroomService;
    private final DateManager dateManager;
    private final ArticleFactory articleFactory;
    private final ConfigFactory configFactory;

    @Override
    public String checkAndReply(Map<String, String> messageMap) {
        String msgType = messageMap.get("MsgType");
        if ("text".equals(msgType)) {
            return handleTextMessage(messageMap);
        } else if ("event".equals(msgType)) {
            return handleEventMessage(messageMap);
        }
        return "";
    }

    // ────────────── 文本消息 ──────────────

    private String handleTextMessage(Map<String, String> messageMap) {
        String content = messageMap.get("Content");
        if (content.startsWith("绑定")) {
            return handleBindingKey(content, messageMap);
        } else if (content.contains("课表")) {
            return sendCourseTable(messageMap);
        } else if (content.contains("成绩")) {
            return updateAndSendGrade(messageMap);
        }
        return "";
    }

    // ────────────── 事件消息 ──────────────

    private String handleEventMessage(Map<String, String> messageMap) {
        String event = messageMap.get("Event");
        if ("subscribe".equals(event)) {
            return textReply(messageMap, WechatMessageConstant.SUBSCRIBE_REPLY);
        } else if ("CLICK".equals(event)) {
            return handleClickEvent(messageMap);
        }
        return textReply(messageMap, "hello world");
    }

    private String handleClickEvent(Map<String, String> messageMap) {
        String eventKey = messageMap.get("EventKey");
        switch (eventKey) {
            case "getCalender":
                String mediaId = configFactory.get("calender_media_id");
                return imageReply(messageMap, mediaId != null ? mediaId : "");
            case "getMapYH":
                String yhMediaId = configFactory.get("school_map_yh_media_id");
                return imageReply(messageMap, yhMediaId != null ? yhMediaId : "");
            case "getMapHQ":
                String hqMediaId = configFactory.get("school_map_hq_media_id");
                return imageReply(messageMap, hqMediaId != null ? hqMediaId : "");
            case "queryCourseTable":
                return sendCourseTable(messageMap);
            case "queryGrade":
                return sendScores(messageMap);
            case "queryEmptyClassroomYH":
                return sendEmptyClassroom(messageMap, CampusConstant.YUHUA);
            case "queryEmptyClassroomHQ":
                return sendEmptyClassroom(messageMap, CampusConstant.HONGQI);
            case "updateGrade":
                return updateAndSendGrade(messageMap);
            default:
                return textReply(messageMap, "hello world");
        }
    }

    // ────────────── 绑定 ──────────────

    private String handleBindingKey(String content, Map<String, String> messageMap) {
        try {
            String[] parts = content.split(" ");
            if (parts.length < 2) {
                return textReply(messageMap, WechatMessageConstant.FAIL_BIND);
            }
            String bindingKey = parts[1];

            // 查询用户
            User user = userMapper.selectOne(new QueryWrapper<User>().eq("binding_key", bindingKey));
            if (user == null) {
                return textReply(messageMap, WechatMessageConstant.USER_NOT_EXISTS);
            }
            if (StringUtils.hasText(user.getOpenId())) {
                return textReply(messageMap, WechatMessageConstant.USER_ALREADY_BIND);
            }

            // 检查该微信号是否已被绑定
            User existByOpenId = userMapper.selectOne(
                    new QueryWrapper<User>().eq("open_id", messageMap.get("FromUserName")));
            if (existByOpenId != null) {
                return textReply(messageMap, WechatMessageConstant.WECHAT_ALREADY_BIND);
            }

            // 绑定 openId
            user.setOpenId(messageMap.get("FromUserName"));
            userMapper.updateById(user);
            return textReply(messageMap, WechatMessageConstant.SUCCESS_BIND);
        } catch (Exception e) {
            log.error("绑定失败", e);
            return textReply(messageMap, WechatMessageConstant.FAIL_BIND);
        }
    }

    // ────────────── 课表 ──────────────

    private String sendCourseTable(Map<String, String> messageMap) {
        User user = userMapper.selectOne(
                new QueryWrapper<User>().eq("open_id", messageMap.get("FromUserName")));
        if (user == null) {
            return textReply(messageMap, WechatMessageConstant.USER_NOT_BIND);
        }

        LocalDate now = LocalDate.now();
        LocalDate termStartDate = dateManager.getTermStartDate();

        // 今日课表
        long week = weekOf(now, termStartDate);
        int weekday = now.getDayOfWeek().getValue();
        List<CourseVO> todayCourse = courseService.getOneDayCourse(weekday, week, user.getSid());

        StringBuilder reply = new StringBuilder();
        reply.append("===今日课程(").append(now.getMonthValue())
                .append("月").append(now.getDayOfMonth()).append("日)===\n\n");
        if (todayCourse.isEmpty()) {
            reply.append("今日无课程~~\n\n");
        }
        for (CourseVO course : todayCourse) {
            reply.append(course.getTitle()).append("\n")
                    .append(course.getCampus()).append(" ").append(course.getPlace()).append("\n")
                    .append(course.getStartSession()).append("-")
                    .append(course.getEndSession()).append("节\n\n");
        }

        // 明日课表
        now = now.plusDays(1);
        week = weekOf(now, termStartDate);
        weekday = now.getDayOfWeek().getValue();
        List<CourseVO> tomorrowCourse = courseService.getOneDayCourse(weekday, week, user.getSid());

        reply.append("===明日课程(").append(now.getMonthValue())
                .append("月").append(now.getDayOfMonth()).append("日)===\n\n");
        if (tomorrowCourse.isEmpty()) {
            reply.append("明日无课程~~\n\n");
        }
        for (CourseVO course : tomorrowCourse) {
            reply.append(course.getTitle()).append("\n")
                    .append(course.getCampus()).append(" ").append(course.getPlace()).append("\n")
                    .append(course.getStartSession()).append("-")
                    .append(course.getEndSession()).append("节\n\n");
        }
        reply.append("课表不准？<a href=\"http://115.190.9.5/dashboard\">更新课表</a>");

        user.setLastLogin(LocalDateTime.now());
        userMapper.updateById(user);
        return textReply(messageMap, reply.toString());
    }

    // ────────────── 成绩 ──────────────

    private String sendScores(Map<String, String> messageMap) {
        User user = userMapper.selectOne(
                new QueryWrapper<User>().eq("open_id", messageMap.get("FromUserName")));
        if (user == null) {
            return textReply(messageMap, WechatMessageConstant.USER_NOT_BIND);
        }

        int year = dateManager.getYear();
        int term = dateManager.getTerm();

        List<Score> scores = scoreMapper.selectList(
                new QueryWrapper<Score>().eq("sid", user.getSid())
                        .eq("year", year).eq("term", term));

        StringBuilder reply = new StringBuilder();
        reply.append("===").append(year).append("学年第").append(term).append("学期成绩===\n\n");
        if (scores.isEmpty()) {
            reply.append("暂无成绩\n");
        }
        for (Score score : scores) {
            reply.append(score.getCourseName()).append("  ").append(score.getGrade()).append("\n\n");
        }
        reply.append("成绩不完整？<a href=\"http://115.190.9.5/all-scores\">点我查看全部成绩</a>");
        return textReply(messageMap, reply.toString());
    }

    private String updateAndSendGrade(Map<String, String> messageMap) {
        User user = userMapper.selectOne(
                new QueryWrapper<User>().eq("open_id", messageMap.get("FromUserName")));
        if (user == null) {
            return textReply(messageMap, WechatMessageConstant.USER_NOT_BIND);
        }

        try {
            // 登录教务系统验证
            String password = AesUtil.decrypt(user.getPassword());
            EduSession session = EduLoginClient.login(user.getSid(), password);
            session.close();

            // 更新成绩
            scoreService.updateScores();

            return sendScores(messageMap);
        } catch (Exception e) {
            log.error("更新成绩失败", e);
            return textReply(messageMap, WechatMessageConstant.UPDATE_GRADE_FAIL);
        }
    }

    // ────────────── 空教室 ──────────────

    private String sendEmptyClassroom(Map<String, String> messageMap, String campus) {
        LocalDate now = LocalDate.now();
        int week = (int) ChronoUnit.WEEKS.between(dateManager.getTermStartDate(), now) + 1;
        int weekday = now.getDayOfWeek().getValue();

        List<EmptyClassroomVO> classrooms = emptyClassroomService
                .getEmptyClassroom(week, weekday, 1, 13).stream()
                .filter(c -> "多媒体教室".equals(c.getClassCategory()))
                .filter(c -> campus.equals(c.getCampusName()))
                .toList();

        if (classrooms.isEmpty()) {
            return textReply(messageMap, "当前空教室信息为空！！");
        }

        StringBuilder reply = new StringBuilder();
        reply.append("===今日空教室(").append(now.getMonthValue())
                .append("月").append(now.getDayOfMonth()).append("日)===\n\n");
        for (int i = 0; i < Math.min(30, classrooms.size()); i++) {
            reply.append(classrooms.get(i).getClassName()).append("\n\n");
        }
        reply.append("仅随机展示30条空教室信息,\n")
                .append("<a href=\"http://115.190.9.5/empty-classroom\">查询更多请点这里</a>\n")
                .append("\ud83c\udf39\ud83c\udf39\ud83c\udf39");
        return textReply(messageMap, reply.toString());
    }

    // ────────────── 回复工具方法 ──────────────

    /** 文本回复 */
    public String textReply(Map<String, String> messageMap, String content) {
        String articleUrl = articleFactory.getArticle();
        String fullContent = content;
        if (articleUrl != null && !articleUrl.isEmpty()) {
            fullContent = content + "\n\n<a href=\"" + articleUrl + "\">点击文章底部广告支持作者\uD83D\uDC97</a>";
        }
        WechatMessage reply = WechatMessage.builder()
                .toUserName(messageMap.get("FromUserName"))
                .fromUserName(messageMap.get("ToUserName"))
                .createTime(System.currentTimeMillis() / 1000)
                .msgType("text")
                .content(fullContent)
                .build();
        XStream xStream = new XStream();
        xStream.processAnnotations(WechatMessage.class);
        return xStream.toXML(reply);
    }

    /** 图片回复 */
    public String imageReply(Map<String, String> messageMap, String mediaId) {
        return String.format(
                "<xml>\n" +
                "  <ToUserName><![CDATA[%s]]></ToUserName>\n" +
                "  <FromUserName><![CDATA[%s]]></FromUserName>\n" +
                "  <CreateTime>%s</CreateTime>\n" +
                "  <MsgType><![CDATA[image]]></MsgType>\n" +
                "  <Image>\n" +
                "    <MediaId><![CDATA[%s]]></MediaId>\n" +
                "  </Image>\n" +
                "</xml>",
                messageMap.get("FromUserName"), messageMap.get("ToUserName"),
                System.currentTimeMillis() / 1000, mediaId);
    }

    /** 计算教学周（从第一周开始） */
    private static long weekOf(LocalDate date, LocalDate termStartDate) {
        LocalDate aligned = date.minusDays(date.getDayOfWeek().getValue() - 1);
        return ChronoUnit.WEEKS.between(termStartDate, aligned) + 1;
    }
}

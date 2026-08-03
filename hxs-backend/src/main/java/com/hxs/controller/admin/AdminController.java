package com.hxs.controller.admin;

import com.hxs.component.SystemDate;
import com.hxs.constant.JwtClaimsConstant;
import com.hxs.mapper.SystemConfigMapper;
import com.hxs.mapper.SystemDateMapper;
import com.hxs.model.dto.TermStartDateUpdateDTO;
import com.hxs.model.dto.UserLoginDTO;
import com.hxs.model.entity.SystemConfig;
import com.hxs.model.entity.User;
import com.hxs.model.vo.UserDistributionVO;
import com.hxs.model.vo.UserLoginVO;
import com.hxs.properties.JwtProperties;
import com.hxs.result.Result;
import javax.annotation.Resource;
import com.hxs.service.admin.AdminService;
import com.hxs.service.user.EmptyClassroomService;
import com.hxs.utils.ConfigFactory;
import com.hxs.utils.JwtUtil;
import com.hxs.utils.WechatClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 管理员控制器 — 管理后台统计与配置
 *
 * <p>RESTful 路径：
 * <pre>
 *   POST /admin/login                    → 管理员登录
 *   GET  /admin/users/distribution       → 用户学院分布
 *   GET  /admin/users/count              → 用户总数
 *   GET  /admin/logins/today-count       → 今日登录数
 *   GET  /admin/logins/weekly-count      → 近七天登录数
 *   PUT  /admin/term-date                → 更新学期日期
 *   GET  /admin/term-date/{id}           → 查询学期日期
 *   PUT  /admin/majors                   → 更新专业信息
 *   PUT  /admin/menu?type=开学           → 更新微信菜单
 *   POST /admin/empty-classrooms/refresh?week= → 刷新空教室
 * </pre>
 *
 * <p>整改对照（旧 → 新）：
 * <pre>
 *   POST /admin/login                     →  POST /admin/login
 *   GET  /admin/getUserDistribution       →  GET  /admin/users/distribution
 *   GET  /admin/getUserCount              →  GET  /admin/users/count
 *   GET  /admin/getTodayLoginCount        →  GET  /admin/logins/today-count
 *   GET  /admin/getSevenDayLoginCount     →  GET  /admin/logins/weekly-count
 *   PUT  /admin/updateTermStartDate       →  PUT  /admin/term-date
 *   GET  /admin/getCurrentTermStartDate   →  GET  /admin/term-date/{id}
 *   PUT  /admin/updateMajorInfo           →  PUT  /admin/majors
 *   PUT  /admin/updateEmptyClassroom      →  POST /admin/empty-classrooms/refresh
 *   PUT  /admin/updateMenu               →  PUT  /admin/menu
 * </pre>
 */
@RestController
@RequestMapping("/admin")
@Slf4j
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final JwtProperties jwtProperties;
    private final SystemDateMapper systemDateMapper;
    private final SystemConfigMapper systemConfigMapper;
    @Resource(name = "termStartDate")
    private SystemDate termStartDate;
    @Resource(name = "courseTableDate")
    private SystemDate courseTableDate;
    private final EmptyClassroomService emptyClassroomService;
    private final ConfigFactory configFactory;
    private final WechatClient wechatClient;

    /** 管理员登录 */
    @PostMapping("/login")
    public Result<UserLoginVO> login(@RequestBody UserLoginDTO dto) {
        log.info("管理员登录: {}", dto.getSid());
        User user = adminService.login(dto);

        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.EMP_ID, user.getSid());
        String token = JwtUtil.createJWT(jwtProperties.getAdminSecretKey(), jwtProperties.getAdminTtl(), claims);
        log.info("管理员登录成功: empId={}", user.getSid());

        return Result.success(UserLoginVO.builder().token(token).build());
    }

    /** 用户学院分布 */
    @GetMapping("/users/distribution")
    public Result<List<UserDistributionVO>> getUserDistribution() {
        log.info("管理员查询用户分布");
        return Result.success(adminService.getUserDistribution());
    }

    /** 用户总数 */
    @GetMapping("/users/count")
    public Result<Long> getUserCount() {
        log.info("管理员查询用户总数");
        return Result.success(adminService.getUserCount());
    }

    /** 今日登录数 */
    @GetMapping("/logins/today-count")
    public Result<Long> getTodayLoginCount() {
        log.info("管理员查询今日登录数");
        return Result.success(adminService.getTodayLoginCount());
    }

    /** 近七天登录数 */
    @GetMapping("/logins/weekly-count")
    public Result<Long> getSevenDayLoginCount() {
        log.info("管理员查询近七天登录数");
        return Result.success(adminService.getSevenDayLoginCount());
    }

    /** 更新学期日期 */
    @PutMapping("/term-date")
    public Result<Void> updateTermStartDate(@RequestBody TermStartDateUpdateDTO dto) {
        log.info("管理员更新学期日期: year={}, term={}, date={}", dto.getYear(), dto.getTerm(), dto.getDate());

        LocalDate date = LocalDate.parse(dto.getDate());
        // 根据 id 选择对应的内存 bean：id=1 → termStartDate，id=2 → courseTableDate
        SystemDate target = dto.getId() == 2L ? courseTableDate : termStartDate;
        target.setTermStartDate(date);
        target.setYear(dto.getYear());
        target.setTerm(dto.getTerm());

        // 同步更新数据库
        systemDateMapper.updateById(com.hxs.model.entity.SystemDate.builder()
                .id(dto.getId().intValue())
                .termStartDate(date)
                .remark(dto.getRemark())
                .year(dto.getYear())
                .term(dto.getTerm())
                .build());

        return Result.success();
    }

    /** 查询学期日期 */
    @GetMapping("/term-date/{id}")
    public Result<com.hxs.model.entity.SystemDate> getTermDate(@PathVariable Long id) {
        log.info("管理员查询学期日期 id={}", id);
        return Result.success(systemDateMapper.selectById(id));
    }

    /** 更新专业信息 */
    @PutMapping("/majors")
    public Result<Integer> updateMajorInfo() {
        log.info("管理员更新专业信息");
        return Result.success(adminService.updateMajorInfo());
    }

    /** 更新微信菜单（从数据库读取菜单状态） */
    @PutMapping("/menu")
    public Result<Void> updateMenu() {
        log.info("管理员更新微信菜单");
        adminService.updateMenu();
        return Result.success();
    }

    /** 刷新空教室信息 */
    @PostMapping("/empty-classrooms/refresh")
    public Result<Void> updateEmptyClassroom(@RequestParam Integer week) {
        log.info("管理员刷新空教室 week={}", week);
        emptyClassroomService.updateEmptyClassRoom(week);
        return Result.success();
    }

    /** 微信菜单 URL 配置键映射 */
    private static final Map<String, String> MENU_URL_KEY_MAP = Map.of(
            "calender", "wechat_menu_calender_url",
            "map_hq", "wechat_menu_map_hq_url",
            "map_yh", "wechat_menu_map_yh_url"
    );

    /** 获取微信菜单 URL */
    @GetMapping("/config/menu-url")
    public Result<String> getMenuUrl(@RequestParam String type) {
        String configKey = MENU_URL_KEY_MAP.get(type);
        if (configKey == null) {
            return Result.error("不支持的类型: " + type);
        }
        log.info("管理员获取菜单 URL type={}", type);
        return Result.success(configFactory.get(configKey));
    }

    /** 更新微信菜单 URL */
    @PutMapping("/config/menu-url")
    public Result<Void> updateMenuUrl(@RequestParam String type, @RequestParam String url) {
        String configKey = MENU_URL_KEY_MAP.get(type);
        if (configKey == null) {
            return Result.error("不支持的类型: " + type);
        }
        log.info("管理员更新菜单 URL type={} url={}", type, url);

        SystemConfig existing = systemConfigMapper.selectById(configKey);
        if (existing != null) {
            existing.setConfigValue(url);
            systemConfigMapper.updateById(existing);
        } else {
            SystemConfig config = new SystemConfig();
            config.setConfigKey(configKey);
            config.setConfigValue(url);
            config.setRemark("微信菜单链接");
            systemConfigMapper.insert(config);
        }
        configFactory.refresh();
        adminService.updateMenu(); // 更新菜单
        return Result.success();
    }

    /** 获取当前微信菜单状态 */
    @GetMapping("/config/wechat-menu-state")
    public Result<String> getWechatMenuState() {
        String state = configFactory.get("wechat_menu_state");
        log.info("管理员查询微信菜单状态 state={}", state);
        return Result.success(state != null ? state : "开学");
    }

    /** 更新微信菜单状态并推送菜单 */
    @PutMapping("/config/wechat-menu-state")
    public Result<Void> updateWechatMenuState(@RequestParam String state) {
        log.info("管理员更新微信菜单状态 state={}", state);

        SystemConfig existing = systemConfigMapper.selectById("wechat_menu_state");
        if (existing != null) {
            existing.setConfigValue(state);
            systemConfigMapper.updateById(existing);
        } else {
            SystemConfig config = new SystemConfig();
            config.setConfigKey("wechat_menu_state");
            config.setConfigValue(state);
            config.setRemark("当前微信菜单状态");
            systemConfigMapper.insert(config);
        }
        configFactory.refresh();

        // 推送菜单到微信
        adminService.updateMenu();
        return Result.success();
    }

    /** 获取点击次数统计 */
    @GetMapping("/stats/click-counts")
    public Result<Map<String, Integer>> getClickCounts() {
        log.info("管理员查询点击次数统计");
        Map<String, Integer> counts = new HashMap<>();
        counts.put("supportClickCount", parseIntOrZero(configFactory.get("support_click_count")));
        counts.put("courseTableClickCount", parseIntOrZero(configFactory.get("course_table_click_count")));
        return Result.success(counts);
    }

    /** 获取最近7天每日跳转数据 */
    @GetMapping("/stats/daily-jumps")
    public Result<List<Map<String, Object>>> getDailyJumps() {
        log.info("管理员查询每日跳转统计");
        List<Map<String, Object>> dailyStats = new java.util.ArrayList<>();
        java.time.format.DateTimeFormatter fmt = java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd");
        java.time.LocalDate today = java.time.LocalDate.now();

        for (int i = 6; i >= 0; i--) {
            java.time.LocalDate date = today.minusDays(i);
            String key = "support_jump_" + date.format(fmt);
            int count = parseIntOrZero(configFactory.get(key));
            Map<String, Object> dayStat = new HashMap<>();
            dayStat.put("date", date.toString());  // yyyy-MM-dd
            dayStat.put("count", count);
            dailyStats.add(dayStat);
        }
        return Result.success(dailyStats);
    }

    private int parseIntOrZero(String value) {
        if (value == null || value.isEmpty()) return 0;
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}

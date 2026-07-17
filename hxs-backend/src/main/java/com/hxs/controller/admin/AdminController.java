package com.hxs.controller.admin;

import com.hxs.component.DateManager;
import com.hxs.constant.JwtClaimsConstant;
import com.hxs.mapper.SystemConfigMapper;
import com.hxs.mapper.SystemDateMapper;
import com.hxs.model.dto.TermStartDateUpdateDTO;
import com.hxs.model.dto.UserLoginDTO;
import com.hxs.model.entity.SystemConfig;
import com.hxs.model.entity.SystemDate;
import com.hxs.model.entity.User;
import com.hxs.model.vo.UserDistributionVO;
import com.hxs.model.vo.UserLoginVO;
import com.hxs.properties.JwtProperties;
import com.hxs.result.Result;
import com.hxs.service.admin.AdminService;
import com.hxs.service.user.EmptyClassroomService;
import com.hxs.utils.ConfigFactory;
import com.hxs.utils.JwtUtil;
import com.hxs.utils.WechatClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
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
    private final DateManager termDateManager;
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
        // 更新内存中的 DateManager
        termDateManager.setTermStartDate(date);
        termDateManager.setYear(dto.getYear());
        termDateManager.setTerm(dto.getTerm());

        // 同步更新数据库
        systemDateMapper.updateById(SystemDate.builder()
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
    public Result<SystemDate> getTermDate(@PathVariable Long id) {
        log.info("管理员查询学期日期 id={}", id);
        return Result.success(systemDateMapper.selectById(id));
    }

    /** 更新专业信息 */
    @PutMapping("/majors")
    public Result<Integer> updateMajorInfo() {
        log.info("管理员更新专业信息");
        return Result.success(adminService.updateMajorInfo());
    }

    /** 更新微信菜单 */
    @PutMapping("/menu")
    public Result<Void> updateMenu(@RequestParam String type) {
        log.info("管理员更新微信菜单 type={}", type);
        adminService.updateMenu(type);
        return Result.success();
    }

    /** 刷新空教室信息 */
    @PostMapping("/empty-classrooms/refresh")
    public Result<Void> updateEmptyClassroom(@RequestParam Integer week) {
        log.info("管理员刷新空教室 week={}", week);
        emptyClassroomService.updateEmptyClassRoom(week);
        return Result.success();
    }

    /** 微信图片素材类型 */
    private static final Map<String, String> MEDIA_TYPE_KEY_MAP = Map.of(
            "calender", "calender_media_id",
            "map_hq", "school_map_hq_media_id",
            "map_yh", "school_map_yh_media_id"
    );

    /** 获取微信素材 mediaId */
    @GetMapping("/config/media-id")
    public Result<String> getMediaId(@RequestParam String type) {
        String configKey = MEDIA_TYPE_KEY_MAP.get(type);
        if (configKey == null) {
            return Result.error("不支持的类型: " + type);
        }
        log.info("管理员获取素材 mediaId type={}", type);
        return Result.success(configFactory.get(configKey));
    }

    /** 上传微信图片素材（校历/地图），后端上传到微信并更新 mediaId */
    @PostMapping("/config/media-id")
    public Result<String> uploadMediaId(@RequestParam String type, @RequestParam("file") MultipartFile file) {
        String configKey = MEDIA_TYPE_KEY_MAP.get(type);
        if (configKey == null) {
            return Result.error("不支持的类型: " + type);
        }
        log.info("管理员上传素材 type={}, name={}, size={}", type, file.getOriginalFilename(), file.getSize());

        // 校验文件
        if (file.isEmpty()) {
            return Result.error("文件不能为空");
        }
        String originalName = file.getOriginalFilename();
        if (originalName == null || (!originalName.endsWith(".jpg") && !originalName.endsWith(".png")
                && !originalName.endsWith(".jpeg"))) {
            return Result.error("仅支持 jpg/png/jpeg 格式的图片");
        }
        if (file.getSize() > 10 * 1024 * 1024) {
            return Result.error("图片大小不能超过 10MB（微信临时素材限制）");
        }

        try {
            // 上传到微信
            String mediaId = wechatClient.uploadMedia(file.getBytes(), originalName);

            // 更新数据库（存在则更新，不存在则插入）
            SystemConfig existing = systemConfigMapper.selectById(configKey);
            if (existing != null) {
                existing.setConfigValue(mediaId);
                systemConfigMapper.updateById(existing);
            } else {
                SystemConfig config = new SystemConfig();
                config.setConfigKey(configKey);
                config.setConfigValue(mediaId);
                config.setRemark("微信公众号素材 mediaId");
                systemConfigMapper.insert(config);
            }

            // 刷新内存缓存
            configFactory.refresh();

            log.info("素材 mediaId 更新成功 type={}, mediaId={}", type, mediaId);
            return Result.success(mediaId);
        } catch (IOException e) {
            log.error("读取上传文件失败", e);
            return Result.error("读取文件失败");
        } catch (RuntimeException e) {
            log.error("上传微信素材失败", e);
            return Result.error(e.getMessage());
        }
    }
}

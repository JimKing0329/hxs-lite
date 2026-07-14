package com.hxs.service.admin;

import com.hxs.model.dto.UserLoginDTO;
import com.hxs.model.entity.User;
import com.hxs.model.vo.UserDistributionVO;

import java.util.List;

/**
 * 管理员服务接口
 */
public interface AdminService {

    /**
     * 管理员登录校验
     * @param dto 登录请求（sid + password）
     * @return 校验通过的用户实体
     */
    User login(UserLoginDTO dto);

    /**
     * 获取用户学院分布统计
     */
    List<UserDistributionVO> getUserDistribution();

    /**
     * 获取用户总数（排除管理员）
     */
    Long getUserCount();

    /**
     * 获取今日登录用户数
     */
    Long getTodayLoginCount();

    /**
     * 获取近七天登录用户数
     */
    Long getSevenDayLoginCount();

    /**
     * 更新专业信息（从教务系统拉取并入库）
     * @return 更新数量
     */
    Integer updateMajorInfo();

    /**
     * 更新微信公众号菜单
     * @param type 菜单类型（开学/假期/迎新）
     */
    void updateMenu(String type);
}

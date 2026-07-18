package com.hxs.utils;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 微信 API 客户端 — access_token 管理 + 菜单更新
 */
@Slf4j
@Component
public class WechatClient {

    private String appId;
    private String appSecret;
    private String baseUrl;
    private String courseUrl;
    private ConfigFactory configFactory;

    /** 缓存的 access_token + 过期时间 */
    private volatile String cachedToken;
    private volatile long tokenExpireTime;
    private final Object tokenLock = new Object();

    @Value("${wechat.app-id}")
    public void setAppId(String appId) {
        this.appId = appId;
    }

    @Value("${wechat.app-secret}")
    public void setAppSecret(String appSecret) {
        this.appSecret = appSecret;
    }

    @Value("${hxs.base-url}")
    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    @Value("${hxs.course-url}")
    public void setCourseUrl(String courseUrl) {
        this.courseUrl = courseUrl;
    }

    @org.springframework.beans.factory.annotation.Autowired
    public void setConfigFactory(ConfigFactory configFactory) {
        this.configFactory = configFactory;
    }

    /**
     * 获取微信 access_token（带缓存，线程安全）
     */
    public String getAccessToken() {
        // 快速路径：缓存未过期直接返回
        if (cachedToken != null && System.currentTimeMillis() < tokenExpireTime) {
            return cachedToken;
        }
        // 同步刷新
        synchronized (tokenLock) {
            if (cachedToken != null && System.currentTimeMillis() < tokenExpireTime) {
                return cachedToken;
            }
            String url = String.format(
                    "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=%s&secret=%s",
                    appId, appSecret);
            try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
                HttpGet httpGet = new HttpGet(url);
                try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
                    String body = EntityUtils.toString(response.getEntity());
                    JSONObject json = JSON.parseObject(body);
                    String token = json.getString("access_token");
                    if (token == null) {
                        log.error("获取微信 access_token 失败: {}", body);
                        throw new RuntimeException("获取微信 access_token 失败");
                    }
                    // 提前 5 分钟过期，避免边界时间窗口问题
                    this.cachedToken = token;
                    this.tokenExpireTime = System.currentTimeMillis() +
                            (json.getLongValue("expires_in") - 300) * 1000L;
                    log.info("微信 access_token 刷新成功");
                    return token;
                }
            } catch (IOException e) {
                throw new RuntimeException("获取微信 access_token 异常", e);
            }
        }
    }

    /**
     * 更新微信公众号菜单 — 从数据库读取菜单状态和 URL 配置
     */
    public void updateMenu() {
        String type = configFactory.get("wechat_menu_state");
        if (type == null) {
            type = "开学";
        }

        String accessToken = getAccessToken();
        String url = String.format(
                "https://api.weixin.qq.com/cgi-bin/menu/create?access_token=%s", accessToken);

        String menuJson = buildMenuJson(type);
        log.info("更新微信菜单 state={}", type);

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost(url);
            httpPost.setHeader("Content-Type", "application/json;charset=utf-8");
            httpPost.setEntity(new StringEntity(menuJson, "utf-8"));
            try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                String result = EntityUtils.toString(response.getEntity());
                log.info("微信菜单更新结果: {}", result);
            }
        } catch (IOException e) {
            throw new RuntimeException("更新微信菜单失败", e);
        }
    }

    private String buildMenuJson(String type) {
        // 从数据库读取菜单 URL 配置
        String calenderUrl = configFactory.get("wechat_menu_calender_url");
        String mapYhUrl = configFactory.get("wechat_menu_map_yh_url");
        String mapHqUrl = configFactory.get("wechat_menu_map_hq_url");

        SubMenu menu1 = new SubMenu("教务工具");
        menu1.subButton = new ArrayList<>();
        menu1.subButton.add(new ViewButton("教务查询", baseUrl + "/dashboard"));
        menu1.subButton.add(new ViewButton("成绩计算器", baseUrl + "/gpa-calculator"));
        menu1.subButton.add(new ViewButton("常见问题", "https://mp.weixin.qq.com/s/_VNcQx3YpF_NSKZUqwuYvg"));

        SubMenu menu2 = new SubMenu("校历/地图");
        menu2.subButton = new ArrayList<>();
        menu2.subButton.add(new ViewButton("校历", calenderUrl != null ? calenderUrl : ""));
        menu2.subButton.add(new ViewButton("地图(裕华)", mapYhUrl != null ? mapYhUrl : ""));
        menu2.subButton.add(new ViewButton("地图(红旗)", mapHqUrl != null ? mapHqUrl : ""));


        List<MenuItem> buttons = new ArrayList<>();
        buttons.add(menu1);
        buttons.add(menu2);

        if ("开学".equals(type)) {
            SubMenu menu3 = new SubMenu("快捷查询");
            menu3.subButton = new ArrayList<>();
            menu3.subButton.add(new ClickButton("查询课表", "queryCourseTable"));
            menu3.subButton.add(new ClickButton("更新成绩", "queryGrade"));
            menu3.subButton.add(new ClickButton("考试安排", "queryExamInfo"));
            menu3.subButton.add(new ClickButton("空教室(红旗)", "queryEmptyClassroomHQ"));
            menu3.subButton.add(new ClickButton("空教室(裕华)", "queryEmptyClassroomYH"));
            buttons.add(menu3);
        } else if ("假期".equals(type)) {
            SubMenu menu3 = new SubMenu("快捷查询");
            menu3.subButton = new ArrayList<>();
            menu3.subButton.add(new ClickButton("更新成绩", "updateGrade"));
            buttons.add(menu3);
        } else if ("迎新".equals(type)) {
            SubMenu menu3 = new SubMenu("快捷查询");
            menu3.subButton = new ArrayList<>();
            menu3.subButton.add(new ViewButton("新学期课表", courseUrl));
            menu3.subButton.add(new ClickButton("更新成绩", "queryGrade"));
            buttons.add(menu3);
        }

        MenuWrapper menu = new MenuWrapper();
        menu.button = buttons;
        return JSON.toJSONString(menu);
    }

    // ---- 微信菜单数据模型 ----

    interface MenuItem {}

    static class MenuWrapper {
        List<MenuItem> button;
        public List<MenuItem> getButton() { return button; }
        public void setButton(List<MenuItem> button) { this.button = button; }
    }

    static class SubMenu implements MenuItem {
        private String name;
        private List<MenuItem> subButton;

        SubMenu(String name) { this.name = name; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public List<MenuItem> getSub_button() { return subButton; }
        public void setSub_button(List<MenuItem> subButton) { this.subButton = subButton; }
    }

    static class ViewButton implements MenuItem {
        private String name;
        private String type;
        private String url;

        ViewButton(String name, String url) {
            this.name = name;
            this.type = "view";
            this.url = url;
        }

        public String getName() { return name; }
        public String getType() { return type; }
        public String getUrl() { return url; }
    }

    static class ClickButton implements MenuItem {
        private String name;
        private String type;
        private String key;

        ClickButton(String name, String key) {
            this.name = name;
            this.type = "click";
            this.key = key;
        }

        public String getName() { return name; }
        public String getType() { return type; }
        public String getKey() { return key; }
    }
}

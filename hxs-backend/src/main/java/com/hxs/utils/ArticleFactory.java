package com.hxs.utils;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.hxs.mapper.SystemConfigMapper;
import com.hxs.mapper.WechatArticleMapper;
import com.hxs.model.entity.SystemConfig;
import com.hxs.model.entity.WechatArticle;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

/**
 * 广告文章工厂 — 启动时加载 wechat_article 表，随机获取广告文章 URL
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ArticleFactory {

    private final WechatArticleMapper wechatArticleMapper;
    private final SystemConfigMapper systemConfigMapper;
    private final ConfigFactory configFactory;

    private List<WechatArticle> articleList;

    private static final Random RANDOM = new Random();

    @PostConstruct
    public void init() {
        articleList = wechatArticleMapper.selectList(null);
        log.info("ArticleFactory 初始化完成, 文章数: {}", articleList.size());
    }

    /**
     * 随机获取一篇文章 URL
     */
    public String getRandomArticle() {
        if (articleList == null || articleList.isEmpty()) return null;
        return articleList.get(RANDOM.nextInt(articleList.size())).getUrl();
    }

    /**
     * 兼容旧方法
     */
    public String getArticle() {
        return getRandomArticle();
    }

    /**
     * 更新支持点击次数
     */
    public void updateClickCount(int count) {
        incrementClickCountByKey("support_click_count", count);
    }

    /**
     * 通用递增点击次数（自动 +1）
     */
    public void incrementClickCount(String configKey) {
        String countStr = configFactory.get(configKey);
        int count = (countStr != null) ? Integer.parseInt(countStr) : 0;
        count++;
        incrementClickCountByKey(configKey, count);
    }

    /**
     * 按 key 更新点击次数
     */
    private void incrementClickCountByKey(String configKey, int count) {
        UpdateWrapper<SystemConfig> wrapper = new UpdateWrapper<>();
        wrapper.eq("config_key", configKey);
        SystemConfig config = new SystemConfig();
        config.setConfigValue(String.valueOf(count));
        config.setUpdatedAt(LocalDateTime.now());
        int rows = systemConfigMapper.update(config, wrapper);
        if (rows == 0) {
            // 不存在则插入
            SystemConfig newConfig = new SystemConfig();
            newConfig.setConfigKey(configKey);
            newConfig.setConfigValue(String.valueOf(count));
            newConfig.setRemark("点击次数统计");
            newConfig.setUpdatedAt(LocalDateTime.now());
            systemConfigMapper.insert(newConfig);
        }
        // 刷新缓存
        configFactory.refresh();
    }
}

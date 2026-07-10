package com.hxs.utils;

import com.hxs.mapper.WechatArticleMapper;
import com.hxs.model.entity.WechatArticle;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
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

    private List<WechatArticle> articleList;

    private static final Random RANDOM = new Random();

    @PostConstruct
    public void init() {
        articleList = wechatArticleMapper.selectList(null);
        log.info("ArticleFactory 初始化完成, 文章数: {}", articleList.size());
    }

    public String getArticle() {
        if (articleList.isEmpty()) return "";
        return articleList.get(RANDOM.nextInt(articleList.size())).getUrl();
    }
}

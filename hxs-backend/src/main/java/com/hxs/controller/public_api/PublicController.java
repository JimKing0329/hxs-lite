package com.hxs.controller.public_api;

import com.hxs.model.vo.ArticleVO;
import com.hxs.result.Result;
import com.hxs.utils.ArticleFactory;
import com.hxs.utils.ConfigFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * 公开接口 — 无需登录
 * 路径 /public/** 不在 JWT 拦截器范围内
 */
@RestController
@RequestMapping("/public")
@Slf4j
@RequiredArgsConstructor
public class PublicController {

    private final ArticleFactory articleFactory;
    private final ConfigFactory configFactory;

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");

    /**
     * 随机获取一篇支持文章
     */
    @GetMapping("/support-article")
    public Result<ArticleVO> getSupportArticle() {
        String url = articleFactory.getRandomArticle();
        if (url == null || url.isEmpty()) {
            return Result.error("暂无支持文章");
        }
        ArticleVO vo = new ArticleVO();
        vo.setUrl(url);
        return Result.success(vo);
    }

    /**
     * 记录跳转次数 +1（总次数 + 当天次数）
     */
    @PostMapping("/support-article/click")
    public Result<?> recordClick() {
        try {
            // 1. 更新总点击次数
            articleFactory.incrementClickCount("support_click_count");

            // 2. 更新当天跳转次数
            String todayKey = "support_jump_" + LocalDate.now().format(DATE_FORMAT);
            articleFactory.incrementClickCount(todayKey);

            log.info("支持跳转次数 +1, 日期: {}", todayKey);
        } catch (Exception e) {
            log.error("记录跳转次数失败", e);
        }
        return Result.success();
    }
}

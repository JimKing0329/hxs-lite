package com.hxs.controller.public_api;

import com.hxs.model.vo.ArticleVO;
import com.hxs.result.Result;
import com.hxs.utils.ArticleFactory;
import com.hxs.utils.ConfigFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

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
     * 记录点击次数 +1
     */
    @PostMapping("/support-article/click")
    public Result<?> recordClick() {
        try {
            // 读取当前 click_count
            String countStr = configFactory.get("support_click_count");
            int count = (countStr != null) ? Integer.parseInt(countStr) : 0;
            count++;

            // 更新到数据库（通过 configFactory 的底层 mapper 无法直接 update，
            // 这里简化处理：直接更新内存缓存 + 数据库）
            articleFactory.updateClickCount(count);
            configFactory.refresh();

            log.info("支持点击次数 +1, 当前: {}", count);
        } catch (Exception e) {
            log.error("记录点击次数失败", e);
        }
        return Result.success();
    }
}

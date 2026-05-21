package com.hxs.controller.user;

import com.hxs.context.UserContext;
import com.hxs.model.support.TextBookItem;
import com.hxs.result.Result;
import com.hxs.service.user.TextBookService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 教材控制器 — 教材信息查询
 *
 * <p>RESTful 路径：
 * <pre>
 *   GET /textbooks/{year}/{term}  → 查询指定学年学期的教材列表
 * </pre>
 *
 * <p>整改对照（旧 → 新）：
 * <pre>
 *   GET /textBook/getTextBook?year=&term=  →  GET /textbooks/{year}/{term}
 * </pre>
 */
@RestController
@RequestMapping("/textbooks")
@Slf4j
@RequiredArgsConstructor
public class TextBookController {

    private final TextBookService textBookService;

    /** 查询指定学年学期的教材信息 */
    @GetMapping("/{year}/{term}")
    public Result<List<TextBookItem>> getTextbooks(@PathVariable String year,
                                                    @PathVariable String term) {
        log.info("查询教材 userId={} year={} term={}", UserContext.getCurrentId(), year, term);
        List<TextBookItem> books = textBookService.getTextbooks(year, term);
        return Result.success(books);
    }
}

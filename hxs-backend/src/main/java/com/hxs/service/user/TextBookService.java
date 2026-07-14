package com.hxs.service.user;

import com.hxs.model.support.TextBookItem;

import java.util.List;

/**
 * 教材服务 — 教材信息查询
 */
public interface TextBookService {

    /**
     * 获取指定学年学期的教材列表
     * @param year 学年（如 "2024"）
     * @param term 学期（"1" 第一学期 / "2" 第二学期）
     * @return 教材列表（已过滤无教材课程）
     */
    List<TextBookItem> getTextbooks(String year, String term);
}

package com.hxs.service.user.impl;

import com.hxs.client.EduSession;
import com.hxs.client.EduSessionManager;
import com.hxs.client.EduTextbookClient;
import com.hxs.context.UserContext;
import com.hxs.model.support.TextBookItem;
import com.hxs.service.user.TextBookService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 教材服务实现 — 通过 EduTextbookClient 从教务系统爬取教材信息
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class TextBookServiceImpl implements TextBookService {

    private final EduSessionManager sessionManager;

    @Override
    public List<TextBookItem> getTextbooks(String year, String term) {
        // 教务系统学期代码：第1学期→"3"，第2学期→"12"
        String eduTerm = "1".equals(term) ? "3" : "12";

        log.info("获取教材信息 userId={} year={} term={}(eduTerm={})",
                UserContext.getCurrentId(), year, term, eduTerm);

        // 获取当前用户的独享 Session（天然用户隔离）
        EduSession session = sessionManager.getOrCreateSession();

        // 创建教材模块 Client 并获取数据
        EduTextbookClient textbookClient = new EduTextbookClient(session);
        return textbookClient.getTextBook(year, eduTerm);
    }
}

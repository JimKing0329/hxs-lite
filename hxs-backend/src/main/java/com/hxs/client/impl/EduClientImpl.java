package com.hxs.client.impl;

import com.hxs.client.EduClient;
import com.hxs.client.EduLoginClient;
import com.hxs.client.EduSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * EduClient 实现 — 委托给 EduLoginClient 完成登录
 */
@Slf4j
@Component
public class EduClientImpl implements EduClient {

    @Override
    public EduSession login(String sid, String password) {
        log.info("发起教务系统登录 sid={}", sid);
        EduSession session = EduLoginClient.login(sid, password);
        log.info("教务系统登录成功 sid={}", sid);
        return session;
    }

}

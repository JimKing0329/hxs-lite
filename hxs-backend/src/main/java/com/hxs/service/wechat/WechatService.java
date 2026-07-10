package com.hxs.service.wechat;

import java.util.Map;

/**
 * 微信公众号消息处理服务
 */
public interface WechatService {

    /**
     * 检查消息类型并回复
     * @param messageMap 解析后的微信消息 Map
     * @return XML 回复字符串
     */
    String checkAndReply(Map<String, String> messageMap);
}

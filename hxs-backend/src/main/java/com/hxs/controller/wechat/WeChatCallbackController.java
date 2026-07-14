package com.hxs.controller.wechat;

import com.hxs.service.wechat.WechatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;
import org.springframework.web.bind.annotation.*;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * 微信公众号回调控制器 — 接入验证 + 消息接收与回复
 *
 * <p>路径：
 * <pre>
 *   GET  /wechat/callback → 服务器接入验证
 *   POST /wechat/callback → 接收用户消息并回复
 * </pre>
 *
 * <p>注意：此接口不对接 JWT，无需认证（请求来自微信服务器）
 */
@RestController
@RequestMapping("/wechat")
@Slf4j
@RequiredArgsConstructor
public class WeChatCallbackController {

    private final WechatService wechatService;

    /** 微信公众号接入验证 */
    @GetMapping("/callback")
    public String check(String signature, String timestamp, String nonce, String echostr) {
        return echostr;
    }

    /** 接收微信消息并回复 */
    @PostMapping(value = "/callback", produces = "application/xml;charset=UTF-8")
    public String receiveAndReply(HttpServletRequest request) throws IOException {
        Map<String, String> messageMap = parseXml(request.getInputStream());
        log.info("接收到微信消息: {}", messageMap);
        return wechatService.checkAndReply(messageMap);
    }

    /** 解析微信 XML 消息为 Map */
    private Map<String, String> parseXml(ServletInputStream input) {
        Map<String, String> map = new HashMap<>();
        SAXReader reader = new SAXReader();
        try {
            Document document = reader.read(input);
            document.getRootElement().elements()
                    .forEach(e -> map.put(e.getName(), e.getText()));
        } catch (DocumentException e) {
            log.error("解析微信 XML 消息失败", e);
        }
        return map;
    }
}

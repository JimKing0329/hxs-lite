package com.hxs.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 管理员教务系统凭证配置
 */
@Data
@Component
@ConfigurationProperties(prefix = "hsxf.admin")
public class AdminProperties {
    private String sid;
    private String password;
}

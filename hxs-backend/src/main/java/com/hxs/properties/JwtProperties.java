package com.hxs.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "hxs.jwt")
public class JwtProperties {
    private String adminSecretKey;
    private long adminTtl;
    private String adminTokenName;
    private String userSecretKey;
    private long userTtl;
    private String userTokenName;
}

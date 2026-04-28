package com.revy.talkblogback.auth;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "auth.jwt")
public class AuthProperties {

    private String issuer = "talkblog-back";
    private String secret = "change-me-in-production-change-me-in-production";
    private long expirationMinutes = 120;
}

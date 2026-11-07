package com.example.paypulse.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "password-reset")
public record PasswordResetProperties(
        Duration tokenTtl,
        String linkBase,
        int maxRequestsPerEmail,
        int maxRequestsPerIp,
        Duration rateLimitWindow
) {
}

package com.example.paypulse.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "login-lockout")
public record LoginLockoutProperties(
        int maxAttempts,
        Duration lockoutDuration
) {
}

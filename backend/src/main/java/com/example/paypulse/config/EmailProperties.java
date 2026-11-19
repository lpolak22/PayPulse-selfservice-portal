package com.example.paypulse.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "email")
public record EmailProperties(
        String fromAddress,
        String fromName,
        int maxRetries,
        Duration retryDelay
) {
}

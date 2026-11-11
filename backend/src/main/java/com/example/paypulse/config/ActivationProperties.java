package com.example.paypulse.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "activation")
public record ActivationProperties(
        Duration tokenTtl,
        String linkBase
) {
}

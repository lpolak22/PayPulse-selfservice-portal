package com.example.paypulse.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "sevenpay")
public record SevenPayProperties(
        String baseUrl,
        String apiKey,
        int pageSize,
        int transactionPageSize,
        int maxRetries,
        Duration retryDelay,
        Duration connectTimeout,
        Duration readTimeout
) {

    public boolean hasApiKey() {
        return apiKey != null && !apiKey.isBlank();
    }

    @Override
    public String toString() {
        return "SevenPayProperties[baseUrl=" + baseUrl + ", apiKey=" + (hasApiKey() ? "***" : "<missing>")
                + ", pageSize=" + pageSize + ", transactionPageSize=" + transactionPageSize
                + ", maxRetries=" + maxRetries + "]";
    }
}

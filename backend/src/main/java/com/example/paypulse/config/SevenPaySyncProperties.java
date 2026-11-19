package com.example.paypulse.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "sevenpay.sync")
public record SevenPaySyncProperties(boolean enabled, int initialDays, int overlapDays, Duration interval) {

    private static final int DEFAULT_INITIAL_DAYS = 30;
    private static final int DEFAULT_OVERLAP_DAYS = 3;
    private static final Duration DEFAULT_INTERVAL = Duration.ofMinutes(15);

    public SevenPaySyncProperties {
        initialDays = initialDays > 0 ? initialDays : DEFAULT_INITIAL_DAYS;
        overlapDays = overlapDays > 0 ? overlapDays : DEFAULT_OVERLAP_DAYS;
        interval = interval != null && !interval.isNegative() && !interval.isZero() ? interval : DEFAULT_INTERVAL;
    }
}

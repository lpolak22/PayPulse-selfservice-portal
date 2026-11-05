package com.example.paypulse.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@EnableCaching
@PropertySource("classpath:cache.properties")
public class CacheConfig {

    public static final String DASHBOARD_SUMMARY_CACHE = "dashboard-summary";
}

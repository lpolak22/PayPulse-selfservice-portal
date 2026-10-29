package com.example.paypulse.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@Profile("!seed")
@EnableScheduling
public class SchedulingConfig {
}

package com.example.paypulse.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@EnableConfigurationProperties(PasswordResetProperties.class)
@PropertySource("classpath:password-reset.properties")
public class PasswordResetConfig {
}

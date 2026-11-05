package com.example.paypulse.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@EnableConfigurationProperties(LoginLockoutProperties.class)
@PropertySource("classpath:login-lockout.properties")
public class LoginLockoutConfig {
}

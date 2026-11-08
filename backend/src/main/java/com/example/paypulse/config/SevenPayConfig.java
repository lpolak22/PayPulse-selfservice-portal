package com.example.paypulse.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@EnableConfigurationProperties({SevenPayProperties.class, SevenPayImportProperties.class})
@PropertySource("classpath:sevenpay.properties")
public class SevenPayConfig {
}

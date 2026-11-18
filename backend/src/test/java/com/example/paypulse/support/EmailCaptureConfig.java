package com.example.paypulse.support;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@TestConfiguration
class EmailCaptureConfig {

    @Bean
    @Primary
    CapturingEmailTransport capturingEmailTransport() {
        return new CapturingEmailTransport();
    }
}

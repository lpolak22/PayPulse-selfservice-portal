package com.example.paypulse.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
@Configuration
@PropertySource("classpath:openapi.properties")
public class OpenApiConfig {

    @Bean
    public OpenAPI payPulseOpenApi() {
        return new OpenAPI().info(new Info()
                .title("PayPulse Merchant Portal API")
                .description("REST API for the 7pay Merchant Portal (PayPulse).")
                .version("v1"));
    }
}
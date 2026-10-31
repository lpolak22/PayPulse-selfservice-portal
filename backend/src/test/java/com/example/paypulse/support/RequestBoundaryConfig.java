package com.example.paypulse.support;

import jakarta.persistence.EntityManager;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.MockMvcBuilderCustomizer;
import org.springframework.context.annotation.Bean;

@TestConfiguration
class RequestBoundaryConfig {

    @Bean
    MockMvcBuilderCustomizer freshPersistenceContextPerRequest(EntityManager entityManager) {
        return builder -> builder.alwaysDo(result -> {
            entityManager.flush();
            entityManager.clear();
        });
    }
}

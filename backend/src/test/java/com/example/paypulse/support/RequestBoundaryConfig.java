package com.example.paypulse.support;

import jakarta.persistence.EntityManager;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.MockMvcBuilderCustomizer;
import org.springframework.context.annotation.Bean;

/**
 * API tests run every request inside one rolled-back test transaction, so Hibernate would keep entities cached
 * across requests and hide bulk updates (such as revoking all refresh tokens). Flushing and clearing after
 * each request gives every request a fresh persistence context, as in production.
 */
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

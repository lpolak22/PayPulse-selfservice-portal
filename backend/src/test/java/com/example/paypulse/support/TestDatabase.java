package com.example.paypulse.support;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.postgresql.PostgreSQLContainer;

/**
 * Chooses the PostgreSQL database for the API tests.
 * <ul>
 *     <li>{@code PAYPULSE_TEST_DB_URL} set: that database is used, with {@code PAYPULSE_TEST_DB_USERNAME}
 *     (default {@code postgres}) and {@code PAYPULSE_TEST_DB_PASSWORD}. Tests roll back, so it stays clean.</li>
 *     <li>Otherwise a throwaway PostgreSQL container is started through Docker (Testcontainers), as in CI.</li>
 * </ul>
 */
final class TestDatabase {

    static final String URL_VARIABLE = "PAYPULSE_TEST_DB_URL";
    private static final String IMAGE = "postgres:18-alpine";

    private static PostgreSQLContainer container;

    private TestDatabase() {
    }

    static void register(DynamicPropertyRegistry registry) {
        String externalUrl = System.getenv(URL_VARIABLE);
        if (externalUrl != null && !externalUrl.isBlank()) {
            registry.add("spring.datasource.url", () -> externalUrl);
            registry.add("spring.datasource.username", () -> env("PAYPULSE_TEST_DB_USERNAME", "postgres"));
            registry.add("spring.datasource.password", () -> env("PAYPULSE_TEST_DB_PASSWORD", ""));
            return;
        }
        PostgreSQLContainer postgres = startedContainer();
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    private static synchronized PostgreSQLContainer startedContainer() {
        if (container == null) {
            requireDocker();
            container = new PostgreSQLContainer(IMAGE);
            container.start();
        }
        return container;
    }

    private static void requireDocker() {
        if (!DockerClientFactory.instance().isDockerAvailable()) {
            throw new IllegalStateException("The API tests need PostgreSQL. Start Docker, or point them at a local "
                    + "database with " + URL_VARIABLE + " (see backend/test.ps1).");
        }
    }

    private static String env(String name, String fallback) {
        String value = System.getenv(name);
        return value == null ? fallback : value;
    }
}

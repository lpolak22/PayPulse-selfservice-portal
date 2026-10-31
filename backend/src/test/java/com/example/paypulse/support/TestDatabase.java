package com.example.paypulse.support;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.postgresql.PostgreSQLContainer;

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
            registry.add("spring.datasource.username",
                    () -> env("PAYPULSE_TEST_DB_USERNAME", env("SPRING_DATASOURCE_USERNAME", "postgres")));
            registry.add("spring.datasource.password",
                    () -> env("PAYPULSE_TEST_DB_PASSWORD", env("SPRING_DATASOURCE_PASSWORD", "")));
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
            throw new IllegalStateException("The API tests need PostgreSQL. Start Docker, or set " + URL_VARIABLE
                    + " in backend/.env to a local database of their own.");
        }
    }

    private static String env(String name, String fallback) {
        String value = System.getenv(name);
        return value == null || value.isBlank() ? fallback : value;
    }
}

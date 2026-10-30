package com.example.paypulse.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.env.PropertiesPropertySourceLoader;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.env.MockEnvironment;

import java.io.IOException;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

class JwtPropertiesEnvironmentTest {

    @Test
    void theVariablesSetTheJwtProperties() throws IOException {
        MockEnvironment environment = environmentWithApplicationProperties()
                .withProperty("JWT_SECRET", "a-secret-from-the-environment-0123456789")
                .withProperty("JWT_ISSUER", "another-issuer")
                .withProperty("JWT_ACCESS_TOKEN_TTL", "7m")
                .withProperty("JWT_REFRESH_TOKEN_IDLE_TTL", "9m")
                .withProperty("JWT_CLOCK_SKEW", "5s");

        assertThat(bind(environment)).isEqualTo(new JwtProperties("a-secret-from-the-environment-0123456789",
                "another-issuer", Duration.ofMinutes(7), Duration.ofMinutes(9), Duration.ofSeconds(5)));
    }

    @Test
    void everythingButTheSecretHasADefault() throws IOException {
        MockEnvironment environment = environmentWithApplicationProperties().withProperty("JWT_SECRET", "secret");

        assertThat(bind(environment)).isEqualTo(new JwtProperties("secret", "paypulse", Duration.ofMinutes(15),
                Duration.ofMinutes(30), Duration.ofSeconds(60)));
    }

    private static JwtProperties bind(MockEnvironment environment) {
        return Binder.get(environment).bind("jwt", JwtProperties.class).get();
    }

    private static MockEnvironment environmentWithApplicationProperties() throws IOException {
        PropertySource<?> applicationProperties = new PropertiesPropertySourceLoader()
                .load("application", new ClassPathResource("application.properties")).get(0);
        MockEnvironment environment = new MockEnvironment();
        environment.getPropertySources().addLast(applicationProperties);
        return environment;
    }
}

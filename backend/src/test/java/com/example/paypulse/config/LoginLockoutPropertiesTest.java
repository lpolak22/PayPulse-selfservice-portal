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

class LoginLockoutPropertiesTest {

    @Test
    void defaultsAreFiveAttemptsAndAFifteenMinuteLock() throws IOException {
        assertThat(bind(environmentWithLockoutProperties()))
                .isEqualTo(new LoginLockoutProperties(5, Duration.ofMinutes(15)));
    }

    @Test
    void bothValuesCanBeOverridden() throws IOException {
        MockEnvironment environment = environmentWithLockoutProperties()
                .withProperty("login-lockout.max-attempts", "3")
                .withProperty("login-lockout.lockout-duration", "30m");

        assertThat(bind(environment)).isEqualTo(new LoginLockoutProperties(3, Duration.ofMinutes(30)));
    }

    private static LoginLockoutProperties bind(MockEnvironment environment) {
        return Binder.get(environment).bind("login-lockout", LoginLockoutProperties.class).get();
    }

    private static MockEnvironment environmentWithLockoutProperties() throws IOException {
        PropertySource<?> lockoutProperties = new PropertiesPropertySourceLoader()
                .load("login-lockout", new ClassPathResource("login-lockout.properties")).get(0);
        MockEnvironment environment = new MockEnvironment();
        environment.getPropertySources().addLast(lockoutProperties);
        return environment;
    }
}

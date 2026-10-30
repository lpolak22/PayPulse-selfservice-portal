package com.example.paypulse.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "password-policy")
public record PasswordPolicyProperties(
        int minLength,
        boolean requireUppercase,
        boolean requireLowercase,
        boolean requireDigit
) {
}

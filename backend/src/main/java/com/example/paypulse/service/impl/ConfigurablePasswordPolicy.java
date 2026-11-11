package com.example.paypulse.service.impl;

import com.example.paypulse.config.PasswordPolicyProperties;
import com.example.paypulse.service.PasswordPolicy;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Component
public class ConfigurablePasswordPolicy implements PasswordPolicy {

    private static final int MAX_BYTES = 72;

    private final PasswordPolicyProperties properties;

    public ConfigurablePasswordPolicy(PasswordPolicyProperties properties) {
        this.properties = properties;
    }

    @Override
    public List<String> findViolations(String password) {
        String value = password == null ? "" : password;
        List<String> violations = new ArrayList<>();
        if (value.length() < properties.minLength()) {
            violations.add("Password must have at least " + properties.minLength() + " characters.");
        }
        if (value.getBytes(StandardCharsets.UTF_8).length > MAX_BYTES) {
            violations.add("Password must not be longer than " + MAX_BYTES + " bytes.");
        }
        if (properties.requireUppercase() && value.chars().noneMatch(Character::isUpperCase)) {
            violations.add("Password must contain an uppercase letter.");
        }
        if (properties.requireLowercase() && value.chars().noneMatch(Character::isLowerCase)) {
            violations.add("Password must contain a lowercase letter.");
        }
        if (properties.requireDigit() && value.chars().noneMatch(Character::isDigit)) {
            violations.add("Password must contain a digit.");
        }
        return violations;
    }
}

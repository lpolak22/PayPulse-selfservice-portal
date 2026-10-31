package com.example.paypulse.service.impl;

import com.example.paypulse.config.PasswordPolicyProperties;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ConfigurablePasswordPolicyTest {

    private final ConfigurablePasswordPolicy policy =
            new ConfigurablePasswordPolicy(new PasswordPolicyProperties(8, true, true, true));

    @Test
    void aPasswordThatMeetsEveryRuleHasNoViolations() {
        assertThat(policy.findViolations("Abcdefg1")).isEmpty();
    }

    @Test
    void everyBrokenRuleIsReported() {
        assertThat(policy.findViolations("ab")).containsExactly(
                "Password must have at least 8 characters.",
                "Password must contain an uppercase letter.",
                "Password must contain a digit.");
    }

    @Test
    void aMissingPasswordBreaksTheRulesInsteadOfFailing() {
        assertThat(policy.findViolations(null)).hasSize(4);
    }

    @Test
    void theRulesCanBeSwitchedOff() {
        ConfigurablePasswordPolicy relaxed =
                new ConfigurablePasswordPolicy(new PasswordPolicyProperties(1, false, false, false));

        assertThat(relaxed.findViolations("a")).isEmpty();
    }

    @Test
    void seventyTwoBytesIsTheLongestPasswordAccepted() {
        assertThat(policy.findViolations("Aa1" + "x".repeat(69))).isEmpty();
        assertThat(policy.findViolations("Aa1" + "x".repeat(70)))
                .containsExactly("Password must not be longer than 72 bytes.");
    }

    @Test
    void theLengthLimitCountsBytesNotCharacters() {
        assertThat(policy.findViolations("Aa1" + "š".repeat(35)))
                .containsExactly("Password must not be longer than 72 bytes.");
    }

    @Test
    void theByteLimitCannotBeRaisedByConfiguration() {
        ConfigurablePasswordPolicy relaxed =
                new ConfigurablePasswordPolicy(new PasswordPolicyProperties(1, false, false, false));

        assertThat(relaxed.findViolations("x".repeat(73))).hasSize(1);
    }
}

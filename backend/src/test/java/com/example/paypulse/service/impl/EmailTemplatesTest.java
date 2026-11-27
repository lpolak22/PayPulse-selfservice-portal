package com.example.paypulse.service.impl;

import com.example.paypulse.config.EmailConfig;
import com.example.paypulse.dto.email.RenderedEmail;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class EmailTemplatesTest {

    private static final String FOOTER = "This is an automated message. Please do not reply to this email.";

    private final ThymeleafEmailTemplateRenderer renderer =
            new ThymeleafEmailTemplateRenderer(new EmailConfig().emailTemplateEngine());

    @Test
    void theActivationEmailUsesTheBaseLayoutAndItsVariables() {
        String link = "paypulse://activate?token=abc.def";
        RenderedEmail email = renderer.render("activation",
                Map.of("firstName", "Ana", "link", link, "validHours", 72L));

        assertThat(email.subject()).isEqualTo("Set your PayPulse password");
        assertThat(email.html())
                .contains("<title>Set your PayPulse password</title>", FOOTER, "Hi Ana,", ">Set password</a>")
                .contains("href=\"" + link + "\"")
                .contains("valid for 72 hours");
        assertThat(email.text())
                .contains("Hi Ana,", link, "valid for 72 hours", FOOTER);
    }

    @Test
    void thePasswordResetEmailUsesTheBaseLayoutAndItsVariables() {
        String link = "paypulse://reset-password?token=abc.def";
        RenderedEmail email = renderer.render("password-reset",
                Map.of("firstName", "Ana", "link", link, "validMinutes", 60L));

        assertThat(email.subject()).isEqualTo("Reset your PayPulse password");
        assertThat(email.html())
                .contains("<title>Reset your PayPulse password</title>", FOOTER, "Hi Ana,", ">Reset password</a>")
                .contains("href=\"" + link + "\"")
                .contains("valid for 60 minutes")
                .contains("If you did not ask for this");
        assertThat(email.text())
                .contains("Hi Ana,", link, "valid for 60 minutes", "If you did not ask for this", FOOTER);
    }
}

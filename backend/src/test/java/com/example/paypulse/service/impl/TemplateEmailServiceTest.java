package com.example.paypulse.service.impl;

import com.example.paypulse.config.EmailConfig;
import com.example.paypulse.dto.email.OutgoingEmail;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TemplateEmailServiceTest {

    private final List<Object> events = new ArrayList<>();
    private final TemplateEmailService service = new TemplateEmailService(
            new ThymeleafEmailTemplateRenderer(new EmailConfig().emailTemplateEngine()), events::add);

    @Test
    void theRenderedEmailIsHandedOverForDelivery() {
        service.send("ana@example.test", "layout-sample", Map.of("name", "Ana", "link", "https://example.test"));

        assertThat(events).singleElement().isInstanceOfSatisfying(OutgoingEmail.class, email -> {
            assertThat(email.to()).isEqualTo("ana@example.test");
            assertThat(email.templateId()).isEqualTo("layout-sample");
            assertThat(email.subject()).isEqualTo("Hello Ana");
            assertThat(email.html()).contains("Hi Ana,");
            assertThat(email.text()).contains("Open PayPulse: https://example.test");
        });
    }

    @Test
    void anUnknownTemplateFailsImmediatelyAndNothingIsSent() {
        assertThatThrownBy(() -> service.send("ana@example.test", "does-not-exist", Map.of()))
                .isInstanceOf(RuntimeException.class);
        assertThat(events).isEmpty();
    }
}

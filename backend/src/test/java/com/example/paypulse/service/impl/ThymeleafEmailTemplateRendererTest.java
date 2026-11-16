package com.example.paypulse.service.impl;

import com.example.paypulse.config.EmailConfig;
import com.example.paypulse.dto.email.RenderedEmail;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ThymeleafEmailTemplateRendererTest {

    private static final String LINK = "https://example.test/open?token=abc&x=1";

    private final ThymeleafEmailTemplateRenderer renderer =
            new ThymeleafEmailTemplateRenderer(new EmailConfig().emailTemplateEngine());

    private final RenderedEmail email =
            renderer.render("layout-sample", Map.of("name", "Ana <script>", "link", LINK));

    @Test
    void theSubjectComesFromTheTemplateWithoutTrailingWhitespace() {
        assertThat(email.subject()).isEqualTo("Hello Ana <script>");
    }

    @Test
    void theHtmlBodyIsWrappedInTheBaseLayoutWithLogoAndFooter() {
        assertThat(email.html())
                .startsWith("<!DOCTYPE html>")
                .contains("<title>Hello Ana &lt;script&gt;</title>")
                .contains("7pay")
                .contains("This is an automated message. Please do not reply to this email.")
                .contains("Hi Ana &lt;script&gt;,")
                .contains("href=\"https://example.test/open?token=abc&amp;x=1\"")
                .contains(">Open PayPulse</a>")
                .doesNotContain("<script>");
    }

    @Test
    void thePlainTextVersionUsesTheTextLayout() {
        assertThat(email.text())
                .startsWith("7pay | PayPulse")
                .contains("Hi Ana <script>,")
                .contains("Open PayPulse: " + LINK)
                .contains("This is an automated message. Please do not reply to this email.")
                .doesNotContain("<p");
    }

    @Test
    void templateIdsThatCouldLeaveTheTemplateFolderAreRejected() {
        assertThatThrownBy(() -> renderer.render("../application", Map.of()))
                .isInstanceOf(IllegalArgumentException.class);
    }
}

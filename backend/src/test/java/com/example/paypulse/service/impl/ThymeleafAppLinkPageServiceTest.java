package com.example.paypulse.service.impl;

import com.example.paypulse.config.AppLinkConfig;
import com.example.paypulse.web.AppLinkTarget;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class ThymeleafAppLinkPageServiceTest {

    private static final String TOKEN = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIn0.sig-_x";

    private final ThymeleafAppLinkPageService service =
            new ThymeleafAppLinkPageService(new AppLinkConfig().pageTemplateEngine());

    @Test
    void theActivationPageOpensTheAppWithTheToken() {
        assertThat(service.render(AppLinkTarget.ACTIVATE, TOKEN))
                .contains("<title>Set your PayPulse password</title>")
                .contains("href=\"paypulse://activate?token=" + TOKEN + "\"")
                .contains("Open in the PayPulse app");
    }

    @Test
    void theResetPageOpensTheResetScreen() {
        assertThat(service.render(AppLinkTarget.RESET_PASSWORD, TOKEN))
                .contains("<title>Reset your PayPulse password</title>")
                .contains("href=\"paypulse://reset-password?token=" + TOKEN + "\"");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"<script>alert(1)</script>", "a b", "abc\"onclick=\"x", "abc&next=evil"})
    void aMalformedTokenShowsTheInvalidLinkMessageWithoutAnAppLink(String token) {
        String page = service.render(AppLinkTarget.ACTIVATE, token);

        assertThat(service.isWellFormedToken(token)).isFalse();
        assertThat(page)
                .contains("This link is not valid.")
                .doesNotContain("paypulse://")
                .doesNotContain("<script>alert");
    }

    @Test
    void targetsAreLookedUpByTheirPath() {
        assertThat(AppLinkTarget.fromPath("activate")).contains(AppLinkTarget.ACTIVATE);
        assertThat(AppLinkTarget.fromPath("reset-password")).contains(AppLinkTarget.RESET_PASSWORD);
        assertThat(AppLinkTarget.fromPath("login")).isEmpty();
    }
}

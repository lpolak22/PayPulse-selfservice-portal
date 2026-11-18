package com.example.paypulse.api;

import com.example.paypulse.dto.email.OutgoingEmail;
import com.example.paypulse.model.UserRole;
import com.example.paypulse.model.UserStatus;
import com.example.paypulse.support.ApiIntegrationTest;
import com.example.paypulse.support.UserFixtures;
import com.example.paypulse.support.UserFixtures.TestUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RecordApplicationEvents
class EmailSendingApiTest extends ApiIntegrationTest {

    private static final Pattern LINK = Pattern.compile("https?://\\S+/open/[a-z-]+\\?token=[A-Za-z0-9_.~-]+");

    @Autowired
    private ApplicationEvents events;

    private long organisationId;
    private TestUser admin;

    @BeforeEach
    void setUp() {
        organisationId = fixtures.organisation();
        admin = fixtures.user(organisationId, UserRole.ADMIN, UserStatus.ACTIVE);
    }

    @Test
    void creatingAUserSendsAnActivationEmailWhoseLinkOpensTheApp() throws Exception {
        String email = UserFixtures.uniqueEmail();

        performAs(admin, withJson(post("/api/users"),
                Map.of("firstName", "Iva", "lastName", "Babic", "email", email, "role", "USER")))
                .andExpect(status().isCreated());

        OutgoingEmail sent = onlyEmailSent();
        assertThat(sent.to()).isEqualTo(email);
        assertThat(sent.templateId()).isEqualTo("activation");
        assertThat(sent.text()).contains("Hi Iva,");
        expectLinkOpensTheApp(sent, "activate");
    }

    @Test
    void resendingTheActivationSendsANewActivationEmail() throws Exception {
        TestUser pending = fixtures.user(organisationId, UserRole.USER, UserStatus.PENDING_ACTIVATION);

        performAs(admin, post("/api/users/{id}/resend-activation", pending.id()))
                .andExpect(status().isNoContent());

        OutgoingEmail sent = onlyEmailSent();
        assertThat(sent.to()).isEqualTo(pending.email());
        assertThat(sent.templateId()).isEqualTo("activation");
        expectLinkOpensTheApp(sent, "activate");
    }

    @Test
    void requestingAPasswordResetSendsAResetEmailWhoseLinkOpensTheApp() throws Exception {
        TestUser active = fixtures.user(organisationId, UserRole.USER, UserStatus.ACTIVE);

        mockMvc.perform(withJson(post("/api/auth/password-reset/request"), Map.of("email", active.email())))
                .andExpect(status().isAccepted());

        OutgoingEmail sent = onlyEmailSent();
        assertThat(sent.to()).isEqualTo(active.email());
        assertThat(sent.templateId()).isEqualTo("password-reset");
        expectLinkOpensTheApp(sent, "reset-password");
    }

    @Test
    void aPasswordResetForAnUnknownAddressSendsNothing() throws Exception {
        mockMvc.perform(withJson(post("/api/auth/password-reset/request"),
                        Map.of("email", UserFixtures.uniqueEmail())))
                .andExpect(status().isAccepted());

        assertThat(events.stream(OutgoingEmail.class)).isEmpty();
    }

    private OutgoingEmail onlyEmailSent() {
        List<OutgoingEmail> sent = events.stream(OutgoingEmail.class).toList();
        assertThat(sent).hasSize(1);
        return sent.getFirst();
    }

    private void expectLinkOpensTheApp(OutgoingEmail email, String target) throws Exception {
        String link = linkIn(email.text());
        assertThat(link).contains("/open/" + target + "?token=");
        assertThat(email.html()).contains("href=\"" + link + "\"");

        String token = link.substring(link.indexOf("?token=") + "?token=".length());
        mockMvc.perform(get(URI.create(link)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("paypulse://" + target + "?token=" + token)));
    }

    private static String linkIn(String text) {
        Matcher matcher = LINK.matcher(text);
        assertThat(matcher.find()).as("a link in:%n%s", text).isTrue();
        return matcher.group();
    }
}

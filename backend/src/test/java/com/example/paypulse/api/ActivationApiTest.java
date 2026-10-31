package com.example.paypulse.api;

import com.example.paypulse.dto.AppUserResponse;
import com.example.paypulse.dto.auth.TokenResponse;
import com.example.paypulse.model.UserRole;
import com.example.paypulse.model.UserStatus;
import com.example.paypulse.repository.AppUserRepository;
import com.example.paypulse.service.ActivationTokenService;
import com.example.paypulse.support.ApiIntegrationTest;
import com.example.paypulse.support.UserFixtures;
import com.example.paypulse.support.UserFixtures.TestUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.test.web.servlet.ResultActions;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ActivationApiTest extends ApiIntegrationTest {

    private static final String NEW_PASSWORD = "New-Password-1";

    @Autowired
    private ActivationTokenService activationTokenService;

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private JwtEncoder jwtEncoder;

    private long organisationId;
    private TestUser admin;
    private TestUser pending;

    @BeforeEach
    void setUp() {
        organisationId = fixtures.organisation();
        admin = fixtures.user(organisationId, UserRole.ADMIN, UserStatus.ACTIVE);
        pending = fixtures.user(organisationId, UserRole.USER, UserStatus.PENDING_ACTIVATION);
    }

    @Test
    void aUserCreatedByAnAdminActivatesAndLogsIn() throws Exception {
        String body = performAs(admin, withJson(post("/api/users"), Map.of("firstName", "Iva", "lastName", "Babic",
                "email", UserFixtures.uniqueEmail(), "role", "USER")))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        AppUserResponse created = jsonMapper.readValue(body, AppUserResponse.class);
        TestUser user = new TestUser(created.id(), created.email());
        postLogin(user.email(), NEW_PASSWORD).andExpect(status().isUnauthorized());

        activate(linkFor(user), NEW_PASSWORD).andExpect(status().isNoContent());

        TokenResponse tokens = refreshed(postLogin(user.email(), NEW_PASSWORD));
        performWithToken(tokens.accessToken(), get("/api/auth/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void aFreshLinkIsValid() throws Exception {
        expectLinkStatus(linkFor(pending), "VALID");
    }

    @Test
    void activatingSetsThePasswordAndTheStatus() throws Exception {
        activate(linkFor(pending), NEW_PASSWORD).andExpect(status().isNoContent());

        assertThat(fixtures.row(pending.id()))
                .containsEntry("status", "ACTIVE")
                .containsEntry("is_active", true)
                .containsEntry("is_first_login", false);
        assertThat((String) fixtures.row(pending.id()).get("password_hash")).isNotBlank().isNotEqualTo(NEW_PASSWORD);
    }

    @Test
    void activatingIsAudited() throws Exception {
        activate(linkFor(pending), NEW_PASSWORD).andExpect(status().isNoContent());

        assertThat(jdbc.queryForList("select id_user from audit_log where action = 'USER_ACTIVATED' and entity_id = ?",
                Long.class, pending.id())).containsExactly(pending.id());
    }

    @Test
    void activatingStartsNoSession() throws Exception {
        activate(linkFor(pending), NEW_PASSWORD).andExpect(status().isNoContent());

        assertThat(openRefreshTokensOf(pending)).isZero();
    }

    @Test
    void aUsedLinkIsReportedAsUsedAndCannotBeUsedAgain() throws Exception {
        String link = linkFor(pending);
        activate(link, NEW_PASSWORD).andExpect(status().isNoContent());

        expectLinkStatus(link, "USED");
        expectError(activate(link, "Other-Password-1"), 410, "ACTIVATION_TOKEN_USED");

        postLogin(pending.email(), NEW_PASSWORD).andExpect(status().isOk());
    }

    @Test
    void anExpiredLinkIsReportedAsExpiredAndCannotBeUsed() throws Exception {
        String link = expiredLinkFor(pending);

        expectLinkStatus(link, "EXPIRED");
        expectError(activate(link, NEW_PASSWORD), 410, "ACTIVATION_TOKEN_EXPIRED");

        assertThat(fixtures.row(pending.id())).containsEntry("status", "PENDING_ACTIVATION");
    }

    @Test
    void resendingReplacesTheOlderLink() throws Exception {
        String older = linkFor(pending);

        performAs(admin, post("/api/users/{id}/resend-activation", pending.id())).andExpect(status().isNoContent());

        expectLinkStatus(older, "USED");
        expectError(activate(older, NEW_PASSWORD), 410, "ACTIVATION_TOKEN_USED");
    }

    @Test
    void aLinkIssuedAfterAResendWorks() throws Exception {
        performAs(admin, post("/api/users/{id}/resend-activation", pending.id())).andExpect(status().isNoContent());

        activate(linkFor(pending), NEW_PASSWORD).andExpect(status().isNoContent());
    }

    @Test
    void anActiveUserHasNoUsableLink() throws Exception {
        expectError(activate(linkFor(admin), NEW_PASSWORD), 410, "ACTIVATION_TOKEN_USED");

        postLogin(admin.email(), UserFixtures.PASSWORD).andExpect(status().isOk());
    }

    @Test
    void theLinkOfADeletedUserIsInvalid() throws Exception {
        fixtures.markDeleted(pending);
        String link = linkFor(pending);

        expectError(mockMvc.perform(get("/api/auth/activation/{token}", link)), 400, "ACTIVATION_TOKEN_INVALID");
        expectError(activate(link, NEW_PASSWORD), 400, "ACTIVATION_TOKEN_INVALID");
    }

    @Test
    void aLinkThatIsNoTokenIsInvalid() throws Exception {
        expectError(mockMvc.perform(get("/api/auth/activation/not-a-token")), 400, "ACTIVATION_TOKEN_INVALID");
        expectError(activate("not-a-token", NEW_PASSWORD), 400, "ACTIVATION_TOKEN_INVALID");
    }

    @Test
    void anAccessTokenIsNotAnActivationLink() throws Exception {
        String accessToken = accessToken(pending);

        expectError(mockMvc.perform(get("/api/auth/activation/{token}", accessToken)), 400,
                "ACTIVATION_TOKEN_INVALID");
        expectError(activate(accessToken, NEW_PASSWORD), 400, "ACTIVATION_TOKEN_INVALID");
    }

    @Test
    void aTamperedLinkIsInvalid() throws Exception {
        String[] parts = linkFor(pending).split("\\.");

        expectError(activate(parts[0] + "." + parts[1] + ".AAAA" + parts[2].substring(4), NEW_PASSWORD), 400,
                "ACTIVATION_TOKEN_INVALID");
    }

    @Test
    void aPasswordThatBreaksThePolicyIsRejectedAndTheLinkStaysValid() throws Exception {
        String link = linkFor(pending);

        expectError(activate(link, "lowercase-only"), 400, "VALIDATION_ERROR")
                .andExpect(jsonPath("$.fieldErrors[0].field").value("password"));

        assertThat(fixtures.row(pending.id())).containsEntry("status", "PENDING_ACTIVATION");
        expectLinkStatus(link, "VALID");
    }

    @Test
    void mismatchedPasswordsAreRejected() throws Exception {
        expectError(activate(linkFor(pending), NEW_PASSWORD, "Other-Password-1"), 400, "VALIDATION_ERROR")
                .andExpect(jsonPath("$.fieldErrors[0].field").value("confirmPassword"));

        assertThat(fixtures.row(pending.id())).containsEntry("status", "PENDING_ACTIVATION");
    }

    static Stream<String> passwordsBCryptCannotHash() {
        return Stream.of("Aa1" + "x".repeat(70), "Aa1" + "š".repeat(35), "Aa1" + "x".repeat(10_000));
    }

    @ParameterizedTest
    @MethodSource("passwordsBCryptCannotHash")
    void aPasswordOverTheBCryptLimitIsAValidationError(String password) throws Exception {
        expectError(activate(linkFor(pending), password), 400, "VALIDATION_ERROR")
                .andExpect(jsonPath("$.fieldErrors[0].field").value("password"));

        assertThat(fixtures.row(pending.id())).containsEntry("status", "PENDING_ACTIVATION");
    }

    @Test
    void aPasswordOfExactlyTheBCryptLimitWorks() throws Exception {
        String password = "Aa1" + "x".repeat(69);

        activate(linkFor(pending), password).andExpect(status().isNoContent());

        postLogin(pending.email(), password).andExpect(status().isOk());
    }

    @ParameterizedTest
    @CsvSource({"token", "password", "confirmPassword"})
    void aMissingFieldReturns400WithTheFieldNamed(String field) throws Exception {
        Map<String, String> body = new HashMap<>(activationBody(linkFor(pending), NEW_PASSWORD, NEW_PASSWORD));
        body.remove(field);

        expectError(mockMvc.perform(withJson(post("/api/auth/activation"), body)), 400, "VALIDATION_ERROR")
                .andExpect(jsonPath("$.fieldErrors[0].field").value(field));
    }

    @Test
    void malformedJsonReturns400() throws Exception {
        expectError(mockMvc.perform(post("/api/auth/activation").contentType(MediaType.APPLICATION_JSON)
                .content("{not json")), 400, "BAD_REQUEST");
    }

    private ResultActions activate(String link, String password) throws Exception {
        return activate(link, password, password);
    }

    private ResultActions activate(String link, String password, String confirmPassword) throws Exception {
        return mockMvc.perform(withJson(post("/api/auth/activation"),
                activationBody(link, password, confirmPassword)));
    }

    private static Map<String, String> activationBody(String link, String password, String confirmPassword) {
        return Map.of("token", link, "password", password, "confirmPassword", confirmPassword);
    }

    private void expectLinkStatus(String link, String expected) throws Exception {
        mockMvc.perform(get("/api/auth/activation/{token}", link))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(expected));
    }

    private static ResultActions expectError(ResultActions result, int status, String code) throws Exception {
        return result.andExpect(status().is(status)).andExpect(jsonPath("$.code").value(code));
    }

    private String linkFor(TestUser user) {
        return activationTokenService.issue(appUserRepository.findById(user.id()).orElseThrow());
    }

    private String expiredLinkFor(TestUser user) {
        Instant expiredAt = Instant.now().minusSeconds(1);
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(jwtProperties.issuer() + "-activation")
                .subject(String.valueOf(user.id()))
                .issuedAt(expiredAt.minus(Duration.ofHours(72)))
                .expiresAt(expiredAt)
                .claim("type", "ACTIVATION")
                .claim("updatedAt", appUserRepository.findUpdatedAt(user.id()).orElseThrow().toString())
                .build();
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }
}

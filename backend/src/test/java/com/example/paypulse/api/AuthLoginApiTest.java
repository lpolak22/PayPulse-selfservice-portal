package com.example.paypulse.api;

import com.example.paypulse.dto.auth.TokenResponse;
import com.example.paypulse.model.UserRole;
import com.example.paypulse.model.UserStatus;
import com.example.paypulse.support.ApiIntegrationTest;
import com.example.paypulse.support.UserFixtures;
import com.example.paypulse.support.UserFixtures.TestUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.web.servlet.ResultActions;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthLoginApiTest extends ApiIntegrationTest {

    @Autowired
    private JwtDecoder jwtDecoder;

    private long organisationId;
    private TestUser admin;

    @BeforeEach
    void setUp() {
        organisationId = fixtures.organisation();
        admin = fixtures.user(organisationId, UserRole.ADMIN, UserStatus.ACTIVE);
    }

    @Test
    void validCredentialsReturnABearerTokenPair() throws Exception {
        postLogin(admin.email(), UserFixtures.PASSWORD)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresIn").value(jwtProperties.accessTokenTtl().toSeconds()))
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty());
    }

    @Test
    void theAccessTokenCarriesTheConfiguredIssuerAndLifetime() throws Exception {
        Jwt jwt = jwtDecoder.decode(login(admin).accessToken());

        assertThat(jwt.getSubject()).isEqualTo(String.valueOf(admin.id()));
        assertThat(jwt.getClaimAsString("iss")).isEqualTo(jwtProperties.issuer());
        assertThat(jwt.getClaimAsString("role")).isEqualTo("ADMIN");
        assertThat(Duration.between(jwt.getIssuedAt(), jwt.getExpiresAt())).isEqualTo(jwtProperties.accessTokenTtl());
    }

    @Test
    void theIssuedAccessTokenSignsTheUserIn() throws Exception {
        TokenResponse tokens = login(admin);

        performWithToken(tokens.accessToken(), get("/api/auth/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(admin.id()))
                .andExpect(jsonPath("$.email").value(admin.email()));
        performWithToken(tokens.accessToken(), get("/api/users")).andExpect(status().isOk());
    }

    @Test
    void emailIsCaseInsensitiveAndTrimmed() throws Exception {
        postLogin("  " + admin.email().toUpperCase(Locale.ROOT) + " ", UserFixtures.PASSWORD)
                .andExpect(status().isOk());
    }

    @Test
    void wrongPasswordAndUnknownEmailGetTheSameAnswer() throws Exception {
        String wrongPassword = expectError(postLogin(admin.email(), "Wrong-Password-1"), 401, "INVALID_CREDENTIALS");
        String unknownEmail = expectError(postLogin(UserFixtures.uniqueEmail(), "x"), 401, "INVALID_CREDENTIALS");

        assertThat(unknownEmail).isEqualTo(wrongPassword);
    }

    @Test
    void passwordIsCaseSensitive() throws Exception {
        expectError(postLogin(admin.email(), UserFixtures.PASSWORD.toUpperCase(Locale.ROOT)), 401,
                "INVALID_CREDENTIALS");
    }

    @Test
    void passwordIsNotTrimmed() throws Exception {
        expectError(postLogin(admin.email(), " " + UserFixtures.PASSWORD + " "), 401, "INVALID_CREDENTIALS");
    }

    @Test
    void aVeryLongPasswordIsJustAWrongPassword() throws Exception {
        expectError(postLogin(admin.email(), "x".repeat(10_000)), 401, "INVALID_CREDENTIALS");
    }

    @Test
    void aNulCharacterInTheEmailReturns400WithTheFieldNamed() throws Exception {
        postLogin("user\u0000@paypulse-test.example", UserFixtures.PASSWORD)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.fieldErrors[0].field").value("email"));
    }

    @Test
    void aNulCharacterInThePasswordIsJustAWrongPassword() throws Exception {
        expectError(postLogin(admin.email(), "Test\u0000Password"), 401, "INVALID_CREDENTIALS");
    }

    @Test
    void thePasswordHashIsNeverReturned() throws Exception {
        String hash = (String) fixtures.row(admin.id()).get("password_hash");

        String me = performWithToken(login(admin).accessToken(), get("/api/auth/me"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertThat(me).doesNotContain(hash).doesNotContainIgnoringCase("password");
    }

    @Test
    void wrongPasswordIssuesNoTokens() throws Exception {
        postLogin(admin.email(), "Wrong-Password-1").andExpect(status().isUnauthorized());

        assertThat(refreshTokensOf(admin)).isZero();
    }

    @Test
    void deletedUserCannotLogIn() throws Exception {
        fixtures.markDeleted(admin);

        expectError(postLogin(admin.email(), UserFixtures.PASSWORD), 401, "INVALID_CREDENTIALS");
    }

    @Test
    void pendingUserWithoutPasswordCannotLogIn() throws Exception {
        TestUser pending = fixtures.user(organisationId, UserRole.USER, UserStatus.PENDING_ACTIVATION);

        expectError(postLogin(pending.email(), UserFixtures.PASSWORD), 401, "INVALID_CREDENTIALS");
    }

    @Test
    void pendingUserWithPasswordIsToldToActivate() throws Exception {
        TestUser pending = fixtures.user(organisationId, UserRole.USER, UserStatus.PENDING_ACTIVATION);
        fixtures.givePassword(pending);

        expectError(postLogin(pending.email(), UserFixtures.PASSWORD), 403, "ACCOUNT_PENDING_ACTIVATION");
    }

    @Test
    void pendingUserWithAWrongPasswordLearnsNothingAboutTheStatus() throws Exception {
        TestUser pending = fixtures.user(organisationId, UserRole.USER, UserStatus.PENDING_ACTIVATION);
        fixtures.givePassword(pending);

        expectError(postLogin(pending.email(), "Wrong-Password-1"), 401, "INVALID_CREDENTIALS");
    }

    @Test
    void deactivatedUserIsToldTheAccountIsInactive() throws Exception {
        TestUser inactive = fixtures.user(organisationId, UserRole.ADMIN, UserStatus.INACTIVE);

        expectError(postLogin(inactive.email(), UserFixtures.PASSWORD), 403, "ACCOUNT_INACTIVE");
    }

    @Test
    void deactivatedUserWithAWrongPasswordLearnsNothingAboutTheStatus() throws Exception {
        TestUser inactive = fixtures.user(organisationId, UserRole.ADMIN, UserStatus.INACTIVE);

        expectError(postLogin(inactive.email(), "Wrong-Password-1"), 401, "INVALID_CREDENTIALS");
    }

    @ParameterizedTest
    @EnumSource(value = UserStatus.class, names = {"INACTIVE", "PENDING_ACTIVATION"})
    void aUserWhoCannotLogInGetsNoTokens(UserStatus status) throws Exception {
        TestUser blocked = fixtures.user(organisationId, UserRole.USER, status);
        fixtures.givePassword(blocked);

        postLogin(blocked.email(), UserFixtures.PASSWORD).andExpect(status().isForbidden());

        assertThat(refreshTokensOf(blocked)).isZero();
    }

    @ParameterizedTest(name = "email \"{0}\", password \"{1}\"")
    @CsvSource({"'', secret", "'   ', secret", "ana@example.com, ''"})
    void blankFieldsReturn400(String email, String password) throws Exception {
        postLogin(email, password)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    void missingPasswordReturns400WithTheFieldNamed() throws Exception {
        mockMvc.perform(withJson(post("/api/auth/login"), Map.of("email", admin.email())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.fieldErrors[0].field").value("password"));
        assertThat(refreshTokensOf(admin)).isZero();
    }

    @Test
    void missingEmailReturns400WithTheFieldNamed() throws Exception {
        mockMvc.perform(withJson(post("/api/auth/login"), Map.of("password", UserFixtures.PASSWORD)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.fieldErrors[0].field").value("email"));
    }

    @Test
    void malformedJsonReturns400() throws Exception {
        mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content("{\"email\": "))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("BAD_REQUEST"));
    }

    @Test
    void invalidRequestsAreNotAuditedAsFailedLogins() throws Exception {
        postLogin(admin.email(), "").andExpect(status().isBadRequest());

        assertThat(auditEntries("LOGIN_FAILED", admin)).isEmpty();
    }

    @Test
    void theRefreshTokenIsStoredHashedWithTheConfiguredIdleTimeout() throws Exception {
        Duration idleTtl = jwtProperties.refreshTokenIdleTtl();
        LocalDateTime before = LocalDateTime.now().withNano(0);
        TokenResponse tokens = login(admin);

        Map<String, Object> row = jdbc.queryForMap(
                "select token_hash, expires_at, used_at from refresh_token where id_user = ?", admin.id());
        assertThat(row.get("token_hash")).isEqualTo(sha256(tokens.refreshToken()));
        assertThat(row.get("used_at")).isNull();
        LocalDateTime expiresAt = ((java.sql.Timestamp) row.get("expires_at")).toLocalDateTime();
        assertThat(expiresAt).isBetween(before.plus(idleTtl), LocalDateTime.now().plus(idleTtl));
    }

    @Test
    void successfulLoginIsAudited() throws Exception {
        login(admin);

        assertThat(auditEntries("LOGIN_SUCCESS", admin)).singleElement()
                .satisfies(entry -> assertThat(entry).containsEntry("id_user", admin.id())
                        .containsEntry("entity_id", admin.id()));
    }

    @Test
    void failedLoginIsAuditedWithTheReason() throws Exception {
        postLogin(admin.email(), "Wrong-Password-1").andExpect(status().isUnauthorized());

        assertThat(auditEntries("LOGIN_FAILED", admin)).singleElement()
                .satisfies(entry -> assertThat(entry).containsEntry("id_user", admin.id())
                        .containsEntry("reason", "INVALID_CREDENTIALS"));
    }

    @ParameterizedTest
    @CsvSource({"INACTIVE, ACCOUNT_INACTIVE", "PENDING_ACTIVATION, ACCOUNT_PENDING_ACTIVATION"})
    void blockedLoginIsAuditedWithTheReason(UserStatus status, String reason) throws Exception {
        TestUser blocked = fixtures.user(organisationId, UserRole.USER, status);
        fixtures.givePassword(blocked);

        postLogin(blocked.email(), UserFixtures.PASSWORD).andExpect(status().isForbidden());

        assertThat(auditEntries("LOGIN_FAILED", blocked)).singleElement()
                .satisfies(entry -> assertThat(entry).containsEntry("id_user", blocked.id())
                        .containsEntry("reason", reason));
    }

    @Test
    void loginWithAnUnknownEmailIsAuditedWithoutAUser() throws Exception {
        String email = UserFixtures.uniqueEmail();
        postLogin(email, "x").andExpect(status().isUnauthorized());

        List<Map<String, Object>> entries = jdbc.queryForList("""
                select id_user, new_value ->> 'reason' as reason from audit_log
                where action = 'LOGIN_FAILED' and new_value ->> 'email' = ?""", email);
        assertThat(entries).singleElement().satisfies(entry -> assertThat(entry)
                .containsEntry("id_user", null).containsEntry("reason", "INVALID_CREDENTIALS"));
    }

    private List<Map<String, Object>> auditEntries(String action, TestUser user) {
        return jdbc.queryForList("""
                select id_user, entity_id, new_value ->> 'reason' as reason from audit_log
                where action = ? and entity_id = ?""", action, user.id());
    }

    private long refreshTokensOf(TestUser user) {
        return jdbc.queryForObject("select count(*) from refresh_token where id_user = ?", Long.class, user.id());
    }

    private static String expectError(ResultActions result, int status, String code) throws Exception {
        return result.andExpect(status().is(status))
                .andExpect(jsonPath("$.code").value(code))
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
    }

    private static String sha256(String value) throws Exception {
        byte[] digest = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
        return HexFormat.of().formatHex(digest);
    }
}

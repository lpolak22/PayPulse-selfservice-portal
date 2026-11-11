package com.example.paypulse.api;

import com.example.paypulse.config.JwtConfig;
import com.example.paypulse.config.JwtProperties;
import com.example.paypulse.model.UserRole;
import com.example.paypulse.model.UserStatus;
import com.example.paypulse.repository.AppUserRepository;
import com.example.paypulse.service.ActivationTokenService;
import com.example.paypulse.support.ApiIntegrationTest;
import com.example.paypulse.support.UserFixtures.TestUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.test.web.servlet.ResultActions;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AccessTokenApiTest extends ApiIntegrationTest {

    @Autowired
    private JwtEncoder jwtEncoder;

    @Autowired
    private ActivationTokenService activationTokenService;

    @Autowired
    private AppUserRepository appUserRepository;

    private long organisationId;
    private TestUser admin;
    private TestUser user;

    @BeforeEach
    void setUp() {
        organisationId = fixtures.organisation();
        admin = fixtures.user(organisationId, UserRole.ADMIN, UserStatus.ACTIVE);
        user = fixtures.user(organisationId, "Iva", "Babic", UserRole.USER, UserStatus.ACTIVE);
    }

    @Test
    void meReturnsTheSignedInUser() throws Exception {
        performWithToken(login(user).accessToken(), get("/api/auth/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(user.id()))
                .andExpect(jsonPath("$.firstName").value("Iva"))
                .andExpect(jsonPath("$.role").value("USER"))
                .andExpect(jsonPath("$.organisationId").value(organisationId));
    }

    @Test
    void requestWithoutTokenIsUnauthorized() throws Exception {
        expectUnauthorized(mockMvc.perform(get("/api/auth/me")));
    }

    @Test
    void basicCredentialsAreNoLongerAccepted() throws Exception {
        String basic = Base64.getEncoder().encodeToString((user.email() + ":x").getBytes(StandardCharsets.UTF_8));

        expectUnauthorized(mockMvc.perform(get("/api/auth/me").header(HttpHeaders.AUTHORIZATION, "Basic " + basic)));
    }

    @Test
    void malformedTokenIsUnauthorized() throws Exception {
        expectUnauthorized(performWithToken("not.a.jwt", get("/api/auth/me")));
    }

    @Test
    void emptyBearerTokenIsUnauthorized() throws Exception {
        expectUnauthorized(performWithToken("", get("/api/auth/me")));
    }

    @Test
    void expiredTokenIsUnauthorized() throws Exception {
        Instant expiresAt = Instant.now().minus(jwtProperties.clockSkew()).minus(Duration.ofMinutes(1));
        Instant issuedAt = expiresAt.minus(jwtProperties.accessTokenTtl());
        String token = encode(jwtEncoder, claims(user.id(), jwtProperties.issuer(), issuedAt, expiresAt));

        expectUnauthorized(performWithToken(token, get("/api/auth/me")));
    }

    @Test
    void tokenSignedWithAnotherSecretIsUnauthorized() throws Exception {
        JwtProperties other = new JwtProperties("an-attackers-secret-that-is-long-enough-0", jwtProperties.issuer(),
                jwtProperties.accessTokenTtl(), jwtProperties.refreshTokenIdleTtl(), jwtProperties.clockSkew());
        String token = encode(new JwtConfig().jwtEncoder(other), validClaims(user.id(), jwtProperties.issuer()));

        expectUnauthorized(performWithToken(token, get("/api/auth/me")));
    }

    @Test
    void unsignedTokenIsUnauthorized() throws Exception {
        Base64.Encoder encoder = Base64.getUrlEncoder().withoutPadding();
        String header = encoder.encodeToString("{\"alg\":\"none\"}".getBytes(StandardCharsets.UTF_8));
        String payload = login(user).accessToken().split("\\.")[1];

        expectUnauthorized(performWithToken(header + "." + payload + ".", get("/api/auth/me")));
    }

    @Test
    void tokenFromAnotherIssuerIsUnauthorized() throws Exception {
        expectUnauthorized(performWithToken(encode(jwtEncoder, validClaims(user.id(), "evil")), get("/api/auth/me")));
    }

    @Test
    void tokenForAUserThatDoesNotExistIsUnauthorized() throws Exception {
        String token = encode(jwtEncoder, validClaims(Long.MAX_VALUE, jwtProperties.issuer()));

        expectUnauthorized(performWithToken(token, get("/api/auth/me")));
    }

    @Test
    void tokenWithANonNumericSubjectIsUnauthorized() throws Exception {
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder().issuer(jwtProperties.issuer()).subject("not-a-number")
                .issuedAt(now).expiresAt(now.plus(jwtProperties.accessTokenTtl())).build();

        expectUnauthorized(performWithToken(encode(jwtEncoder, claims), get("/api/auth/me")));
    }

    @Test
    void theRoleComesFromTheDatabaseNotFromTheToken() throws Exception {
        String token = encode(jwtEncoder, validClaims(user.id(), jwtProperties.issuer()));

        performWithToken(token, get("/api/users")).andExpect(status().isForbidden());
    }

    @Test
    void refreshTokenIsNotAnAccessToken() throws Exception {
        expectUnauthorized(performWithToken(login(user).refreshToken(), get("/api/auth/me")));
    }

    @Test
    void activationTokenIsNotAnAccessToken() throws Exception {
        TestUser pending = fixtures.user(organisationId, UserRole.ADMIN, UserStatus.PENDING_ACTIVATION);
        String token = activationTokenService.issue(appUserRepository.findById(pending.id()).orElseThrow());

        expectUnauthorized(performWithToken(token, get("/api/auth/me")));
    }

    @Test
    void tamperedTokenIsUnauthorized() throws Exception {
        String[] parts = login(user).accessToken().split("\\.");
        String payload = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8)
                .replace("\"sub\":\"" + user.id() + "\"", "\"sub\":\"" + admin.id() + "\"");
        String forged = parts[0] + "." + Base64.getUrlEncoder().withoutPadding()
                .encodeToString(payload.getBytes(StandardCharsets.UTF_8)) + "." + parts[2];

        expectUnauthorized(performWithToken(forged, get("/api/auth/me")));
    }

    @Test
    void deactivatedUsersAccessTokenIsRejectedImmediately() throws Exception {
        String token = login(user).accessToken();

        performAs(admin, withJson(patch("/api/users/{id}", user.id()), Map.of("status", "INACTIVE")))
                .andExpect(status().isOk());

        expectUnauthorized(performWithToken(token, get("/api/auth/me")));
    }

    @Test
    void deletedUsersAccessTokenIsRejectedImmediately() throws Exception {
        String token = login(user).accessToken();

        performAs(admin, delete("/api/users/{id}", user.id())).andExpect(status().isNoContent());

        expectUnauthorized(performWithToken(token, get("/api/auth/me")));
    }

    @Test
    void reactivatedUsersTokenWorksAgain() throws Exception {
        String token = login(user).accessToken();
        performAs(admin, withJson(patch("/api/users/{id}", user.id()), Map.of("status", "INACTIVE")));

        performAs(admin, withJson(patch("/api/users/{id}", user.id()), Map.of("status", "ACTIVE")))
                .andExpect(status().isOk());

        performWithToken(token, get("/api/auth/me")).andExpect(status().isOk());
    }

    @Test
    void promotedUsersTokenGainsAdminAccessImmediately() throws Exception {
        String token = login(user).accessToken();
        performWithToken(token, get("/api/users")).andExpect(status().isForbidden());

        performAs(admin, withJson(patch("/api/users/{id}", user.id()), Map.of("role", "ADMIN")))
                .andExpect(status().isOk());

        performWithToken(token, get("/api/users")).andExpect(status().isOk());
    }

    private static void expectUnauthorized(ResultActions result) throws Exception {
        result.andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"))
                .andExpect(header().string(HttpHeaders.WWW_AUTHENTICATE, "Bearer"));
    }

    private JwtClaimsSet validClaims(long userId, String issuer) {
        Instant now = Instant.now();
        return claims(userId, issuer, now, now.plus(jwtProperties.accessTokenTtl()));
    }

    private static JwtClaimsSet claims(long userId, String issuer, Instant issuedAt, Instant expiresAt) {
        return JwtClaimsSet.builder().issuer(issuer).subject(String.valueOf(userId))
                .issuedAt(issuedAt).expiresAt(expiresAt).claim("role", "ADMIN").build();
    }

    private static String encode(JwtEncoder encoder, JwtClaimsSet claims) {
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        return encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }
}

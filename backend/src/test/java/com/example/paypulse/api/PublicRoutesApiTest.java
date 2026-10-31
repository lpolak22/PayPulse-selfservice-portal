package com.example.paypulse.api;

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
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.time.Instant;
import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PublicRoutesApiTest extends ApiIntegrationTest {

    @Autowired
    private JwtEncoder jwtEncoder;

    @Autowired
    private ActivationTokenService activationTokenService;

    @Autowired
    private AppUserRepository appUserRepository;

    private long organisationId;
    private TestUser user;

    @BeforeEach
    void setUp() {
        organisationId = fixtures.organisation();
        user = fixtures.user(organisationId, UserRole.USER, UserStatus.ACTIVE);
    }

    static Stream<String> staleHeaders() {
        return Stream.of("Bearer not.a.jwt", "Bearer ", "Bearer", "Basic dXNlcjpwYXNz");
    }

    @Test
    void anExpiredAccessTokenDoesNotBlockTheRefreshThatReplacesIt() throws Exception {
        TokenResponse tokens = login(user);
        String expired = expiredAccessToken();
        performWithToken(expired, get("/api/auth/me")).andExpect(status().isUnauthorized());

        TokenResponse renewed = refreshed(withHeader("Bearer " + expired,
                withJson(post("/api/auth/refresh"), Map.of("refreshToken", tokens.refreshToken()))));

        performWithToken(renewed.accessToken(), get("/api/auth/me")).andExpect(status().isOk());
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("staleHeaders")
    void loginIgnoresAStaleHeader(String authorization) throws Exception {
        withHeader(authorization, loginRequest(user.email(), UserFixtures.PASSWORD))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty());
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("staleHeaders")
    void aFailedLoginIsStillAnsweredForTheCredentials(String authorization) throws Exception {
        withHeader(authorization, loginRequest(user.email(), "Wrong-Password-1"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("INVALID_CREDENTIALS"));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("staleHeaders")
    void refreshIgnoresAStaleHeader(String authorization) throws Exception {
        String refreshToken = login(user).refreshToken();

        withHeader(authorization, withJson(post("/api/auth/refresh"), Map.of("refreshToken", refreshToken)))
                .andExpect(status().isOk());
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("staleHeaders")
    void logoutIgnoresAStaleHeaderAndStillRevokesTheToken(String authorization) throws Exception {
        String refreshToken = login(user).refreshToken();

        withHeader(authorization, withJson(post("/api/auth/logout"), Map.of("refreshToken", refreshToken)))
                .andExpect(status().isNoContent());

        expectInvalidRefreshToken(postRefresh(refreshToken));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("staleHeaders")
    void checkingAnActivationLinkIgnoresAStaleHeader(String authorization) throws Exception {
        withHeader(authorization, get("/api/auth/activation/{token}", activationLink()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("VALID"));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("staleHeaders")
    void activatingIgnoresAStaleHeader(String authorization) throws Exception {
        Map<String, String> body = Map.of("token", activationLink(), "password", "New-Password-1",
                "confirmPassword", "New-Password-1");

        withHeader(authorization, withJson(post("/api/auth/activation"), body)).andExpect(status().isNoContent());
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("staleHeaders")
    void theApiDocsIgnoreAStaleHeader(String authorization) throws Exception {
        int status = withHeader(authorization, get("/api/docs")).andReturn().getResponse().getStatus();

        assertThat(status).isBetween(200, 399);
    }

    @Test
    void aValidTokenOfAnotherUserDoesNotChangeWhoLogsIn() throws Exception {
        TestUser other = fixtures.user(organisationId, UserRole.ADMIN, UserStatus.ACTIVE);

        String body = withHeader("Bearer " + accessToken(user), loginRequest(other.email(), UserFixtures.PASSWORD))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        String accessToken = jsonMapper.readValue(body, TokenResponse.class).accessToken();
        performWithToken(accessToken, get("/api/auth/me")).andExpect(jsonPath("$.id").value(other.id()));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("staleHeaders")
    void theSamePathsWithAnotherMethodStillRejectAStaleHeader(String authorization) throws Exception {
        expectUnauthorized(withHeader(authorization, get("/api/auth/login")));
        expectUnauthorized(withHeader(authorization, get("/api/auth/refresh")));
        expectUnauthorized(withHeader(authorization, delete("/api/auth/activation/some-token")));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("staleHeaders")
    void aProtectedRouteStillRejectsAStaleHeader(String authorization) throws Exception {
        expectUnauthorized(withHeader(authorization, get("/api/auth/me")));
        expectUnauthorized(withHeader(authorization, get("/api/users")));
    }

    private ResultActions withHeader(String authorization, MockHttpServletRequestBuilder request) throws Exception {
        return mockMvc.perform(request.header(HttpHeaders.AUTHORIZATION, authorization));
    }

    private MockHttpServletRequestBuilder loginRequest(String email, String password) {
        return withJson(post("/api/auth/login"), Map.of("email", email, "password", password));
    }

    private static void expectUnauthorized(ResultActions result) throws Exception {
        result.andExpect(status().isUnauthorized()).andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
    }

    private String activationLink() {
        TestUser pending = fixtures.user(organisationId, UserRole.USER, UserStatus.PENDING_ACTIVATION);
        return activationTokenService.issue(appUserRepository.findById(pending.id()).orElseThrow());
    }

    private String expiredAccessToken() {
        Instant expiresAt = Instant.now().minus(jwtProperties.clockSkew()).minusSeconds(60);
        JwtClaimsSet claims = JwtClaimsSet.builder().issuer(jwtProperties.issuer()).subject(String.valueOf(user.id()))
                .issuedAt(expiresAt.minus(jwtProperties.accessTokenTtl())).expiresAt(expiresAt).build();
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }
}

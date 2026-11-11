package com.example.paypulse.api;

import com.example.paypulse.dto.auth.TokenResponse;
import com.example.paypulse.model.UserRole;
import com.example.paypulse.model.UserStatus;
import com.example.paypulse.support.ApiIntegrationTest;
import com.example.paypulse.support.UserFixtures.TestUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@TestPropertySource(properties = {
        "jwt.access-token-ttl=2s",
        "jwt.refresh-token-idle-ttl=4s",
        "jwt.clock-skew=0s"
})
class TokenExpiryApiTest extends ApiIntegrationTest {

    private static final Duration MARGIN = Duration.ofMillis(100);

    private TestUser user;

    @BeforeEach
    void setUp() {
        user = fixtures.user(fixtures.organisation(), UserRole.USER, UserStatus.ACTIVE);
    }

    @Test
    void anExpiredAccessTokenReturns401AndTheRefreshTokenReplacesIt() throws Exception {
        TokenResponse tokens = login(user);
        assertThat(tokens.expiresIn()).isEqualTo(jwtProperties.accessTokenTtl().toSeconds());
        performWithToken(tokens.accessToken(), get("/api/auth/me")).andExpect(status().isOk());

        waitFor(jwtProperties.accessTokenTtl().plus(MARGIN));

        performWithToken(tokens.accessToken(), get("/api/auth/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
        TokenResponse renewed = refreshed(postRefresh(tokens.refreshToken()));
        performWithToken(renewed.accessToken(), get("/api/auth/me")).andExpect(status().isOk());
    }

    @Test
    void theRefreshTokenExpiresAfterTheIdleTimeout() throws Exception {
        TokenResponse tokens = login(user);

        waitFor(jwtProperties.refreshTokenIdleTtl().plus(MARGIN));

        expectInvalidRefreshToken(postRefresh(tokens.refreshToken()));
        login(user);
    }

    @Test
    void usingTheRefreshTokenRestartsTheIdleTimeout() throws Exception {
        Duration moreThanHalfTheTimeout = jwtProperties.refreshTokenIdleTtl().multipliedBy(5).dividedBy(8);
        TokenResponse first = login(user);

        waitFor(moreThanHalfTheTimeout);
        TokenResponse second = refreshed(postRefresh(first.refreshToken()));

        waitFor(moreThanHalfTheTimeout);
        refreshed(postRefresh(second.refreshToken()));
    }

    private static void waitFor(Duration duration) throws InterruptedException {
        Thread.sleep(duration);
    }
}

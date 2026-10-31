package com.example.paypulse.api;

import com.example.paypulse.dto.auth.TokenResponse;
import com.example.paypulse.model.UserRole;
import com.example.paypulse.model.UserStatus;
import com.example.paypulse.support.ApiIntegrationTest;
import com.example.paypulse.support.UserFixtures.TestUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.ResultActions;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * POST /api/auth/refresh and /api/auth/logout: rotation, reuse detection, expiry, revocation, and what happens
 * to the refresh token of a user who is deactivated or deleted.
 */
class RefreshTokenApiTest extends ApiIntegrationTest {

    private TestUser admin;
    private TestUser user;

    @BeforeEach
    void setUp() {
        long organisationId = fixtures.organisation();
        admin = fixtures.user(organisationId, UserRole.ADMIN, UserStatus.ACTIVE);
        user = fixtures.user(organisationId, UserRole.USER, UserStatus.ACTIVE);
    }

    @Test
    void refreshReturnsANewWorkingTokenPair() throws Exception {
        TokenResponse first = login(user);

        TokenResponse second = refreshed(postRefresh(first.refreshToken()));

        assertThat(second.refreshToken()).isNotEqualTo(first.refreshToken());
        assertThat(second.tokenType()).isEqualTo("Bearer");
        performWithToken(second.accessToken(), get("/api/auth/me")).andExpect(status().isOk());
    }

    @Test
    void refreshNeedsNoAccessToken() throws Exception {
        postRefresh(login(user).refreshToken()).andExpect(status().isOk());
    }

    @Test
    void aRotatedRefreshTokenCannotBeUsedAgain() throws Exception {
        TokenResponse first = login(user);
        refreshed(postRefresh(first.refreshToken()));

        expectInvalidRefreshToken(postRefresh(first.refreshToken()));
    }

    @Test
    void reusingARotatedTokenRevokesTheWholeSession() throws Exception {
        TokenResponse first = login(user);
        TokenResponse second = refreshed(postRefresh(first.refreshToken()));

        expectInvalidRefreshToken(postRefresh(first.refreshToken()));

        expectInvalidRefreshToken(postRefresh(second.refreshToken()));
    }

    @Test
    void reuseRevokesEveryOpenSessionOfTheUser() throws Exception {
        TokenResponse phone = login(user);
        TokenResponse laptop = login(user);
        refreshed(postRefresh(phone.refreshToken()));

        expectInvalidRefreshToken(postRefresh(phone.refreshToken()));

        expectInvalidRefreshToken(postRefresh(laptop.refreshToken()));
    }

    @Test
    void unknownRefreshTokenIsRejected() throws Exception {
        expectInvalidRefreshToken(postRefresh("made-up-token"));
    }

    @Test
    void expiredRefreshTokenIsRejected() throws Exception {
        TokenResponse tokens = login(user);
        jdbc.update("update refresh_token set expires_at = current_timestamp - interval '1 minute' where id_user = ?",
                user.id());

        expectInvalidRefreshToken(postRefresh(tokens.refreshToken()));
    }

    @Test
    void blankRefreshTokenReturns400() throws Exception {
        postRefresh(" ").andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    void deactivatingAUserRevokesTheirRefreshTokensAtOnce() throws Exception {
        TokenResponse tokens = login(user);

        performAs(admin, withJson(patch("/api/users/{id}", user.id()), Map.of("status", "INACTIVE")))
                .andExpect(status().isOk());

        assertThat(openRefreshTokensOf(user)).isZero();
        expectInvalidRefreshToken(postRefresh(tokens.refreshToken()));
    }

    @Test
    void deletingAUserRevokesTheirRefreshTokensAtOnce() throws Exception {
        TokenResponse tokens = login(user);

        performAs(admin, delete("/api/users/{id}", user.id())).andExpect(status().isNoContent());

        assertThat(openRefreshTokensOf(user)).isZero();
        expectInvalidRefreshToken(postRefresh(tokens.refreshToken()));
    }

    @Test
    void refreshTokenOfAUserDeactivatedOutsideTheApiIsRejectedAndRevoked() throws Exception {
        TokenResponse tokens = login(user);
        jdbc.update("update app_user set status = 'INACTIVE', is_active = false where id_user = ?", user.id());

        postRefresh(tokens.refreshToken())
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("ACCOUNT_INACTIVE"));

        assertThat(openRefreshTokensOf(user)).isZero();
        expectInvalidRefreshToken(postRefresh(tokens.refreshToken()));
    }

    @Test
    void logoutRevokesTheRefreshToken() throws Exception {
        TokenResponse tokens = login(user);

        logout(tokens.refreshToken()).andExpect(status().isNoContent());

        expectInvalidRefreshToken(postRefresh(tokens.refreshToken()));
    }

    @Test
    void logoutEndsOnlyThatSession() throws Exception {
        TokenResponse phone = login(user);
        TokenResponse laptop = login(user);

        logout(phone.refreshToken()).andExpect(status().isNoContent());

        postRefresh(laptop.refreshToken()).andExpect(status().isOk());
    }

    @Test
    void logoutIsIdempotentAndAcceptsUnknownTokens() throws Exception {
        TokenResponse tokens = login(user);
        logout(tokens.refreshToken()).andExpect(status().isNoContent());

        logout(tokens.refreshToken()).andExpect(status().isNoContent());
        logout("made-up-token").andExpect(status().isNoContent());
    }

    @Test
    void blankLogoutTokenReturns400() throws Exception {
        logout("").andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    private ResultActions logout(String refreshToken) throws Exception {
        return mockMvc.perform(withJson(post("/api/auth/logout"), Map.of("refreshToken", refreshToken)));
    }

    private static void expectInvalidRefreshToken(ResultActions result) throws Exception {
        result.andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("INVALID_REFRESH_TOKEN"));
    }
}

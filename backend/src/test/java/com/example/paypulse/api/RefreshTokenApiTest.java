package com.example.paypulse.api;

import com.example.paypulse.dto.auth.TokenResponse;
import com.example.paypulse.model.UserRole;
import com.example.paypulse.model.UserStatus;
import com.example.paypulse.support.ApiIntegrationTest;
import com.example.paypulse.support.UserFixtures.TestUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.MediaType;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
    void refreshStartsANewIdleWindow() throws Exception {
        Duration idleTtl = jwtProperties.refreshTokenIdleTtl();
        TokenResponse first = login(user);
        jdbc.update("update refresh_token set expires_at = current_timestamp + interval '1 minute' where id_user = ?",
                user.id());
        LocalDateTime before = LocalDateTime.now().withNano(0);

        postRefresh(first.refreshToken())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.expiresIn").value(jwtProperties.accessTokenTtl().toSeconds()));

        LocalDateTime expiresAt = jdbc.queryForObject(
                "select expires_at from refresh_token where id_user = ? and used_at is null",
                LocalDateTime.class, user.id());
        assertThat(expiresAt).isBetween(before.plus(idleTtl), LocalDateTime.now().plus(idleTtl));
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
    void reuseLeavesTheSessionsOfOtherUsersOpen() throws Exception {
        TokenResponse own = login(user);
        TokenResponse other = login(admin);
        refreshed(postRefresh(own.refreshToken()));

        expectInvalidRefreshToken(postRefresh(own.refreshToken()));

        postRefresh(other.refreshToken()).andExpect(status().isOk());
    }

    @Test
    void reusingARotatedTokenThatHasSinceExpiredIsStillReuse() throws Exception {
        TokenResponse first = login(user);
        TokenResponse second = refreshed(postRefresh(first.refreshToken()));
        jdbc.update("update refresh_token set expires_at = current_timestamp - interval '1 minute' "
                + "where id_user = ? and used_at is not null", user.id());

        expectInvalidRefreshToken(postRefresh(first.refreshToken()));

        expectInvalidRefreshToken(postRefresh(second.refreshToken()));
    }

    @Test
    void unknownRefreshTokenIsRejected() throws Exception {
        expectInvalidRefreshToken(postRefresh("made-up-token"));
    }

    @Test
    void accessTokenIsNotARefreshToken() throws Exception {
        expectInvalidRefreshToken(postRefresh(login(user).accessToken()));
    }

    @Test
    void anExpiredRefreshTokenIsNotTreatedAsReuse() throws Exception {
        TokenResponse phone = login(user);
        TokenResponse laptop = login(user);
        jdbc.update("""
                update refresh_token set expires_at = current_timestamp - interval '1 minute'
                where id_token = (select min(id_token) from refresh_token where id_user = ?)""", user.id());

        expectInvalidRefreshToken(postRefresh(phone.refreshToken()));

        postRefresh(laptop.refreshToken()).andExpect(status().isOk());
    }

    @Test
    void blankRefreshTokenReturns400() throws Exception {
        postRefresh(" ").andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"/api/auth/refresh", "/api/auth/logout"})
    void missingRefreshTokenReturns400WithTheFieldNamed(String path) throws Exception {
        mockMvc.perform(withJson(post(path), Map.of()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.fieldErrors[0].field").value("refreshToken"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"/api/auth/refresh", "/api/auth/logout"})
    void malformedJsonReturns400(String path) throws Exception {
        mockMvc.perform(post(path).contentType(MediaType.APPLICATION_JSON).content("{\"refreshToken\": "))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("BAD_REQUEST"));
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
    void refreshTokenOfAUserDeletedOutsideTheApiIsRejectedAndRevoked() throws Exception {
        TokenResponse tokens = login(user);
        jdbc.update("update app_user set deleted_at = current_timestamp where id_user = ?", user.id());

        postRefresh(tokens.refreshToken())
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("ACCOUNT_INACTIVE"));

        assertThat(openRefreshTokensOf(user)).isZero();
    }

    @Test
    void logoutRevokesTheRefreshToken() throws Exception {
        TokenResponse tokens = login(user);

        postLogout(tokens.refreshToken()).andExpect(status().isNoContent());

        assertThat(openRefreshTokensOf(user)).isZero();
        expectInvalidRefreshToken(postRefresh(tokens.refreshToken()));
    }

    @Test
    void logoutEndsOnlyThatSession() throws Exception {
        TokenResponse phone = login(user);
        TokenResponse laptop = login(user);

        postLogout(phone.refreshToken()).andExpect(status().isNoContent());

        postRefresh(laptop.refreshToken()).andExpect(status().isOk());
    }

    @Test
    void replayingALoggedOutTokenCountsAsReuse() throws Exception {
        TokenResponse phone = login(user);
        TokenResponse laptop = login(user);
        postLogout(phone.refreshToken()).andExpect(status().isNoContent());

        expectInvalidRefreshToken(postRefresh(phone.refreshToken()));

        expectInvalidRefreshToken(postRefresh(laptop.refreshToken()));
    }

    @Test
    void logoutWithARotatedTokenLeavesTheNewSessionOpen() throws Exception {
        TokenResponse first = login(user);
        TokenResponse second = refreshed(postRefresh(first.refreshToken()));

        postLogout(first.refreshToken()).andExpect(status().isNoContent());

        postRefresh(second.refreshToken()).andExpect(status().isOk());
    }

    @Test
    void logoutAcceptsAnExpiredToken() throws Exception {
        TokenResponse tokens = login(user);
        jdbc.update("update refresh_token set expires_at = current_timestamp - interval '1 minute' where id_user = ?",
                user.id());

        postLogout(tokens.refreshToken()).andExpect(status().isNoContent());

        assertThat(openRefreshTokensOf(user)).isZero();
    }

    @Test
    void logoutLeavesTheAccessTokenWorkingUntilItExpires() throws Exception {
        TokenResponse tokens = login(user);

        postLogout(tokens.refreshToken()).andExpect(status().isNoContent());

        performWithToken(tokens.accessToken(), get("/api/auth/me")).andExpect(status().isOk());
    }

    @Test
    void logoutIsIdempotentAndAcceptsUnknownTokens() throws Exception {
        TokenResponse tokens = login(user);
        postLogout(tokens.refreshToken()).andExpect(status().isNoContent());

        postLogout(tokens.refreshToken()).andExpect(status().isNoContent());
        postLogout("made-up-token").andExpect(status().isNoContent());
    }

    @Test
    void blankLogoutTokenReturns400() throws Exception {
        postLogout("").andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }
}

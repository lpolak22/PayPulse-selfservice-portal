package com.example.paypulse.service.impl;

import com.example.paypulse.config.JwtProperties;
import com.example.paypulse.exception.ApiException;
import com.example.paypulse.exception.AuthException;
import com.example.paypulse.exception.ErrorCode;
import com.example.paypulse.model.AppUser;
import com.example.paypulse.model.Organisation;
import com.example.paypulse.model.RefreshToken;
import com.example.paypulse.model.UserRole;
import com.example.paypulse.repository.RefreshTokenRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HexFormat;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RefreshTokenServiceImplTest {

    private static final long USER_ID = 7L;
    private static final Instant NOW_INSTANT = Instant.parse("2026-10-28T09:30:00Z");
    private static final LocalDateTime NOW = LocalDateTime.ofInstant(NOW_INSTANT, ZoneOffset.UTC);
    private static final Duration IDLE_TTL = Duration.ofMinutes(30);

    private final RefreshTokenRepository repository = mock(RefreshTokenRepository.class);
    private final RefreshTokenServiceImpl service = new RefreshTokenServiceImpl(repository,
            new JwtProperties("unused", "paypulse", Duration.ofMinutes(15), IDLE_TTL, Duration.ofSeconds(60)),
            Clock.fixed(NOW_INSTANT, ZoneOffset.UTC));
    private final AppUser user = user();

    @Test
    void issuedTokenIsStoredOnlyAsItsSha256Hash() {
        String rawToken = service.issue(user);

        RefreshToken saved = savedToken();
        assertThat(saved.getTokenHash()).isEqualTo(sha256(rawToken)).isNotEqualTo(rawToken);
        assertThat(saved.getUser()).isSameAs(user);
    }

    @Test
    void issuedTokenExpiresAfterTheIdleTimeout() {
        service.issue(user);

        assertThat(savedToken().getExpiresAt()).isEqualTo(NOW.plus(IDLE_TTL));
    }

    @Test
    void issuedTokensAreRandomAndUrlSafe() {
        String first = service.issue(user);
        String second = service.issue(user);

        assertThat(first).isNotEqualTo(second).matches("[A-Za-z0-9_-]{43}");
    }

    @Test
    void rotatingAValidTokenMarksItUsedAndReturnsItsUser() {
        RefreshToken token = stored("raw", NOW.plusMinutes(5));

        assertThat(service.rotate("raw")).isSameAs(user);
        assertThat(token.getUsedAt()).isEqualTo(NOW);
    }

    @Test
    void rotatingAnUnknownTokenFails() {
        when(repository.findByTokenHash(any())).thenReturn(Optional.empty());

        expectInvalidRefreshToken("unknown");
    }

    @Test
    void reusingAnAlreadyUsedTokenRevokesEveryTokenOfTheUser() {
        RefreshToken token = stored("raw", NOW.plusMinutes(5));
        token.markUsed(NOW.minusMinutes(1));

        expectInvalidRefreshToken("raw");
        verify(repository).revokeAllForUser(USER_ID, NOW);
    }

    @Test
    void aTokenIsExpiredExactlyAtItsExpiryTime() {
        RefreshToken token = stored("raw", NOW);

        expectInvalidRefreshToken("raw");
        assertThat(token.isUsed()).isFalse();
        verify(repository, never()).revokeAllForUser(anyLong(), any());
    }

    @Test
    void revokingMarksAnUnusedTokenUsed() {
        RefreshToken token = stored("raw", NOW.plusMinutes(5));

        service.revoke("raw");

        assertThat(token.getUsedAt()).isEqualTo(NOW);
    }

    @Test
    void revokingAnAlreadyUsedTokenKeepsTheOriginalTime() {
        RefreshToken token = stored("raw", NOW.plusMinutes(5));
        token.markUsed(NOW.minusMinutes(3));

        service.revoke("raw");

        assertThat(token.getUsedAt()).isEqualTo(NOW.minusMinutes(3));
    }

    @Test
    void revokingAnUnknownTokenIsANoOp() {
        when(repository.findByTokenHash(any())).thenReturn(Optional.empty());

        service.revoke("unknown");

        verify(repository, never()).save(any());
    }

    @Test
    void revokeAllForUserUsesTheCurrentTime() {
        service.revokeAllForUser(USER_ID);

        verify(repository).revokeAllForUser(USER_ID, NOW);
    }

    @Test
    void cleanupDeletesTokensOlderThanSevenDays() {
        service.deleteOldTokens();

        verify(repository).deleteExpiredOrUsedBefore(NOW.minusDays(7));
    }

    private RefreshToken stored(String rawToken, LocalDateTime expiresAt) {
        RefreshToken token = new RefreshToken(user, sha256(rawToken), expiresAt);
        when(repository.findByTokenHash(sha256(rawToken))).thenReturn(Optional.of(token));
        return token;
    }

    private RefreshToken savedToken() {
        ArgumentCaptor<RefreshToken> captor = ArgumentCaptor.forClass(RefreshToken.class);
        verify(repository).save(captor.capture());
        return captor.getValue();
    }

    private void expectInvalidRefreshToken(String rawToken) {
        assertThatThrownBy(() -> service.rotate(rawToken))
                .isInstanceOf(AuthException.class)
                .extracting(e -> ((ApiException) e).getErrorCode()).isEqualTo(ErrorCode.INVALID_REFRESH_TOKEN);
    }

    private static AppUser user() {
        AppUser user = AppUser.createPending(new Organisation(), "Iva", "Babic", "iva@example.com", null,
                UserRole.USER);
        ReflectionTestUtils.setField(user, "idUser", USER_ID);
        return user;
    }

    private static String sha256(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (Exception exception) {
            throw new IllegalStateException(exception);
        }
    }
}

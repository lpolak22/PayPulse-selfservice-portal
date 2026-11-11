package com.example.paypulse.service.impl;

import com.example.paypulse.config.JwtProperties;
import com.example.paypulse.exception.AuthException;
import com.example.paypulse.exception.ErrorCode;
import com.example.paypulse.model.AppUser;
import com.example.paypulse.model.RefreshToken;
import com.example.paypulse.repository.RefreshTokenRepository;
import com.example.paypulse.service.RefreshTokenService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;

@Service
@Transactional(noRollbackFor = AuthException.class)
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private static final int TOKEN_BYTES = 32;
    private static final int CLEANUP_AFTER_DAYS = 7;

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtProperties properties;
    private final Clock clock;
    private final SecureRandom secureRandom = new SecureRandom();

    public RefreshTokenServiceImpl(RefreshTokenRepository refreshTokenRepository,
                                   JwtProperties properties,
                                   Clock clock) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.properties = properties;
        this.clock = clock;
    }

    @Override
    public String issue(AppUser user) {
        String rawToken = generateRawToken();
        LocalDateTime expiresAt = now().plus(properties.refreshTokenIdleTtl());
        refreshTokenRepository.save(new RefreshToken(user, hash(rawToken), expiresAt));
        return rawToken;
    }

    @Override
    public AppUser rotate(String rawToken) {
        RefreshToken token = findOrThrow(rawToken);
        if (token.isUsed()) {
            refreshTokenRepository.revokeAllForUser(token.getUser().getIdUser(), now());
            throw new AuthException(ErrorCode.INVALID_REFRESH_TOKEN);
        }
        if (token.isExpired(now())) {
            throw new AuthException(ErrorCode.INVALID_REFRESH_TOKEN);
        }
        token.markUsed(now());
        return token.getUser();
    }

    @Override
    public void revoke(String rawToken) {
        refreshTokenRepository.findByTokenHash(hash(rawToken))
                .filter(token -> !token.isUsed())
                .ifPresent(token -> token.markUsed(now()));
    }

    @Override
    public void revokeAllForUser(Long userId) {
        refreshTokenRepository.revokeAllForUser(userId, now());
    }

    @Scheduled(cron = "0 0 3 * * *")
    public void deleteOldTokens() {
        refreshTokenRepository.deleteExpiredOrUsedBefore(now().minusDays(CLEANUP_AFTER_DAYS));
    }

    private RefreshToken findOrThrow(String rawToken) {
        return refreshTokenRepository.findByTokenHash(hash(rawToken))
                .orElseThrow(() -> new AuthException(ErrorCode.INVALID_REFRESH_TOKEN));
    }

    private String generateRawToken() {
        byte[] bytes = new byte[TOKEN_BYTES];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private static String hash(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(rawToken.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is not available", exception);
        }
    }

    private LocalDateTime now() {
        return LocalDateTime.now(clock);
    }
}

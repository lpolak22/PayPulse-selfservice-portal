package com.example.paypulse.dto.auth;

public record TokenResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        long expiresIn
) {

    public static TokenResponse bearer(String accessToken, String refreshToken, long expiresInSeconds) {
        return new TokenResponse(accessToken, refreshToken, "Bearer", expiresInSeconds);
    }
}

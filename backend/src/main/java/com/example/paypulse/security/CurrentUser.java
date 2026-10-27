package com.example.paypulse.security;

/**
 * The authenticated user making the current request.
 */
public record CurrentUser(Long userId, Long organisationId) {
}

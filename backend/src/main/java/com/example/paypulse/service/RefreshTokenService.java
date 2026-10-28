package com.example.paypulse.service;

import com.example.paypulse.model.AppUser;

public interface RefreshTokenService {

    String issue(AppUser user);

    AppUser rotate(String rawToken);

    void revoke(String rawToken);

    void revokeAllForUser(Long userId);
}

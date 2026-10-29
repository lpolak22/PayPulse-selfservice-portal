package com.example.paypulse.service;

import com.example.paypulse.model.AppUser;

public interface AccessTokenService {

    String issue(AppUser user);

    long getExpiresInSeconds();
}

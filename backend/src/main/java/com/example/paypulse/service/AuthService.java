package com.example.paypulse.service;

import com.example.paypulse.dto.AppUserResponse;
import com.example.paypulse.dto.auth.LoginRequest;
import com.example.paypulse.dto.auth.TokenResponse;

public interface AuthService {

    TokenResponse login(LoginRequest request);

    TokenResponse refresh(String refreshToken);

    void logout(String refreshToken);

    AppUserResponse getCurrentUser();
}

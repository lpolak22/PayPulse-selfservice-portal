package com.example.paypulse.service;

public interface PasswordResetService {

    void requestReset(String email, String clientIp);
}

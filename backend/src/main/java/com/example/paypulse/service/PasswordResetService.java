package com.example.paypulse.service;

import com.example.paypulse.dto.auth.PasswordResetTokenStatus;
import com.example.paypulse.dto.auth.ResetPasswordRequest;

public interface PasswordResetService {

    void requestReset(String email, String clientIp);

    PasswordResetTokenStatus checkToken(String token);

    void resetPassword(ResetPasswordRequest request);
}

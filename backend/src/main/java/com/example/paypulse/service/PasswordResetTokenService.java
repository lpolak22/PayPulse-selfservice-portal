package com.example.paypulse.service;

import com.example.paypulse.dto.auth.PasswordResetTokenStatus;
import com.example.paypulse.model.AppUser;

public interface PasswordResetTokenService {

    String issue(AppUser user);

    PasswordResetTokenStatus statusOf(String token);

    AppUser requireValid(String token);
}

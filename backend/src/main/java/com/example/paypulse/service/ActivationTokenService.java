package com.example.paypulse.service;

import com.example.paypulse.dto.auth.ActivationTokenStatus;
import com.example.paypulse.model.AppUser;

public interface ActivationTokenService {

    String issue(AppUser user);

    ActivationTokenStatus statusOf(String token);

    AppUser requireValid(String token);
}

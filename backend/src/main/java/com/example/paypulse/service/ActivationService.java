package com.example.paypulse.service;

import com.example.paypulse.dto.auth.ActivateAccountRequest;
import com.example.paypulse.dto.auth.ActivationTokenStatus;
import com.example.paypulse.model.AppUser;

public interface ActivationService {

    void startActivation(AppUser user);

    ActivationTokenStatus checkToken(String token);

    void activate(ActivateAccountRequest request);
}

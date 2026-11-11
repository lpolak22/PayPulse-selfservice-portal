package com.example.paypulse.service;

import com.example.paypulse.model.UnknownLoginAttempt;

public interface UnknownLoginAttemptService {

    UnknownLoginAttempt forEmail(String email);

    void deleteStaleAttempts();
}

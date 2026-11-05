package com.example.paypulse.model;

import java.time.LocalDateTime;

public interface LoginLockoutState {

    Short getFailedLoginCount();

    LocalDateTime getLockedUntil();

    void recordFailedLogin();

    void resetFailedLogins();

    void lockUntil(LocalDateTime lockedUntil);

    boolean isLockedAt(LocalDateTime now);
}

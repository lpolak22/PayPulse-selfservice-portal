package com.example.paypulse.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "unknown_login_attempt")
public class UnknownLoginAttempt implements LoginLockoutState {

    @Id
    @Column(name = "email_hash", length = 64)
    private String emailHash;

    @Column(name = "failed_login_count", nullable = false)
    private Short failedLoginCount = 0;

    @Column(name = "locked_until")
    private LocalDateTime lockedUntil;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected UnknownLoginAttempt() {
    }

    public UnknownLoginAttempt(String emailHash) {
        this.emailHash = emailHash;
    }

    @Override
    public void recordFailedLogin() {
        this.failedLoginCount = (short) (failedLoginCount + 1);
    }

    @Override
    public void resetFailedLogins() {
        this.failedLoginCount = 0;
        this.lockedUntil = null;
    }

    @Override
    public void lockUntil(LocalDateTime lockedUntil) {
        this.lockedUntil = lockedUntil;
    }

    @Override
    public boolean isLockedAt(LocalDateTime now) {
        return lockedUntil != null && lockedUntil.isAfter(now);
    }

    public String getEmailHash() {
        return emailHash;
    }

    @Override
    public Short getFailedLoginCount() {
        return failedLoginCount;
    }

    @Override
    public LocalDateTime getLockedUntil() {
        return lockedUntil;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}

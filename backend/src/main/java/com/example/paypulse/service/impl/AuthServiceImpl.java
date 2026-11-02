package com.example.paypulse.service.impl;

import com.example.paypulse.config.LoginLockoutProperties;
import com.example.paypulse.dto.AppUserResponse;
import com.example.paypulse.dto.auth.LoginRequest;
import com.example.paypulse.dto.auth.TokenResponse;
import com.example.paypulse.exception.AuthException;
import com.example.paypulse.exception.ErrorCode;
import com.example.paypulse.exception.InvalidCredentialsException;
import com.example.paypulse.exception.ResourceNotFoundException;
import com.example.paypulse.mapper.AppUserMapper;
import com.example.paypulse.model.AppUser;
import com.example.paypulse.model.AuditAction;
import com.example.paypulse.model.UserStatus;
import com.example.paypulse.repository.AppUserRepository;
import com.example.paypulse.security.CurrentUser;
import com.example.paypulse.security.CurrentUserProvider;
import com.example.paypulse.service.AccessTokenService;
import com.example.paypulse.service.AuditLogService;
import com.example.paypulse.service.AuthService;
import com.example.paypulse.service.RefreshTokenService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Map;

@Service
@Transactional(noRollbackFor = AuthException.class)
public class AuthServiceImpl implements AuthService {

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final AccessTokenService accessTokenService;
    private final RefreshTokenService refreshTokenService;
    private final AuditLogService auditLogService;
    private final CurrentUserProvider currentUserProvider;
    private final AppUserMapper appUserMapper;
    private final LoginLockoutProperties lockoutProperties;
    private final Clock clock;

    public AuthServiceImpl(AppUserRepository appUserRepository,
                           PasswordEncoder passwordEncoder,
                           AccessTokenService accessTokenService,
                           RefreshTokenService refreshTokenService,
                           AuditLogService auditLogService,
                           CurrentUserProvider currentUserProvider,
                           AppUserMapper appUserMapper,
                           LoginLockoutProperties lockoutProperties,
                           Clock clock) {
        this.appUserRepository = appUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.accessTokenService = accessTokenService;
        this.refreshTokenService = refreshTokenService;
        this.auditLogService = auditLogService;
        this.currentUserProvider = currentUserProvider;
        this.appUserMapper = appUserMapper;
        this.lockoutProperties = lockoutProperties;
        this.clock = clock;
    }

    @Override
    public TokenResponse login(LoginRequest request) {
        AppUser user = appUserRepository.findUndeletedByEmail(request.email().strip())
                .orElseThrow(() -> invalidCredentials(null, request.email(), lockoutProperties.maxAttempts() - 1));
        LocalDateTime now = LocalDateTime.now(clock);
        ensureNotLocked(user, now);
        ensureCanLogIn(user, request.password(), now);
        user.resetFailedLogins();
        auditLogService.record(user.getIdUser(), AuditAction.LOGIN_SUCCESS, user.getIdUser(), null, null);
        return issueTokens(user);
    }

    @Override
    public TokenResponse refresh(String refreshToken) {
        AppUser user = refreshTokenService.rotate(refreshToken);
        if (!user.isActive() || user.getDeletedAt() != null) {
            refreshTokenService.revokeAllForUser(user.getIdUser());
            throw new AuthException(ErrorCode.ACCOUNT_INACTIVE);
        }
        return issueTokens(user);
    }

    @Override
    public void logout(String refreshToken) {
        refreshTokenService.revoke(refreshToken);
    }

    @Override
    @Transactional(readOnly = true)
    public AppUserResponse getCurrentUser() {
        CurrentUser current = currentUserProvider.getCurrentUser();
        return appUserRepository.findUndeletedInOrganisation(current.userId(), current.organisationId())
                .map(appUserMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("User", current.userId()));
    }

    private void ensureNotLocked(AppUser user, LocalDateTime now) {
        if (user.isLockedAt(now)) {
            throw failedLogin(user.getIdUser(), user.getEmail(), ErrorCode.ACCOUNT_LOCKED);
        }
        if (user.getLockedUntil() != null) {
            user.resetFailedLogins();
        }
    }

    private void ensureCanLogIn(AppUser user, String password, LocalDateTime now) {
        if (user.getPasswordHash() == null || !passwordEncoder.matches(password, user.getPasswordHash())) {
            throw wrongPassword(user, now);
        }
        if (user.getStatus() == UserStatus.PENDING_ACTIVATION) {
            throw failedLogin(user.getIdUser(), user.getEmail(), ErrorCode.ACCOUNT_PENDING_ACTIVATION);
        }
        if (!user.isActive()) {
            throw failedLogin(user.getIdUser(), user.getEmail(), ErrorCode.ACCOUNT_INACTIVE);
        }
    }

    private AuthException wrongPassword(AppUser user, LocalDateTime now) {
        user.recordFailedLogin();
        int remaining = remainingAttempts(user);
        if (remaining > 0) {
            return invalidCredentials(user.getIdUser(), user.getEmail(), remaining);
        }
        user.lockUntil(now.plus(lockoutProperties.lockoutDuration()));
        auditFailedLogin(user.getIdUser(), user.getEmail(), ErrorCode.INVALID_CREDENTIALS);
        return new AuthException(ErrorCode.ACCOUNT_LOCKED);
    }

    private int remainingAttempts(AppUser user) {
        return Math.max(0, lockoutProperties.maxAttempts() - user.getFailedLoginCount());
    }

    private InvalidCredentialsException invalidCredentials(Long userId, String email, int remainingAttempts) {
        auditFailedLogin(userId, email, ErrorCode.INVALID_CREDENTIALS);
        return new InvalidCredentialsException(remainingAttempts);
    }

    private AuthException failedLogin(Long userId, String email, ErrorCode reason) {
        auditFailedLogin(userId, email, reason);
        return new AuthException(reason);
    }

    private void auditFailedLogin(Long userId, String email, ErrorCode reason) {
        Map<String, String> details = Map.of("email", email.strip(), "reason", reason.name());
        auditLogService.record(userId, AuditAction.LOGIN_FAILED, userId, null, details);
    }

    private TokenResponse issueTokens(AppUser user) {
        String accessToken = accessTokenService.issue(user);
        String refreshToken = refreshTokenService.issue(user);
        return TokenResponse.bearer(accessToken, refreshToken, accessTokenService.getExpiresInSeconds());
    }
}

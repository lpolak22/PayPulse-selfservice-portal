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
import com.example.paypulse.model.LoginLockoutState;
import com.example.paypulse.model.UserStatus;
import com.example.paypulse.repository.AppUserRepository;
import com.example.paypulse.security.CurrentUser;
import com.example.paypulse.security.CurrentUserProvider;
import com.example.paypulse.service.AccessTokenService;
import com.example.paypulse.service.AuditLogService;
import com.example.paypulse.service.AuthService;
import com.example.paypulse.service.RefreshTokenService;
import com.example.paypulse.service.UnknownLoginAttemptService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.UUID;

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
    private final UnknownLoginAttemptService unknownLoginAttemptService;
    private final Clock clock;
    private final String placeholderPasswordHash;

    public AuthServiceImpl(AppUserRepository appUserRepository,
                           PasswordEncoder passwordEncoder,
                           AccessTokenService accessTokenService,
                           RefreshTokenService refreshTokenService,
                           AuditLogService auditLogService,
                           CurrentUserProvider currentUserProvider,
                           AppUserMapper appUserMapper,
                           LoginLockoutProperties lockoutProperties,
                           UnknownLoginAttemptService unknownLoginAttemptService,
                           Clock clock) {
        this.appUserRepository = appUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.accessTokenService = accessTokenService;
        this.refreshTokenService = refreshTokenService;
        this.auditLogService = auditLogService;
        this.currentUserProvider = currentUserProvider;
        this.appUserMapper = appUserMapper;
        this.lockoutProperties = lockoutProperties;
        this.unknownLoginAttemptService = unknownLoginAttemptService;
        this.clock = clock;
        this.placeholderPasswordHash = passwordEncoder.encode(UUID.randomUUID().toString());
    }

    @Override
    public TokenResponse login(LoginRequest request) {
        String email = request.email().strip();
        LocalDateTime now = LocalDateTime.now(clock);
        AppUser user = appUserRepository.findUndeletedByEmail(email)
                .orElseThrow(() -> unknownEmail(email, request.password(), now));
        ensureNotLocked(LoginTarget.of(user), now);
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

    private AuthException unknownEmail(String email, String password, LocalDateTime now) {
        passwordEncoder.matches(password, placeholderPasswordHash);
        LoginTarget target = new LoginTarget(null, email, unknownLoginAttemptService.forEmail(email));
        ensureNotLocked(target, now);
        return wrongPassword(target, now);
    }

    private void ensureNotLocked(LoginTarget target, LocalDateTime now) {
        LoginLockoutState lockout = target.lockout();
        if (lockout.isLockedAt(now)) {
            throw failedLogin(target.userId(), target.email(), ErrorCode.ACCOUNT_LOCKED);
        }
        if (lockout.getLockedUntil() != null) {
            lockout.resetFailedLogins();
        }
    }

    private void ensureCanLogIn(AppUser user, String password, LocalDateTime now) {
        if (!passwordMatches(user, password)) {
            throw wrongPassword(LoginTarget.of(user), now);
        }
        if (user.getStatus() == UserStatus.PENDING_ACTIVATION) {
            throw failedLogin(user.getIdUser(), user.getEmail(), ErrorCode.ACCOUNT_PENDING_ACTIVATION);
        }
        if (!user.isActive()) {
            throw failedLogin(user.getIdUser(), user.getEmail(), ErrorCode.ACCOUNT_INACTIVE);
        }
    }

    private boolean passwordMatches(AppUser user, String password) {
        if (user.getPasswordHash() == null) {
            passwordEncoder.matches(password, placeholderPasswordHash);
            return false;
        }
        return passwordEncoder.matches(password, user.getPasswordHash());
    }

    private AuthException wrongPassword(LoginTarget target, LocalDateTime now) {
        target.lockout().recordFailedLogin();
        int remaining = remainingAttempts(target.lockout());
        if (remaining > 0) {
            return invalidCredentials(target.userId(), target.email(), remaining);
        }
        auditFailedLogin(target.userId(), target.email(), ErrorCode.INVALID_CREDENTIALS);
        lock(target, now);
        return new AuthException(ErrorCode.ACCOUNT_LOCKED);
    }

    private void lock(LoginTarget target, LocalDateTime now) {
        LocalDateTime lockedUntil = now.plus(lockoutProperties.lockoutDuration()).truncatedTo(ChronoUnit.SECONDS);
        target.lockout().lockUntil(lockedUntil);
        Map<String, Object> details = Map.of("email", target.email(),
                "failedAttempts", target.lockout().getFailedLoginCount(), "lockedUntil", lockedUntil.toString());
        auditLogService.record(target.userId(), AuditAction.ACCOUNT_LOCKED, target.userId(), null, details);
    }

    private int remainingAttempts(LoginLockoutState lockout) {
        return Math.max(0, lockoutProperties.maxAttempts() - lockout.getFailedLoginCount());
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

    private record LoginTarget(Long userId, String email, LoginLockoutState lockout) {

        static LoginTarget of(AppUser user) {
            return new LoginTarget(user.getIdUser(), user.getEmail(), user);
        }
    }
}

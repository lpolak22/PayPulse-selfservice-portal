package com.example.paypulse.service.impl;

import com.example.paypulse.config.PasswordResetProperties;
import com.example.paypulse.dto.auth.PasswordResetTokenStatus;
import com.example.paypulse.dto.auth.ResetPasswordRequest;
import com.example.paypulse.dto.email.EmailMessage;
import com.example.paypulse.exception.ApiException;
import com.example.paypulse.exception.ErrorCode;
import com.example.paypulse.model.AppUser;
import com.example.paypulse.model.AuditAction;
import com.example.paypulse.model.UserStatus;
import com.example.paypulse.repository.AppUserRepository;
import com.example.paypulse.service.AuditLogService;
import com.example.paypulse.service.EmailSender;
import com.example.paypulse.service.NewPasswordValidator;
import com.example.paypulse.service.PasswordResetService;
import com.example.paypulse.service.PasswordResetTokenService;
import com.example.paypulse.service.RateLimiter;
import com.example.paypulse.service.RefreshTokenService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
@Transactional
public class PasswordResetServiceImpl implements PasswordResetService {

    private static final String SUBJECT = "Reset your PayPulse password";

    private final AppUserRepository appUserRepository;
    private final PasswordResetTokenService resetTokenService;
    private final EmailSender emailSender;
    private final PasswordResetProperties properties;
    private final RateLimiter rateLimiter;
    private final NewPasswordValidator newPasswordValidator;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenService refreshTokenService;
    private final AuditLogService auditLogService;

    public PasswordResetServiceImpl(AppUserRepository appUserRepository,
                                    PasswordResetTokenService resetTokenService,
                                    EmailSender emailSender,
                                    PasswordResetProperties properties,
                                    RateLimiter rateLimiter,
                                    NewPasswordValidator newPasswordValidator,
                                    PasswordEncoder passwordEncoder,
                                    RefreshTokenService refreshTokenService,
                                    AuditLogService auditLogService) {
        this.appUserRepository = appUserRepository;
        this.resetTokenService = resetTokenService;
        this.emailSender = emailSender;
        this.properties = properties;
        this.rateLimiter = rateLimiter;
        this.newPasswordValidator = newPasswordValidator;
        this.passwordEncoder = passwordEncoder;
        this.refreshTokenService = refreshTokenService;
        this.auditLogService = auditLogService;
    }

    @Override
    @Transactional(readOnly = true)
    public void requestReset(String email, String clientIp) {
        String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);
        checkRateLimit("ip:" + clientIp, properties.maxRequestsPerIp());
        checkRateLimit("email:" + normalizedEmail, properties.maxRequestsPerEmail());
        appUserRepository.findUndeletedByEmail(normalizedEmail)
                .filter(user -> user.getStatus() == UserStatus.ACTIVE)
                .ifPresent(this::sendResetEmail);
    }

    @Override
    @Transactional(readOnly = true)
    public PasswordResetTokenStatus checkToken(String token) {
        return resetTokenService.statusOf(token);
    }

    @Override
    public void resetPassword(ResetPasswordRequest request) {
        AppUser user = resetTokenService.requireValid(request.token());
        newPasswordValidator.validate(request.password(), request.confirmPassword());
        user.resetPassword(passwordEncoder.encode(request.password()));
        refreshTokenService.revokeAllForUser(user.getIdUser());
        auditLogService.record(user.getIdUser(), AuditAction.PASSWORD_RESET, user.getIdUser(), null, null);
    }

    private void checkRateLimit(String key, int limit) {
        if (!rateLimiter.tryAcquire(key, limit, properties.rateLimitWindow())) {
            throw new ApiException(ErrorCode.PASSWORD_RESET_RATE_LIMITED);
        }
    }

    private void sendResetEmail(AppUser user) {
        String link = properties.linkBase() + "?token=" + resetTokenService.issue(user);
        emailSender.send(new EmailMessage(user.getEmail(), SUBJECT, resetBody(user, link)));
    }

    private String resetBody(AppUser user, String link) {
        return "Hi " + user.getFirstName() + ",\n\n"
                + "we received a request to reset your PayPulse password.\n\n"
                + "Reset password: " + link + "\n\n"
                + "The link is valid for " + properties.tokenTtl().toMinutes() + " minutes and can be used only once.\n"
                + "If you did not ask for this, you can ignore this email.";
    }
}

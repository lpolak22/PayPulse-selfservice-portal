package com.example.paypulse.service.impl;

import com.example.paypulse.config.ActivationProperties;
import com.example.paypulse.dto.auth.ActivateAccountRequest;
import com.example.paypulse.dto.auth.ActivationTokenStatus;
import com.example.paypulse.model.AppUser;
import com.example.paypulse.model.AuditAction;
import com.example.paypulse.service.ActivationService;
import com.example.paypulse.service.ActivationTokenService;
import com.example.paypulse.service.AuditLogService;
import com.example.paypulse.service.EmailService;
import com.example.paypulse.service.NewPasswordValidator;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@Transactional
public class ActivationServiceImpl implements ActivationService {

    private static final String TEMPLATE = "activation";

    private final ActivationTokenService activationTokenService;
    private final EmailService emailService;
    private final NewPasswordValidator newPasswordValidator;
    private final PasswordEncoder passwordEncoder;
    private final AuditLogService auditLogService;
    private final ActivationProperties properties;

    public ActivationServiceImpl(ActivationTokenService activationTokenService,
                                 EmailService emailService,
                                 NewPasswordValidator newPasswordValidator,
                                 PasswordEncoder passwordEncoder,
                                 AuditLogService auditLogService,
                                 ActivationProperties properties) {
        this.activationTokenService = activationTokenService;
        this.emailService = emailService;
        this.newPasswordValidator = newPasswordValidator;
        this.passwordEncoder = passwordEncoder;
        this.auditLogService = auditLogService;
        this.properties = properties;
    }

    @Override
    public void startActivation(AppUser user) {
        String link = properties.linkBase() + "?token=" + activationTokenService.issue(user);
        emailService.send(user.getEmail(), TEMPLATE, Map.of(
                "firstName", user.getFirstName(),
                "link", link,
                "validHours", properties.tokenTtl().toHours()));
    }

    @Override
    @Transactional(readOnly = true)
    public ActivationTokenStatus checkToken(String token) {
        return activationTokenService.statusOf(token);
    }

    @Override
    public void activate(ActivateAccountRequest request) {
        AppUser user = activationTokenService.requireValid(request.token());
        newPasswordValidator.validate(request.password(), request.confirmPassword());
        user.completeActivation(passwordEncoder.encode(request.password()));
        auditLogService.record(user.getIdUser(), AuditAction.USER_ACTIVATED, user.getIdUser(), null, null);
    }
}

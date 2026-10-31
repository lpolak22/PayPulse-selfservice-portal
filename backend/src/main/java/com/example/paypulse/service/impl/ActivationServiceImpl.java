package com.example.paypulse.service.impl;

import com.example.paypulse.config.ActivationProperties;
import com.example.paypulse.dto.auth.ActivateAccountRequest;
import com.example.paypulse.dto.auth.ActivationTokenStatus;
import com.example.paypulse.dto.common.ErrorResponse;
import com.example.paypulse.dto.email.EmailMessage;
import com.example.paypulse.exception.FieldValidationException;
import com.example.paypulse.model.AppUser;
import com.example.paypulse.model.AuditAction;
import com.example.paypulse.service.ActivationService;
import com.example.paypulse.service.ActivationTokenService;
import com.example.paypulse.service.AuditLogService;
import com.example.paypulse.service.EmailSender;
import com.example.paypulse.service.PasswordPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class ActivationServiceImpl implements ActivationService {

    private static final String SUBJECT = "Set your PayPulse password";

    private final ActivationTokenService activationTokenService;
    private final EmailSender emailSender;
    private final PasswordPolicy passwordPolicy;
    private final PasswordEncoder passwordEncoder;
    private final AuditLogService auditLogService;
    private final ActivationProperties properties;

    public ActivationServiceImpl(ActivationTokenService activationTokenService,
                                 EmailSender emailSender,
                                 PasswordPolicy passwordPolicy,
                                 PasswordEncoder passwordEncoder,
                                 AuditLogService auditLogService,
                                 ActivationProperties properties) {
        this.activationTokenService = activationTokenService;
        this.emailSender = emailSender;
        this.passwordPolicy = passwordPolicy;
        this.passwordEncoder = passwordEncoder;
        this.auditLogService = auditLogService;
        this.properties = properties;
    }

    @Override
    public void startActivation(AppUser user) {
        String link = properties.linkBase() + "?token=" + activationTokenService.issue(user);
        emailSender.send(new EmailMessage(user.getEmail(), SUBJECT, activationBody(user, link)));
    }

    @Override
    @Transactional(readOnly = true)
    public ActivationTokenStatus checkToken(String token) {
        return activationTokenService.statusOf(token);
    }

    @Override
    public void activate(ActivateAccountRequest request) {
        AppUser user = activationTokenService.requireValid(request.token());
        ensureValidPassword(request.password(), request.confirmPassword());
        user.completeActivation(passwordEncoder.encode(request.password()));
        auditLogService.record(user.getIdUser(), AuditAction.USER_ACTIVATED, user.getIdUser(), null, null);
    }

    private void ensureValidPassword(String password, String confirmPassword) {
        List<ErrorResponse.FieldError> errors = new ArrayList<>();
        passwordPolicy.findViolations(password)
                .forEach(message -> errors.add(new ErrorResponse.FieldError("password", message)));
        if (!password.equals(confirmPassword)) {
            errors.add(new ErrorResponse.FieldError("confirmPassword", "Passwords do not match."));
        }
        if (!errors.isEmpty()) {
            throw new FieldValidationException(errors);
        }
    }

    private String activationBody(AppUser user, String link) {
        return "Hi " + user.getFirstName() + ",\n\n"
                + "an account was created for you in the PayPulse merchant portal.\n\n"
                + "Set your password: " + link + "\n\n"
                + "The link is valid for " + properties.tokenTtl().toHours() + " hours and can be used only once.";
    }
}

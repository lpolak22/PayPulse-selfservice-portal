package com.example.paypulse.service.impl;

import com.example.paypulse.config.PasswordResetProperties;
import com.example.paypulse.dto.email.EmailMessage;
import com.example.paypulse.model.AppUser;
import com.example.paypulse.model.UserStatus;
import com.example.paypulse.repository.AppUserRepository;
import com.example.paypulse.service.EmailSender;
import com.example.paypulse.service.PasswordResetService;
import com.example.paypulse.service.PasswordResetTokenService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class PasswordResetServiceImpl implements PasswordResetService {

    private static final String SUBJECT = "Reset your PayPulse password";

    private final AppUserRepository appUserRepository;
    private final PasswordResetTokenService resetTokenService;
    private final EmailSender emailSender;
    private final PasswordResetProperties properties;

    public PasswordResetServiceImpl(AppUserRepository appUserRepository,
                                    PasswordResetTokenService resetTokenService,
                                    EmailSender emailSender,
                                    PasswordResetProperties properties) {
        this.appUserRepository = appUserRepository;
        this.resetTokenService = resetTokenService;
        this.emailSender = emailSender;
        this.properties = properties;
    }

    @Override
    @Transactional(readOnly = true)
    public void requestReset(String email) {
        appUserRepository.findUndeletedByEmail(email.trim())
                .filter(user -> user.getStatus() == UserStatus.ACTIVE)
                .ifPresent(this::sendResetEmail);
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

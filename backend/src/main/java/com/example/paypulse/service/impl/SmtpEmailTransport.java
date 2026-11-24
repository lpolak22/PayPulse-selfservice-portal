package com.example.paypulse.service.impl;

import com.example.paypulse.config.EmailProperties;
import com.example.paypulse.dto.email.OutgoingEmail;
import com.example.paypulse.service.EmailTransport;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
public class SmtpEmailTransport implements EmailTransport {

    private final JavaMailSender mailSender;
    private final EmailProperties properties;

    public SmtpEmailTransport(JavaMailSender mailSender, EmailProperties properties) {
        this.mailSender = mailSender;
        this.properties = properties;
    }

    @Override
    public void deliver(OutgoingEmail email) {
        mailSender.send(mimeMessage -> {
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage,
                    MimeMessageHelper.MULTIPART_MODE_RELATED, StandardCharsets.UTF_8.name());
            helper.setFrom(properties.fromAddress(), properties.fromName());
            helper.setTo(email.to());
            helper.setSubject(email.subject());
            helper.setText(email.text(), email.html());
        });
    }
}

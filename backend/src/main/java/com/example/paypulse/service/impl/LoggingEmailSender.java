package com.example.paypulse.service.impl;

import com.example.paypulse.dto.email.EmailMessage;
import com.example.paypulse.service.EmailSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class LoggingEmailSender implements EmailSender {

    private static final Logger log = LoggerFactory.getLogger(LoggingEmailSender.class);

    @Override
    public void send(EmailMessage message) {
        log.info("""

                ===== EMAIL (not sent, logged until PULSE-31 is done) =====
                To: {}
                Subject: {}

                {}
                ===================================================""",
                message.to(), message.subject(), message.body());
    }
}

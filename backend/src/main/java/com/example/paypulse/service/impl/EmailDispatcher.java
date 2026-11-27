package com.example.paypulse.service.impl;

import com.example.paypulse.config.EmailProperties;
import com.example.paypulse.dto.email.OutgoingEmail;
import com.example.paypulse.service.EmailTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.MailException;
import org.springframework.mail.MailParseException;
import org.springframework.mail.MailPreparationException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.Duration;

@Component
public class EmailDispatcher {

    private static final Logger log = LoggerFactory.getLogger(EmailDispatcher.class);

    private final EmailTransport transport;
    private final int maxRetries;
    private final Duration retryDelay;

    public EmailDispatcher(EmailTransport transport, EmailProperties properties) {
        this.transport = transport;
        this.maxRetries = properties.maxRetries();
        this.retryDelay = properties.retryDelay();
    }

    @Async
    @TransactionalEventListener(fallbackExecution = true)
    public void onOutgoingEmail(OutgoingEmail email) {
        dispatch(email);
    }

    public boolean dispatch(OutgoingEmail email) {
        for (int attempt = 1; ; attempt++) {
            try {
                transport.deliver(email);
                log.info("Email '{}' sent to {}", email.templateId(), mask(email.to()));
                return true;
            } catch (MailException exception) {
                if (!isRetryable(exception) || attempt > maxRetries) {
                    log.error("Email '{}' to {} failed after {} attempt(s)",
                            email.templateId(), mask(email.to()), attempt, exception);
                    return false;
                }
                log.warn("Email '{}' to {} failed ({}), retry {} of {}",
                        email.templateId(), mask(email.to()), exception.getMessage(), attempt, maxRetries);
                if (!pause(retryDelay.multipliedBy(attempt))) {
                    return false;
                }
            }
        }
    }

    private static boolean isRetryable(MailException exception) {
        return !(exception instanceof MailPreparationException || exception instanceof MailParseException);
    }

    private static boolean pause(Duration duration) {
        try {
            Thread.sleep(duration.toMillis());
            return true;
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            log.error("Interrupted while waiting to retry an email, giving up");
            return false;
        }
    }

    static String mask(String address) {
        int at = address == null ? -1 : address.indexOf('@');
        return at <= 0 ? "***" : address.charAt(0) + "***" + address.substring(at);
    }
}

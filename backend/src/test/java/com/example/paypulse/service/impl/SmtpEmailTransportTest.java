package com.example.paypulse.service.impl;

import com.example.paypulse.config.EmailProperties;
import com.example.paypulse.dto.email.OutgoingEmail;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Test;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SmtpEmailTransportTest {

    private final CapturingMailSender mailSender = new CapturingMailSender();
    private final SmtpEmailTransport transport = new SmtpEmailTransport(mailSender,
            new EmailProperties("no-reply@example.test", "PayPulse", 3, Duration.ZERO));

    @Test
    void theMessageCarriesSenderRecipientSubjectAndBothBodies() throws Exception {
        transport.deliver(new OutgoingEmail("ana@example.test", "activation", "Set your password",
                "<p>Hi Ana</p>", "Hi Ana"));

        MimeMessage message = mailSender.sent.getFirst();
        InternetAddress from = (InternetAddress) message.getFrom()[0];
        assertThat(from.getAddress()).isEqualTo("no-reply@example.test");
        assertThat(from.getPersonal()).isEqualTo("PayPulse");
        assertThat(message.getAllRecipients()).extracting(Object::toString).containsExactly("ana@example.test");
        assertThat(message.getSubject()).isEqualTo("Set your password");
        assertThat(raw(message))
                .doesNotContain("multipart/mixed")
                .contains("multipart/alternative")
                .containsPattern("text/plain;\\s?charset=UTF-8")
                .containsPattern("text/html;\\s?charset=UTF-8")
                .contains("Hi Ana")
                .contains("<p>Hi Ana</p>");
    }

    private static String raw(MimeMessage message) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        message.writeTo(out);
        return out.toString(StandardCharsets.UTF_8);
    }

    private static final class CapturingMailSender extends JavaMailSenderImpl {

        private final List<MimeMessage> sent = new ArrayList<>();

        @Override
        protected void doSend(MimeMessage[] mimeMessages, Object[] originalMessages) {
            sent.addAll(List.of(mimeMessages));
        }
    }
}

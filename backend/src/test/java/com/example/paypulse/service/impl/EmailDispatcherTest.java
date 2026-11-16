package com.example.paypulse.service.impl;

import com.example.paypulse.config.EmailProperties;
import com.example.paypulse.dto.email.OutgoingEmail;
import com.example.paypulse.service.EmailTransport;
import org.junit.jupiter.api.Test;
import org.springframework.mail.MailException;
import org.springframework.mail.MailPreparationException;
import org.springframework.mail.MailSendException;

import java.time.Duration;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class EmailDispatcherTest {

    private static final OutgoingEmail EMAIL =
            new OutgoingEmail("ana@example.test", "activation", "Subject", "<p>html</p>", "text");

    private final ScriptedTransport transport = new ScriptedTransport();
    private final EmailDispatcher dispatcher =
            new EmailDispatcher(transport, new EmailProperties("from@example.test", "PayPulse", 3, Duration.ZERO));

    @Test
    void aSuccessfulSendIsDeliveredOnce() {
        assertThat(dispatcher.dispatch(EMAIL)).isTrue();
        assertThat(transport.attempts).isEqualTo(1);
    }

    @Test
    void aProviderErrorIsRetriedUntilTheSendSucceeds() {
        transport.failWith(sendFailure(), sendFailure());

        assertThat(dispatcher.dispatch(EMAIL)).isTrue();
        assertThat(transport.attempts).isEqualTo(3);
    }

    @Test
    void theSendIsRetriedThreeTimesAndThenGivenUp() {
        transport.failWith(sendFailure(), sendFailure(), sendFailure(), sendFailure(), sendFailure());

        assertThat(dispatcher.dispatch(EMAIL)).isFalse();
        assertThat(transport.attempts).isEqualTo(4);
    }

    @Test
    void aMessageThatCannotBeBuiltIsNotRetried() {
        transport.failWith(new MailPreparationException("bad address"));

        assertThat(dispatcher.dispatch(EMAIL)).isFalse();
        assertThat(transport.attempts).isEqualTo(1);
    }

    @Test
    void recipientsAreMaskedInLogs() {
        assertThat(EmailDispatcher.mask("ana.horvat@example.test")).isEqualTo("a***@example.test");
        assertThat(EmailDispatcher.mask("not-an-address")).isEqualTo("***");
        assertThat(EmailDispatcher.mask(null)).isEqualTo("***");
    }

    private static MailSendException sendFailure() {
        return new MailSendException("421 Service not available");
    }

    private static final class ScriptedTransport implements EmailTransport {

        private final Deque<MailException> failures = new ArrayDeque<>();
        private int attempts;

        void failWith(MailException... exceptions) {
            failures.addAll(List.of(exceptions));
        }

        @Override
        public void deliver(OutgoingEmail email) {
            attempts++;
            if (!failures.isEmpty()) {
                throw failures.poll();
            }
        }
    }
}

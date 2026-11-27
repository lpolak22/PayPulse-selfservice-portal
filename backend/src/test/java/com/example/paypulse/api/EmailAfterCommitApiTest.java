package com.example.paypulse.api;

import com.example.paypulse.dto.email.OutgoingEmail;
import com.example.paypulse.service.EmailService;
import com.example.paypulse.support.ApiIntegrationTest;
import com.example.paypulse.support.CapturingEmailTransport;
import com.example.paypulse.support.UserFixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Duration;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional(propagation = Propagation.NOT_SUPPORTED)
class EmailAfterCommitApiTest extends ApiIntegrationTest {

    private static final Duration DELIVERY_TIMEOUT = Duration.ofSeconds(10);

    @Autowired
    private EmailService emailService;

    @Autowired
    private CapturingEmailTransport transport;

    @Autowired
    private PlatformTransactionManager transactionManager;

    private TransactionTemplate transaction;

    @BeforeEach
    void setUp() {
        transaction = new TransactionTemplate(transactionManager);
        transport.clear();
    }

    @Test
    void anEmailIsDeliveredOnlyAfterTheTransactionCommits() throws Exception {
        String to = UserFixtures.uniqueEmail();

        transaction.executeWithoutResult(status -> {
            sendActivation(to);
            assertThat(transport.delivered()).isEmpty();
        });

        OutgoingEmail delivered = transport.awaitNext(DELIVERY_TIMEOUT);
        assertThat(delivered).isNotNull();
        assertThat(delivered.to()).isEqualTo(to);
        assertThat(delivered.templateId()).isEqualTo("activation");
    }

    @Test
    void anEmailFromARolledBackTransactionIsNeverDelivered() throws Exception {
        String rolledBack = UserFixtures.uniqueEmail();
        String committed = UserFixtures.uniqueEmail();

        transaction.executeWithoutResult(status -> {
            sendActivation(rolledBack);
            status.setRollbackOnly();
        });
        transaction.executeWithoutResult(status -> sendActivation(committed));

        OutgoingEmail delivered = transport.awaitNext(DELIVERY_TIMEOUT);
        assertThat(delivered).isNotNull();
        assertThat(delivered.to()).isEqualTo(committed);
        assertThat(transport.delivered()).isEmpty();
    }

    private void sendActivation(String to) {
        emailService.send(to, "activation", Map.of(
                "firstName", "Iva",
                "link", "http://localhost:8080/open/activate?token=abc",
                "validHours", 24));
    }
}

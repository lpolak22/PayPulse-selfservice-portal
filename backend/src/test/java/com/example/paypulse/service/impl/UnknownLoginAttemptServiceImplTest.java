package com.example.paypulse.service.impl;

import com.example.paypulse.model.UnknownLoginAttempt;
import com.example.paypulse.repository.UnknownLoginAttemptRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HexFormat;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UnknownLoginAttemptServiceImplTest {

    private static final ZoneId ZONE = ZoneId.of("Europe/Zagreb");
    private static final LocalDateTime NOW = LocalDateTime.of(2026, 11, 4, 3, 0);

    @Mock
    private UnknownLoginAttemptRepository repository;

    private UnknownLoginAttemptServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new UnknownLoginAttemptServiceImpl(repository, Clock.fixed(NOW.atZone(ZONE).toInstant(), ZONE));
    }

    @Test
    void theEmailIsKeyedOnlyByTheSha256OfItsTrimmedLowerCaseForm() throws Exception {
        String expectedHash = sha256Hex("ghost@example.com");
        when(repository.findById(expectedHash)).thenReturn(Optional.of(new UnknownLoginAttempt(expectedHash)));

        UnknownLoginAttempt attempt = service.forEmail("  Ghost@Example.COM ");

        assertThat(attempt.getEmailHash()).isEqualTo(expectedHash).doesNotContain("ghost");
    }

    @Test
    void aMissingRowIsCreatedBeforeItIsLoaded() throws Exception {
        String hash = sha256Hex("ghost@example.com");
        when(repository.findById(hash)).thenReturn(Optional.of(new UnknownLoginAttempt(hash)));

        service.forEmail("ghost@example.com");

        InOrder order = inOrder(repository);
        order.verify(repository).insertIfMissing(hash);
        order.verify(repository).findById(hash);
    }

    @Test
    void cleanupDeletesAttemptsUntouchedForSevenDays() {
        service.deleteStaleAttempts();

        verify(repository).deleteNotUpdatedSince(NOW.minusDays(7));
    }

    private static String sha256Hex(String value) throws Exception {
        byte[] digest = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
        return HexFormat.of().formatHex(digest);
    }
}

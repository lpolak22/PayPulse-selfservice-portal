package com.example.paypulse.service.impl;

import com.example.paypulse.model.UnknownLoginAttempt;
import com.example.paypulse.repository.UnknownLoginAttemptRepository;
import com.example.paypulse.service.UnknownLoginAttemptService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.Locale;

@Service
@Transactional
public class UnknownLoginAttemptServiceImpl implements UnknownLoginAttemptService {

    private static final int CLEANUP_AFTER_DAYS = 7;

    private final UnknownLoginAttemptRepository unknownLoginAttemptRepository;
    private final Clock clock;

    public UnknownLoginAttemptServiceImpl(UnknownLoginAttemptRepository unknownLoginAttemptRepository, Clock clock) {
        this.unknownLoginAttemptRepository = unknownLoginAttemptRepository;
        this.clock = clock;
    }

    @Override
    public UnknownLoginAttempt forEmail(String email) {
        String emailHash = hash(email.strip().toLowerCase(Locale.ROOT));
        unknownLoginAttemptRepository.insertIfMissing(emailHash);
        return unknownLoginAttemptRepository.findById(emailHash).orElseThrow();
    }

    @Override
    @Scheduled(cron = "0 0 3 * * *")
    public void deleteStaleAttempts() {
        unknownLoginAttemptRepository.deleteNotUpdatedSince(LocalDateTime.now(clock).minusDays(CLEANUP_AFTER_DAYS));
    }

    private static String hash(String email) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(email.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is not available", exception);
        }
    }
}

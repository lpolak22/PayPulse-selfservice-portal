package com.example.paypulse.seed.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("seed")
public class SeedProperties {
    private final String testPassword;
    private final int transactionCount;
    private final int batchSize;

    public SeedProperties(
            @Value("${SEED_TEST_PASSWORD}") String testPassword,
            @Value("${SEED_TRANSACTION_COUNT}") int transactionCount,
            @Value("${SEED_BATCH_SIZE}") int batchSize) {
        this.testPassword = testPassword;
        this.transactionCount = transactionCount;
        this.batchSize = batchSize;
    }

    public String getTestPassword() {
        return testPassword;
    }

    public int getTransactionCount() {
        return transactionCount;
    }

    public int getBatchSize() {
        return batchSize;
    }
}

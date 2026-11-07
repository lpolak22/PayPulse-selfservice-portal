package com.example.paypulse.integration.sevenpay;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.function.Supplier;

class SevenPayRetry {

    private static final Logger log = LoggerFactory.getLogger(SevenPayRetry.class);

    private final int maxRetries;
    private final Duration delay;

    SevenPayRetry(int maxRetries, Duration delay) {
        this.maxRetries = maxRetries;
        this.delay = delay;
    }

    <T> T run(Supplier<T> call) {
        int attempt = 0;
        while (true) {
            try {
                return call.get();
            } catch (SevenPayException exception) {
                if (!exception.isRetryable() || attempt >= maxRetries) {
                    throw exception;
                }
                attempt++;
                log.warn("7Pay call failed ({}), retry {} of {}", exception.getMessage(), attempt, maxRetries);
                sleep(delay.multipliedBy(attempt));
            }
        }
    }

    private static void sleep(Duration duration) {
        try {
            Thread.sleep(duration.toMillis());
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new SevenPayException("Interrupted while waiting to retry 7Pay call", exception);
        }
    }
}

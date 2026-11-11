package com.example.paypulse.service.impl;

import com.example.paypulse.service.RateLimiter;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Component
public class InMemoryRateLimiter implements RateLimiter {

    private final Map<String, Window> windows = new ConcurrentHashMap<>();
    private final Clock clock;

    public InMemoryRateLimiter(Clock clock) {
        this.clock = clock;
    }

    @Override
    public boolean tryAcquire(String key, int limit, Duration window) {
        Instant now = clock.instant();
        Window updated = windows.compute(key, (k, current) ->
                current == null || current.isOver(now) ? new Window(now.plus(window), 1) : current.increment());
        return updated.count() <= limit;
    }

    @Scheduled(fixedDelay = 10, timeUnit = TimeUnit.MINUTES)
    public void removeExpiredWindows() {
        Instant now = clock.instant();
        windows.values().removeIf(window -> window.isOver(now));
    }

    private record Window(Instant endsAt, int count) {

        boolean isOver(Instant now) {
            return !now.isBefore(endsAt);
        }

        Window increment() {
            return new Window(endsAt, count + 1);
        }
    }
}

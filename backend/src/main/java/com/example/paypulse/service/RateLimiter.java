package com.example.paypulse.service;

import java.time.Duration;

public interface RateLimiter {

    boolean tryAcquire(String key, int limit, Duration window);
}

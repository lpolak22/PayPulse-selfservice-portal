package com.example.paypulse.repository.dashboard;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TrendBucketRow(LocalDateTime start, BigDecimal amount, long count) {
}

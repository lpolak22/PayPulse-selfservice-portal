package com.example.paypulse.dto.dashboard;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TrendPointResponse(LocalDateTime start, BigDecimal amount, long count) implements Serializable {
}

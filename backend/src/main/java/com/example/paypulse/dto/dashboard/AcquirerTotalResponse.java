package com.example.paypulse.dto.dashboard;

import java.math.BigDecimal;

public record AcquirerTotalResponse(String acquirer, BigDecimal amount, long count) {
}

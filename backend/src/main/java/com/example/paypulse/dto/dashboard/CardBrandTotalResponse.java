package com.example.paypulse.dto.dashboard;

import java.math.BigDecimal;

public record CardBrandTotalResponse(String brand, BigDecimal amount, long count) {
}

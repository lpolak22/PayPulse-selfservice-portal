package com.example.paypulse.repository.dashboard;

import java.math.BigDecimal;

public record LabeledTotalRow(String label, BigDecimal amount, long count) {
}

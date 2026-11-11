package com.example.paypulse.repository.dashboard;

import java.math.BigDecimal;

public record DashboardTotalsRow(
        BigDecimal totalAmount,
        BigDecimal previousTotalAmount,
        long totalCount,
        long declinedCount
) {
}

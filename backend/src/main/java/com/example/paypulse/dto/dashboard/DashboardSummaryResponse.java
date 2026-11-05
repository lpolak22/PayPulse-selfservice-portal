package com.example.paypulse.dto.dashboard;

import java.math.BigDecimal;
import java.util.List;

public record DashboardSummaryResponse(
        BigDecimal totalAmount,
        BigDecimal previousTotalAmount,
        long totalCount,
        long declinedCount,
        TrendResponse trend,
        List<CardBrandTotalResponse> byCardBrand,
        List<AcquirerTotalResponse> byAcquirer,
        List<LatestTransactionResponse> latestTransactions
) {
}

package com.example.paypulse.mapper;

import com.example.paypulse.dto.dashboard.AcquirerTotalResponse;
import com.example.paypulse.dto.dashboard.CardBrandTotalResponse;
import com.example.paypulse.dto.dashboard.DashboardSummaryResponse;
import com.example.paypulse.dto.dashboard.LatestTransactionResponse;
import com.example.paypulse.dto.dashboard.TrendPointResponse;
import com.example.paypulse.dto.dashboard.TrendResponse;
import com.example.paypulse.dto.dashboard.TrendUnit;
import com.example.paypulse.repository.dashboard.DashboardTotalsRow;
import com.example.paypulse.repository.dashboard.LabeledTotalRow;
import com.example.paypulse.repository.dashboard.LatestTransactionRow;
import com.example.paypulse.repository.dashboard.TrendBucketRow;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class DashboardMapper {

    private static final int AMOUNT_SCALE = 2;

    public DashboardSummaryResponse toResponse(DashboardTotalsRow totals,
                                               TrendResponse trend,
                                               List<LabeledTotalRow> cardBrandTotals,
                                               List<LabeledTotalRow> acquirerTotals,
                                               List<LatestTransactionRow> latestTransactions) {
        return new DashboardSummaryResponse(
                money(totals.totalAmount()),
                money(totals.previousTotalAmount()),
                totals.totalCount(),
                totals.declinedCount(),
                trend,
                cardBrandTotals.stream().map(this::toCardBrandTotal).toList(),
                acquirerTotals.stream().map(this::toAcquirerTotal).toList(),
                latestTransactions.stream().map(this::toLatestTransaction).toList()
        );
    }

    public TrendResponse toTrend(TrendUnit unit, List<LocalDateTime> bucketStarts, List<TrendBucketRow> buckets) {
        Map<LocalDateTime, TrendBucketRow> bucketsByStart = buckets.stream()
                .collect(Collectors.toMap(TrendBucketRow::start, Function.identity()));
        List<TrendPointResponse> points = bucketStarts.stream()
                .map(start -> toTrendPoint(start, bucketsByStart.get(start)))
                .toList();
        return new TrendResponse(unit, points);
    }

    private TrendPointResponse toTrendPoint(LocalDateTime start, TrendBucketRow bucket) {
        if (bucket == null) {
            return new TrendPointResponse(start, money(BigDecimal.ZERO), 0);
        }
        return new TrendPointResponse(start, money(bucket.amount()), bucket.count());
    }

    private CardBrandTotalResponse toCardBrandTotal(LabeledTotalRow row) {
        return new CardBrandTotalResponse(row.label(), money(row.amount()), row.count());
    }

    private AcquirerTotalResponse toAcquirerTotal(LabeledTotalRow row) {
        return new AcquirerTotalResponse(row.label(), money(row.amount()), row.count());
    }

    private LatestTransactionResponse toLatestTransaction(LatestTransactionRow row) {
        return new LatestTransactionResponse(
                row.cardBrand(),
                row.maskedPan(),
                row.outletName(),
                row.transactionAt(),
                money(row.amount()),
                row.status()
        );
    }

    private static BigDecimal money(BigDecimal amount) {
        return amount.setScale(AMOUNT_SCALE, RoundingMode.HALF_UP);
    }
}

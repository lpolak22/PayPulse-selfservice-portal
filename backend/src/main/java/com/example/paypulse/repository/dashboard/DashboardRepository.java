package com.example.paypulse.repository.dashboard;

import java.util.List;

public interface DashboardRepository {

    DashboardTotalsRow findTotals(DashboardQuery query);

    List<TrendBucketRow> findTrend(DashboardQuery query);

    List<LabeledTotalRow> findTotalsByCardBrand(DashboardQuery query);

    List<LabeledTotalRow> findTotalsByAcquirer(DashboardQuery query);

    List<LatestTransactionRow> findLatestTransactions(DashboardQuery query, int limit);
}

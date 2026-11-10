package com.example.paypulse.service.impl;

import com.example.paypulse.dto.dashboard.DashboardSummaryResponse;
import com.example.paypulse.dto.dashboard.TrendResponse;
import com.example.paypulse.mapper.DashboardMapper;
import com.example.paypulse.repository.dashboard.DashboardQuery;
import com.example.paypulse.repository.dashboard.DashboardRepository;
import com.example.paypulse.service.DashboardPeriod;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DashboardSummaryCalculator {

    private static final int LATEST_TRANSACTION_COUNT = 5;

    private final DashboardRepository dashboardRepository;
    private final DashboardMapper dashboardMapper;

    public DashboardSummaryCalculator(DashboardRepository dashboardRepository, DashboardMapper dashboardMapper) {
        this.dashboardRepository = dashboardRepository;
        this.dashboardMapper = dashboardMapper;
    }

    @Transactional(readOnly = true, isolation = Isolation.REPEATABLE_READ)
    public DashboardSummaryResponse calculate(long organisationId, DashboardPeriod period) {
        DashboardQuery query = period.toQuery(organisationId);
        TrendResponse trend = dashboardMapper.toTrend(
                period.trendUnit(), period.trendBucketStarts(), dashboardRepository.findTrend(query));
        return dashboardMapper.toResponse(
                dashboardRepository.findTotals(query),
                trend,
                dashboardRepository.findTotalsByCardBrand(query),
                dashboardRepository.findTotalsByAcquirer(query),
                dashboardRepository.findLatestTransactions(query, LATEST_TRANSACTION_COUNT)
        );
    }
}

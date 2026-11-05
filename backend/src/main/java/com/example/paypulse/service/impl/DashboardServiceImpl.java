package com.example.paypulse.service.impl;

import com.example.paypulse.dto.dashboard.DashboardSummaryResponse;
import com.example.paypulse.security.CurrentOrganisationScope;
import com.example.paypulse.service.DashboardPeriod;
import com.example.paypulse.service.DashboardService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class DashboardServiceImpl implements DashboardService {

    private final DashboardPeriodResolver periodResolver;
    private final DashboardSummaryCalculator summaryCalculator;
    private final CurrentOrganisationScope organisationScope;

    public DashboardServiceImpl(DashboardPeriodResolver periodResolver,
                                DashboardSummaryCalculator summaryCalculator,
                                CurrentOrganisationScope organisationScope) {
        this.periodResolver = periodResolver;
        this.summaryCalculator = summaryCalculator;
        this.organisationScope = organisationScope;
    }

    @Override
    public DashboardSummaryResponse getSummary(LocalDate from, LocalDate to) {
        DashboardPeriod period = periodResolver.resolve(from, to);
        return summaryCalculator.calculate(organisationScope.organisationId(), period);
    }
}

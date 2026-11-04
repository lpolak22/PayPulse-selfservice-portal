package com.example.paypulse.service.impl;

import com.example.paypulse.service.DashboardPeriod;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDate;

@Component
public class DashboardPeriodResolver {

    private static final int DEFAULT_DAY_COUNT = 30;

    private final Clock clock;

    public DashboardPeriodResolver(Clock clock) {
        this.clock = clock;
    }

    public DashboardPeriod resolve(LocalDate from, LocalDate to) {
        if (from == null || to == null) {
            return lastDays(DEFAULT_DAY_COUNT);
        }
        return new DashboardPeriod(from, to);
    }

    private DashboardPeriod lastDays(int dayCount) {
        LocalDate today = LocalDate.now(clock.withZone(DashboardPeriod.ZONE));
        return new DashboardPeriod(today.minusDays(dayCount - 1L), today);
    }
}

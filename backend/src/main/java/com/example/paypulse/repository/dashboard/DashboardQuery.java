package com.example.paypulse.repository.dashboard;

import java.time.Instant;
import java.time.ZoneId;

public record DashboardQuery(
        long organisationId,
        Instant previousStart,
        Instant start,
        Instant end,
        String trendUnit,
        ZoneId zone
) {
}

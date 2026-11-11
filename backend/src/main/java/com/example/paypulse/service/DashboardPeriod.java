package com.example.paypulse.service;

import com.example.paypulse.dto.dashboard.TrendUnit;
import com.example.paypulse.repository.dashboard.DashboardQuery;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Stream;

public record DashboardPeriod(LocalDate from, LocalDate to) {

    public static final ZoneId ZONE = ZoneId.of("Europe/Zagreb");

    private static final int HOURS_PER_DAY = 24;

    public long dayCount() {
        return ChronoUnit.DAYS.between(from, to) + 1;
    }

    public TrendUnit trendUnit() {
        return from.equals(to) ? TrendUnit.HOUR : TrendUnit.DAY;
    }

    public List<LocalDateTime> trendBucketStarts() {
        if (trendUnit() == TrendUnit.HOUR) {
            return Stream.iterate(from.atStartOfDay(), hour -> hour.plusHours(1)).limit(HOURS_PER_DAY).toList();
        }
        return from.datesUntil(to.plusDays(1)).map(LocalDate::atStartOfDay).toList();
    }

    public DashboardQuery toQuery(long organisationId) {
        return new DashboardQuery(
                organisationId,
                startOf(from.minusDays(dayCount())),
                startOf(from),
                startOf(to.plusDays(1)),
                trendUnit().name(),
                ZONE
        );
    }

    private static Instant startOf(LocalDate day) {
        return day.atStartOfDay(ZONE).toInstant();
    }
}

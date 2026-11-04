package com.example.paypulse.service.impl;

import com.example.paypulse.dto.common.ErrorResponse.FieldError;
import com.example.paypulse.exception.FieldValidationException;
import com.example.paypulse.service.DashboardPeriod;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Component
public class DashboardPeriodResolver {

    private static final int DEFAULT_DAY_COUNT = 30;
    private static final int MAX_DAY_COUNT = 366;
    private static final String FROM = "from";
    private static final String TO = "to";

    private final Clock clock;

    public DashboardPeriodResolver(Clock clock) {
        this.clock = clock;
    }

    public DashboardPeriod resolve(LocalDate from, LocalDate to) {
        if (from == null && to == null) {
            return lastDays(DEFAULT_DAY_COUNT);
        }
        invalidField(from, to).ifPresent(error -> {
            throw new FieldValidationException(List.of(error));
        });
        return new DashboardPeriod(from, to);
    }

    private static Optional<FieldError> invalidField(LocalDate from, LocalDate to) {
        if (from == null) {
            return Optional.of(new FieldError(FROM, "must be sent together with to"));
        }
        if (to == null) {
            return Optional.of(new FieldError(TO, "must be sent together with from"));
        }
        if (from.isAfter(to)) {
            return Optional.of(new FieldError(FROM, "must not be after to"));
        }
        if (new DashboardPeriod(from, to).dayCount() > MAX_DAY_COUNT) {
            return Optional.of(new FieldError(TO, "must give a range of at most " + MAX_DAY_COUNT + " days, both days included"));
        }
        return Optional.empty();
    }

    private DashboardPeriod lastDays(int dayCount) {
        LocalDate today = LocalDate.now(clock.withZone(DashboardPeriod.ZONE));
        return new DashboardPeriod(today.minusDays(dayCount - 1L), today);
    }
}

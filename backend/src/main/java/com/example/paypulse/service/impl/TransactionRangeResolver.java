package com.example.paypulse.service.impl;

import com.example.paypulse.exception.ApiException;
import com.example.paypulse.exception.ErrorCode;
import com.example.paypulse.service.TransactionRange;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;

@Component
public class TransactionRangeResolver {

    private static final ZoneId ZONE = ZoneId.of("Europe/Zagreb");
    private static final int DEFAULT_DAY_COUNT = 7;
    private static final int MAX_MONTH_COUNT = 13;

    private final Clock clock;

    public TransactionRangeResolver(Clock clock) {
        this.clock = clock;
    }

    public TransactionRange resolve(OffsetDateTime from, OffsetDateTime to) {
        Instant end = to != null ? to.toInstant() : clock.instant();
        Instant start = from != null ? from.toInstant() : end.minus(Duration.ofDays(DEFAULT_DAY_COUNT));
        if (start.isAfter(end)) {
            throw new ApiException(ErrorCode.BAD_REQUEST, "from must not be after to.");
        }
        if (end.isAfter(latestAllowedEnd(start))) {
            throw new ApiException(ErrorCode.RANGE_TOO_LARGE);
        }
        return new TransactionRange(start, end);
    }

    private static Instant latestAllowedEnd(Instant start) {
        return start.atZone(ZONE).plusMonths(MAX_MONTH_COUNT).toInstant();
    }
}

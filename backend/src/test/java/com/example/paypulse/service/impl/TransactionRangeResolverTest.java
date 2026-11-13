package com.example.paypulse.service.impl;

import com.example.paypulse.exception.ApiException;
import com.example.paypulse.exception.ErrorCode;
import com.example.paypulse.service.TransactionRange;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TransactionRangeResolverTest {

    private static final ZoneId ZAGREB = ZoneId.of("Europe/Zagreb");
    private static final Instant NOW = Instant.parse("2026-11-13T10:00:00Z");

    private final TransactionRangeResolver resolver = new TransactionRangeResolver(Clock.fixed(NOW, ZoneOffset.UTC));

    @Test
    void withoutFromAndToUsesLastSevenDaysEndingNow() {
        TransactionRange range = resolver.resolve(null, null);

        assertThat(range.start()).isEqualTo(NOW.minus(Duration.ofDays(7)));
        assertThat(range.end()).isEqualTo(NOW);
    }

    @Test
    void withoutFromUsesSevenDaysBeforeTo() {
        OffsetDateTime to = startOf(LocalDate.of(2026, 10, 15));

        TransactionRange range = resolver.resolve(null, to);

        assertThat(range.start()).isEqualTo(to.toInstant().minus(Duration.ofDays(7)));
        assertThat(range.end()).isEqualTo(to.toInstant());
    }

    @Test
    void withoutToUsesNow() {
        OffsetDateTime from = startOf(LocalDate.of(2026, 11, 1));

        TransactionRange range = resolver.resolve(from, null);

        assertThat(range.start()).isEqualTo(from.toInstant());
        assertThat(range.end()).isEqualTo(NOW);
    }

    @Test
    void differentOffsetsResolveToTheSameInstants() {
        TransactionRange range = resolver.resolve(
                OffsetDateTime.parse("2026-11-01T01:00:00+01:00"),
                OffsetDateTime.parse("2026-11-02T00:00:00Z"));

        assertThat(range.start()).isEqualTo(Instant.parse("2026-11-01T00:00:00Z"));
        assertThat(range.end()).isEqualTo(Instant.parse("2026-11-02T00:00:00Z"));
    }

    @Test
    void fromAfterToIsBadRequest() {
        OffsetDateTime to = startOf(LocalDate.of(2026, 10, 1));

        expectError(to.plusSeconds(1), to, ErrorCode.BAD_REQUEST);
    }

    @Test
    void fromAfterNowWithoutToIsBadRequest() {
        expectError(NOW.plusSeconds(1).atOffset(ZoneOffset.UTC), null, ErrorCode.BAD_REQUEST);
    }

    @Test
    void equalFromAndToIsAllowed() {
        OffsetDateTime moment = startOf(LocalDate.of(2026, 10, 1));

        TransactionRange range = resolver.resolve(moment, moment);

        assertThat(range.start()).isEqualTo(range.end());
    }

    @ParameterizedTest
    @ValueSource(strings = {"2025-10-01", "2025-12-01", "2025-06-15", "2025-03-30"})
    void exactlyThirteenMonthsInZagrebIsAllowed(LocalDate firstDay) {
        OffsetDateTime from = startOf(firstDay);
        OffsetDateTime to = startOf(firstDay.plusMonths(13));

        TransactionRange range = resolver.resolve(from, to);

        assertThat(range.start()).isEqualTo(from.toInstant());
        assertThat(range.end()).isEqualTo(to.toInstant());
    }

    @ParameterizedTest
    @ValueSource(strings = {"2025-10-01", "2025-12-01", "2025-06-15", "2025-03-30"})
    void oneSecondOverThirteenMonthsIsRangeTooLarge(LocalDate firstDay) {
        OffsetDateTime from = startOf(firstDay);

        expectError(from, startOf(firstDay.plusMonths(13)).plusSeconds(1), ErrorCode.RANGE_TOO_LARGE);
    }

    @Test
    void thirteenMonthsAreCountedInZagrebTimeNotUtc() {
        OffsetDateTime from = OffsetDateTime.parse("2025-10-01T00:00:00+02:00");
        OffsetDateTime toInUtc = OffsetDateTime.parse("2026-10-31T23:00:00Z");

        assertThat(resolver.resolve(from, toInUtc).end()).isEqualTo(toInUtc.toInstant());
        expectError(from, toInUtc.plusSeconds(1), ErrorCode.RANGE_TOO_LARGE);
    }

    @Test
    void fromMoreThanThirteenMonthsAgoWithoutToIsRangeTooLarge() {
        OffsetDateTime from = NOW.atZone(ZAGREB).minusMonths(13).minusSeconds(1).toOffsetDateTime();

        expectError(from, null, ErrorCode.RANGE_TOO_LARGE);
    }

    private void expectError(OffsetDateTime from, OffsetDateTime to, ErrorCode code) {
        assertThatThrownBy(() -> resolver.resolve(from, to))
                .isInstanceOf(ApiException.class)
                .extracting(e -> ((ApiException) e).getErrorCode()).isEqualTo(code);
    }

    private static OffsetDateTime startOf(LocalDate day) {
        return day.atStartOfDay(ZAGREB).toOffsetDateTime();
    }
}

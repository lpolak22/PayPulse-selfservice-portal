package com.example.paypulse.repository.transaction;

import com.example.paypulse.model.CardBrand;
import com.example.paypulse.model.SalesChannel;
import com.example.paypulse.model.TransactionStatus;
import com.example.paypulse.model.TransactionType;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Sort;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

class TransactionWhereClauseTest {

    private static final long ORGANISATION_ID = 42L;
    private static final Instant START = Instant.parse("2026-10-31T23:00:00Z");
    private static final Instant END = Instant.parse("2026-11-07T23:00:00Z");

    @Test
    void withoutFiltersOnlyOrganisationAndRangeAreBound() {
        TransactionWhereClause where = TransactionWhereClause.of(new Filters().query());

        assertThat(where.sql()).isEmpty();
        assertThat(where.params()).containsOnly(
                entry("organisationId", ORGANISATION_ID),
                entry("start", OffsetDateTime.parse("2026-10-31T23:00:00Z")),
                entry("end", OffsetDateTime.parse("2026-11-07T23:00:00Z")));
        assertThat(((OffsetDateTime) where.params().get("start")).getOffset()).isEqualTo(ZoneOffset.UTC);
    }

    @Test
    void statusesUseInList() {
        Filters filters = new Filters();
        filters.statuses = List.of(TransactionStatus.APPROVED, TransactionStatus.DECLINED);

        TransactionWhereClause where = TransactionWhereClause.of(filters.query());

        assertThat(where.sql()).isEqualTo(" AND t.status IN (:statuses)");
        assertThat(where.params()).containsEntry("statuses", List.of("APPROVED", "DECLINED"));
    }

    @Test
    void rawDataFiltersUseJsonbContainmentJoinedWithOr() {
        Filters filters = new Filters();
        filters.types = List.of(TransactionType.SALE, TransactionType.REFUND);
        filters.cardBrands = List.of(CardBrand.VISA);
        filters.salesChannels = List.of(SalesChannel.ECOM, SalesChannel.MOTO);

        TransactionWhereClause where = TransactionWhereClause.of(filters.query());

        assertThat(where.sql()).isEqualTo(
                " AND (t.raw_data @> CAST(:type0 AS jsonb) OR t.raw_data @> CAST(:type1 AS jsonb))"
                        + " AND (t.raw_data @> CAST(:cardBrand0 AS jsonb))"
                        + " AND (t.raw_data @> CAST(:salesChannel0 AS jsonb)"
                        + " OR t.raw_data @> CAST(:salesChannel1 AS jsonb))");
        assertThat(where.params()).contains(
                entry("type0", "{\"type\":\"SALE\"}"),
                entry("type1", "{\"type\":\"REFUND\"}"),
                entry("cardBrand0", "{\"card_brand\":\"VISA\"}"),
                entry("salesChannel0", "{\"sales_channel\":\"ECOM\"}"),
                entry("salesChannel1", "{\"sales_channel\":\"MOTO\"}"));
    }

    @Test
    void duplicateValuesAreBoundOnce() {
        Filters filters = new Filters();
        filters.statuses = List.of(TransactionStatus.APPROVED, TransactionStatus.APPROVED);
        filters.types = List.of(TransactionType.SALE, TransactionType.SALE);

        TransactionWhereClause where = TransactionWhereClause.of(filters.query());

        assertThat(where.params()).containsEntry("statuses", List.of("APPROVED"))
                .containsKey("type0")
                .doesNotContainKey("type1");
        assertThat(where.sql()).doesNotContain(" OR ");
    }

    @Test
    void exactMatchFiltersUseEquals() {
        Filters filters = new Filters();
        filters.authCode = "A1B2C3";
        filters.rrn = "123456789012";
        filters.outletId = 7L;
        filters.terminalId = "T0000001";

        TransactionWhereClause where = TransactionWhereClause.of(filters.query());

        assertThat(where.sql()).isEqualTo(" AND t.raw_data ->> 'auth_code' = :authCode"
                + " AND t.raw_data ->> 'rrn' = :rrn"
                + " AND o.id_outlet = :outletId"
                + " AND te.terminal_id = :terminalId");
        assertThat(where.params()).contains(entry("authCode", "A1B2C3"), entry("rrn", "123456789012"),
                entry("outletId", 7L), entry("terminalId", "T0000001"));
    }

    @Test
    void differentFiltersAreJoinedWithAnd() {
        Filters filters = new Filters();
        filters.statuses = List.of(TransactionStatus.APPROVED);
        filters.cardBrands = List.of(CardBrand.VISA);
        filters.rrn = "123456789012";

        TransactionWhereClause where = TransactionWhereClause.of(filters.query());

        assertThat(where.sql()).isEqualTo(" AND t.status IN (:statuses)"
                + " AND (t.raw_data @> CAST(:cardBrand0 AS jsonb))"
                + " AND t.raw_data ->> 'rrn' = :rrn");
    }

    @Test
    void filterValuesAreBoundAsParametersNotInlined() {
        Filters filters = new Filters();
        filters.rrn = "x' OR '1'='1";
        filters.authCode = "'; DROP TABLE transaction; --";
        filters.terminalId = "T' OR 1=1 --";

        TransactionWhereClause where = TransactionWhereClause.of(filters.query());

        assertThat(where.sql()).doesNotContain("'1'='1", "DROP TABLE", "1=1");
        assertThat(where.params()).contains(entry("rrn", filters.rrn), entry("authCode", filters.authCode),
                entry("terminalId", filters.terminalId));
    }

    private static final class Filters {
        private List<TransactionStatus> statuses = List.of();
        private List<TransactionType> types = List.of();
        private List<CardBrand> cardBrands = List.of();
        private List<SalesChannel> salesChannels = List.of();
        private String authCode;
        private String rrn;
        private Long outletId;
        private String terminalId;

        private TransactionSearchQuery query() {
            return new TransactionSearchQuery(ORGANISATION_ID, START, END, statuses, types, cardBrands,
                    salesChannels, authCode, rrn, outletId, terminalId, TransactionSortField.DATE,
                    Sort.Direction.DESC, 20, 0);
        }
    }
}

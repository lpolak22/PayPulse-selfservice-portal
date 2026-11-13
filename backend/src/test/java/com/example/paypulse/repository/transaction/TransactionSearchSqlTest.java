package com.example.paypulse.repository.transaction;

import com.example.paypulse.model.TransactionStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.data.domain.Sort;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TransactionSearchSqlTest {

    private static final String ORGANISATION_SCOPE = "WHERE m.id_organisation = :organisationId";
    private static final TransactionWhereClause NO_FILTERS = where(List.of());
    private static final TransactionWhereClause STATUS_FILTER = where(List.of(TransactionStatus.APPROVED));

    @ParameterizedTest
    @CsvSource({"DATE, ASC, t.transaction_at", "DATE, DESC, t.transaction_at", "AMOUNT, ASC, t.amount",
            "AMOUNT, DESC, t.amount"})
    void pageOrdersBySortFieldThenIdInSameDirection(TransactionSortField field, Sort.Direction direction,
                                                    String column) {
        String sql = TransactionSearchSql.page(NO_FILTERS, field, direction);

        assertThat(sql).endsWith(" ORDER BY " + column + " " + direction.name()
                + ", t.id_transaction " + direction.name() + " LIMIT :limit OFFSET :offset");
    }

    @Test
    void pageAddsFiltersBeforeOrderBy() {
        String sql = TransactionSearchSql.page(STATUS_FILTER, TransactionSortField.DATE, Sort.Direction.DESC);

        assertThat(sql).contains(STATUS_FILTER.sql() + " ORDER BY ");
    }

    @Test
    void countHasNoOrderOrLimit() {
        String sql = TransactionSearchSql.count(STATUS_FILTER);

        assertThat(sql).startsWith("SELECT COUNT(*)")
                .endsWith(STATUS_FILTER.sql())
                .doesNotContain("ORDER BY", "LIMIT", "OFFSET");
    }

    @Test
    void pageAndCountShareOrganisationAndRangeConditions() {
        List<String> queries = List.of(
                TransactionSearchSql.page(NO_FILTERS, TransactionSortField.DATE, Sort.Direction.DESC),
                TransactionSearchSql.count(NO_FILTERS));

        assertThat(queries).allSatisfy(sql -> assertThat(sql)
                .contains(ORGANISATION_SCOPE, "t.transaction_at >= :start", "t.transaction_at < :end"));
    }

    @Test
    void pageSelectsOnlyMaskedCardDataAndNeverWholeRawData() {
        String sql = TransactionSearchSql.page(NO_FILTERS, TransactionSortField.DATE, Sort.Direction.DESC);
        String selectList = sql.substring(0, sql.indexOf(" FROM "));

        assertThat(selectList).contains("t.raw_data ->> 'masked_pan' AS masked_pan")
                .doesNotContainPattern("t\\.raw_data\\s+AS")
                .doesNotContainPattern("t\\.raw_data\\s*,")
                .doesNotContain("'pan'", "card_number");
    }

    private static TransactionWhereClause where(List<TransactionStatus> statuses) {
        return TransactionWhereClause.of(new TransactionSearchQuery(1L, Instant.EPOCH, Instant.EPOCH.plusSeconds(60),
                statuses, List.of(), List.of(), List.of(), null, null, null, null,
                TransactionSortField.DATE, Sort.Direction.DESC, 20, 0));
    }
}

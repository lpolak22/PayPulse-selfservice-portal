package com.example.paypulse.repository.dashboard;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Repository
public class JdbcDashboardRepository implements DashboardRepository {

    private final JdbcClient jdbcClient;

    public JdbcDashboardRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    @Override
    public DashboardTotalsRow findTotals(DashboardQuery query) {
        return inPeriod(DashboardSql.TOTALS, query)
                .param("previousStart", timestamp(query.previousStart()))
                .query((rs, rowNum) -> toTotalsRow(rs))
                .single();
    }

    @Override
    public List<TrendBucketRow> findTrend(DashboardQuery query) {
        return inPeriod(DashboardSql.TREND, query)
                .param("unit", query.trendUnit())
                .param("zone", query.zone().getId())
                .query((rs, rowNum) -> toTrendBucketRow(rs))
                .list();
    }

    @Override
    public List<LabeledTotalRow> findTotalsByCardBrand(DashboardQuery query) {
        return inPeriod(DashboardSql.TOTALS_BY_CARD_BRAND, query)
                .query((rs, rowNum) -> toLabeledTotalRow(rs))
                .list();
    }

    @Override
    public List<LabeledTotalRow> findTotalsByAcquirer(DashboardQuery query) {
        return inPeriod(DashboardSql.TOTALS_BY_ACQUIRER, query)
                .query((rs, rowNum) -> toLabeledTotalRow(rs))
                .list();
    }

    @Override
    public List<LatestTransactionRow> findLatestTransactions(DashboardQuery query, int limit) {
        return inPeriod(DashboardSql.LATEST_TRANSACTIONS, query)
                .param("limit", limit)
                .query((rs, rowNum) -> toLatestTransactionRow(rs))
                .list();
    }

    private JdbcClient.StatementSpec inPeriod(String sql, DashboardQuery query) {
        return jdbcClient.sql(sql)
                .param("organisationId", query.organisationId())
                .param("start", timestamp(query.start()))
                .param("end", timestamp(query.end()));
    }

    private static OffsetDateTime timestamp(Instant instant) {
        return instant.atOffset(ZoneOffset.UTC);
    }

    private static DashboardTotalsRow toTotalsRow(ResultSet rs) throws SQLException {
        return new DashboardTotalsRow(
                rs.getBigDecimal("total_amount"),
                rs.getBigDecimal("previous_total_amount"),
                rs.getLong("total_count"),
                rs.getLong("declined_count")
        );
    }

    private static TrendBucketRow toTrendBucketRow(ResultSet rs) throws SQLException {
        return new TrendBucketRow(
                rs.getObject("bucket_start", LocalDateTime.class),
                rs.getBigDecimal("amount"),
                rs.getLong("count")
        );
    }

    private static LabeledTotalRow toLabeledTotalRow(ResultSet rs) throws SQLException {
        return new LabeledTotalRow(rs.getString("label"), rs.getBigDecimal("amount"), rs.getLong("count"));
    }

    private static LatestTransactionRow toLatestTransactionRow(ResultSet rs) throws SQLException {
        return new LatestTransactionRow(
                rs.getString("card_brand"),
                rs.getString("masked_pan"),
                rs.getString("outlet_name"),
                rs.getObject("transaction_at", OffsetDateTime.class).toInstant(),
                rs.getBigDecimal("amount"),
                rs.getString("status")
        );
    }
}

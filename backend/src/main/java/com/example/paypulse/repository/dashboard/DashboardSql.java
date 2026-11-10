package com.example.paypulse.repository.dashboard;

import static com.example.paypulse.repository.dashboard.DashboardRules.COUNTED_TRANSACTION;
import static com.example.paypulse.repository.dashboard.DashboardRules.DECLINED_TRANSACTION;

final class DashboardSql {

    private static final String ORGANISATION_TRANSACTIONS = """
             FROM transaction t
             JOIN terminal te ON te.id_terminal = t.id_terminal
             JOIN outlet o ON o.id_outlet = te.id_outlet
             JOIN merchant m ON m.id_merchant = o.id_merchant
            """;

    private static final String IN_ORGANISATION_BEFORE_END = """
             WHERE m.id_organisation = :organisationId
               AND t.transaction_at < :end
            """;

    private static final String IN_CURRENT_PERIOD = "t.transaction_at >= :start";

    private static final String COUNTED_IN_PERIOD = IN_ORGANISATION_BEFORE_END
            + " AND " + IN_CURRENT_PERIOD
            + " AND " + COUNTED_TRANSACTION;

    static final String TOTALS = "SELECT"
            + " COALESCE(SUM(t.amount) FILTER (WHERE " + IN_CURRENT_PERIOD + " AND " + COUNTED_TRANSACTION + "), 0)"
            + " AS total_amount,"
            + " COUNT(*) FILTER (WHERE " + IN_CURRENT_PERIOD + " AND " + COUNTED_TRANSACTION + ") AS total_count,"
            + " COUNT(*) FILTER (WHERE " + IN_CURRENT_PERIOD + " AND " + DECLINED_TRANSACTION + ") AS declined_count,"
            + " COALESCE(SUM(t.amount) FILTER (WHERE NOT " + IN_CURRENT_PERIOD + " AND " + COUNTED_TRANSACTION + "), 0)"
            + " AS previous_total_amount"
            + ORGANISATION_TRANSACTIONS
            + IN_ORGANISATION_BEFORE_END
            + " AND t.transaction_at >= :previousStart";

    static final String TREND = """
            SELECT date_trunc(CAST(:unit AS text), t.transaction_at AT TIME ZONE CAST(:zone AS text)) AS bucket_start,
                   SUM(t.amount) AS amount,
                   COUNT(*) AS count
            """
            + ORGANISATION_TRANSACTIONS
            + COUNTED_IN_PERIOD
            + " GROUP BY bucket_start";

    static final String TOTALS_BY_CARD_BRAND = """
            SELECT t.raw_data ->> 'card_brand' AS label,
                   SUM(t.amount) AS amount,
                   COUNT(*) AS count
            """
            + ORGANISATION_TRANSACTIONS
            + COUNTED_IN_PERIOD
            + " GROUP BY label ORDER BY amount DESC, label";

    static final String TOTALS_BY_ACQUIRER = """
            SELECT a.name AS label,
                   SUM(t.amount) AS amount,
                   COUNT(*) AS count
            """
            + ORGANISATION_TRANSACTIONS
            + " JOIN acquirer a ON a.id_acquirer = t.id_acquirer"
            + COUNTED_IN_PERIOD
            + " GROUP BY a.id_acquirer, a.name ORDER BY amount DESC, a.name";

    static final String LATEST_TRANSACTIONS = """
            SELECT t.raw_data ->> 'card_brand' AS card_brand,
                   t.raw_data ->> 'masked_pan' AS masked_pan,
                   o.name AS outlet_name,
                   t.transaction_at AS transaction_at,
                   t.amount AS amount,
                   t.status AS status
            """
            + ORGANISATION_TRANSACTIONS
            + IN_ORGANISATION_BEFORE_END
            + " AND " + IN_CURRENT_PERIOD
            + " ORDER BY t.transaction_at DESC, t.id_transaction DESC"
            + " LIMIT :limit";

    private DashboardSql() {
    }
}

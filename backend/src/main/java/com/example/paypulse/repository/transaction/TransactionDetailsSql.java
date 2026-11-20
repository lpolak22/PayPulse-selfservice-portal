package com.example.paypulse.repository.transaction;

import static com.example.paypulse.repository.transaction.TransactionRawDataKeys.AUTH_CODE;
import static com.example.paypulse.repository.transaction.TransactionRawDataKeys.CARD_BRAND;
import static com.example.paypulse.repository.transaction.TransactionRawDataKeys.MASKED_PAN;
import static com.example.paypulse.repository.transaction.TransactionRawDataKeys.RRN;
import static com.example.paypulse.repository.transaction.TransactionRawDataKeys.SALES_CHANNEL;
import static com.example.paypulse.repository.transaction.TransactionRawDataKeys.TYPE;
import static com.example.paypulse.repository.transaction.TransactionRawDataKeys.text;

final class TransactionDetailsSql {

    static final String FIND_IN_ORGANISATION = """
            SELECT t.id_transaction AS id,
                   t.transaction_at AS transaction_at,
                   t.amount AS amount,
                   t.currency AS currency,
                   t.status AS status,
                   %s AS type,
                   %s AS card_brand,
                   %s AS masked_pan,
                   %s AS sales_channel,
                   %s AS auth_code,
                   %s AS rrn,
                   a.name AS acquirer_name,
                   te.id_terminal AS id_terminal,
                   te.terminal_id AS terminal_id,
                   te.type AS terminal_type,
                   o.id_outlet AS id_outlet,
                   o.name AS outlet_name,
                   o.address AS outlet_address,
                   o.city AS outlet_city,
                   o.postal_code AS outlet_postal_code,
                   m.id_merchant AS id_merchant,
                   m.name AS merchant_name
              FROM transaction t
              JOIN acquirer a ON a.id_acquirer = t.id_acquirer
              JOIN terminal te ON te.id_terminal = t.id_terminal
              JOIN outlet o ON o.id_outlet = te.id_outlet
              JOIN merchant m ON m.id_merchant = o.id_merchant
             WHERE t.id_transaction = :id
               AND m.id_organisation = :organisationId
            """.formatted(text(TYPE), text(CARD_BRAND), text(MASKED_PAN), text(SALES_CHANNEL), text(AUTH_CODE),
            text(RRN));

    private TransactionDetailsSql() {
    }
}

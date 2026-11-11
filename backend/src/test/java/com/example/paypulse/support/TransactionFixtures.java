package com.example.paypulse.support;

import com.example.paypulse.model.CardBrand;
import com.example.paypulse.model.TransactionStatus;
import com.example.paypulse.model.TransactionType;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public final class TransactionFixtures {

    private static final String MCC_CODE = "5411";

    private final JdbcTemplate jdbc;

    public TransactionFixtures(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public long terminal(long organisationId) {
        long outletId = outlet(merchant(organisationId));
        jdbc.update("insert into mcc_code (mcc_code, description) values (?, 'Test') on conflict do nothing", MCC_CODE);
        return jdbc.queryForObject("""
                        insert into terminal (id_outlet, mcc_code, terminal_id, type, status)
                        values (?, ?, ?, 'POS', 'ACTIVE')
                        returning id_terminal
                        """, Long.class, outletId, MCC_CODE, uniqueCode(16));
    }

    public long acquirer() {
        String code = uniqueCode(20);
        return jdbc.queryForObject("insert into acquirer (code, name) values (?, ?) returning id_acquirer",
                Long.class, code, "Test acquirer " + code);
    }

    public void add(TestTransaction transaction) {
        jdbc.update("""
                        insert into transaction (id_terminal, id_acquirer, amount, currency, status, raw_data)
                        values (?, ?, ?, ?, ?, jsonb_build_object(
                            'transaction_at', cast(? as text),
                            'type', cast(? as text),
                            'card_brand', cast(? as text),
                            'sales_channel', 'POS',
                            'masked_pan', '411111******1111'))
                        """, transaction.terminalId(), transaction.acquirerId(), transaction.amount(),
                transaction.currency(), transaction.status().name(), transaction.at().toString(),
                transaction.type().name(), transaction.brand().name());
    }

    private long merchant(long organisationId) {
        return jdbc.queryForObject("""
                        insert into merchant (id_organisation, name, registration_number, status)
                        values (?, 'Test merchant', ?, 'ACTIVE')
                        returning id_merchant
                        """, Long.class, organisationId, uniqueCode(11));
    }

    private long outlet(long merchantId) {
        return jdbc.queryForObject("""
                        insert into outlet (id_merchant, name, status)
                        values (?, 'Test outlet', 'ACTIVE')
                        returning id_outlet
                        """, Long.class, merchantId);
    }

    private static String uniqueCode(int length) {
        return UUID.randomUUID().toString().replace("-", "").substring(0, length);
    }

    public record TestTransaction(long terminalId, long acquirerId, Instant at, BigDecimal amount, String currency,
                                  TransactionStatus status, TransactionType type, CardBrand brand) {

        public static TestTransaction approvedEurSale(long terminalId, long acquirerId, Instant at, String amount) {
            return new TestTransaction(terminalId, acquirerId, at, new BigDecimal(amount), "EUR",
                    TransactionStatus.APPROVED, TransactionType.SALE, CardBrand.VISA);
        }

        public TestTransaction withAcquirer(long newAcquirerId) {
            return new TestTransaction(terminalId, newAcquirerId, at, amount, currency, status, type, brand);
        }

        public TestTransaction withCurrency(String newCurrency) {
            return new TestTransaction(terminalId, acquirerId, at, amount, newCurrency, status, type, brand);
        }

        public TestTransaction withStatus(TransactionStatus newStatus) {
            return new TestTransaction(terminalId, acquirerId, at, amount, currency, newStatus, type, brand);
        }

        public TestTransaction withType(TransactionType newType) {
            return new TestTransaction(terminalId, acquirerId, at, amount, currency, status, newType, brand);
        }

        public TestTransaction withBrand(CardBrand newBrand) {
            return new TestTransaction(terminalId, acquirerId, at, amount, currency, status, type, newBrand);
        }
    }
}

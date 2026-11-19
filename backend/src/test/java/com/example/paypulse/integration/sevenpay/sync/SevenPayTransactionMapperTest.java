package com.example.paypulse.integration.sevenpay.sync;

import com.example.paypulse.integration.sevenpay.dto.SevenPayTransactionDetails;
import com.example.paypulse.integration.sevenpay.sync.SevenPayTransactionMapping.Mapped;
import com.example.paypulse.integration.sevenpay.sync.SevenPayTransactionMapping.Skipped;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class SevenPayTransactionMapperTest {

    private final SevenPayTransactionMapper mapper = new SevenPayTransactionMapper();

    @Test
    void mapsSaleToOurFormat() {
        Mapped mapped = mapped(details());

        assertThat(mapped.sourceId()).isEqualTo("tx-1");
        assertThat(mapped.tid()).isEqualTo("SED00420");
        assertThat(mapped.acquirerCode()).isEqualTo("03");
        assertThat(mapped.amount()).isEqualByComparingTo("7.00");
        assertThat(mapped.currency()).isEqualTo("EUR");
        assertThat(mapped.status()).isEqualTo("APPROVED");
        assertThat(mapped.rawData())
                .containsEntry("transaction_at", "2026-10-22T10:35:07.557+02:00")
                .containsEntry("type", "SALE")
                .containsEntry("card_brand", "VISA")
                .containsEntry("masked_pan", "478560******9947")
                .containsEntry("rrn", "632016120310")
                .containsEntry("auth_code", "071035")
                .doesNotContainKey("sales_channel");
    }

    @Test
    void winterTimeUsesZagrebOffset() {
        Mapped mapped = mapped(details().timestamp(LocalDateTime.of(2026, 11, 16, 16, 5, 25)));
        assertThat(mapped.rawData()).containsEntry("transaction_at", "2026-11-16T16:05:25+01:00");
    }

    @Test
    void usesAcquirerRrnWhenRrnIsMissing() {
        Mapped mapped = mapped(details().rrn(null).acquirerRrn("000000004098"));
        assertThat(mapped.rawData()).containsEntry("rrn", "000000004098");
    }

    @Test
    void leavesOutMaskedPanThatIsNotInOurFormat() {
        Mapped mapped = mapped(details().panMasked("4785XXXX9947"));
        assertThat(mapped.rawData()).doesNotContainKey("masked_pan");
    }

    @Test
    void saleWithInstallmentNumberIsSaleWithInstallments() {
        Mapped mapped = mapped(details().installmentNumber(6));
        assertThat(mapped.rawData()).containsEntry("type", "SALE_WITH_INSTALLMENTS");
        assertThat(mapped.status()).isEqualTo("APPROVED");
    }

    @ParameterizedTest
    @CsvSource({
            "sale, valid, 00, APPROVED",
            "sale, voided, 00, CANCELLED",
            "sale, cancelled, 00, CANCELLED",
            "sale, valid, 51, DECLINED",
            "sale, valid, 'O5 ', DECLINED",
            "refund, valid, 00, REFUNDED",
            "refund, valid, 05, DECLINED",
            "storno, valid, 00, CANCELLED",
            "technical_reversal, valid, 00, CANCELLED"
    })
    void mapsStatusWithAgreedRules(String type, String validity, String responseCode, String expected) {
        Mapped mapped = mapped(details().transactionType(type).validity(validity).responseCode(responseCode));
        assertThat(mapped.status()).isEqualTo(expected);
        assertThat(mapped.rawData()).containsEntry("type", type.toUpperCase());
    }

    @Test
    void noResponseIsDeclined() {
        Mapped mapped = mapped(details().responseCode(null));
        assertThat(mapped.status()).isEqualTo("DECLINED");
    }

    @ParameterizedTest
    @ValueSource(strings = {"not_known", "closed_loop_prepaid", "closed_loop_postpaid"})
    void skipsDummyCardBrands(String brand) {
        assertThat(skipReason(details().cardBrand(brand))).isEqualTo(SyncSkipReason.DUMMY_CARD_BRAND);
    }

    @Test
    void importsJcb() {
        assertThat(mapped(details().cardBrand("jcb")).rawData()).containsEntry("card_brand", "JCB");
    }

    @Test
    void skipsUnknownCardBrand() {
        assertThat(skipReason(details().cardBrand("unionpay"))).isEqualTo(SyncSkipReason.UNKNOWN_CARD_BRAND);
        assertThat(skipReason(details().cardBrand(null))).isEqualTo(SyncSkipReason.UNKNOWN_CARD_BRAND);
    }

    @ParameterizedTest
    @ValueSource(strings = {"preauthorization", "preauthorization_void", "completion"})
    void skipsPreauthorizations(String type) {
        assertThat(skipReason(details().transactionType(type))).isEqualTo(SyncSkipReason.UNSUPPORTED_TYPE);
    }

    @Test
    void skipsOtherCurrencies() {
        assertThat(skipReason(details().currency("840"))).isEqualTo(SyncSkipReason.UNSUPPORTED_CURRENCY);
    }

    @Test
    void skipsWhenRequiredDataIsMissing() {
        assertThat(skipReason(details().tid(null))).isEqualTo(SyncSkipReason.MISSING_DATA);
        assertThat(skipReason(details().amount(null))).isEqualTo(SyncSkipReason.MISSING_DATA);
        assertThat(skipReason(details().timestamp(null))).isEqualTo(SyncSkipReason.MISSING_DATA);
    }

    private Mapped mapped(DetailsBuilder builder) {
        return (Mapped) mapper.map(builder.build());
    }

    private SyncSkipReason skipReason(DetailsBuilder builder) {
        return ((Skipped) mapper.map(builder.build())).reason();
    }

    private static DetailsBuilder details() {
        return new DetailsBuilder();
    }

    private static final class DetailsBuilder {
        private String transactionType = "sale";
        private BigDecimal amount = new BigDecimal("7.0");
        private String currency = "978";
        private String validity = "valid";
        private String responseCode = "00";
        private LocalDateTime timestamp = LocalDateTime.of(2026, 10, 22, 10, 35, 7, 557_000_000);
        private String tid = "SED00420";
        private String panMasked = "478560******9947";
        private String cardBrand = "visa";
        private String rrn = "632016120310";
        private String acquirerRrn = "000000004098";
        private Integer installmentNumber;

        DetailsBuilder transactionType(String value) { transactionType = value; return this; }
        DetailsBuilder amount(BigDecimal value) { amount = value; return this; }
        DetailsBuilder currency(String value) { currency = value; return this; }
        DetailsBuilder validity(String value) { validity = value; return this; }
        DetailsBuilder responseCode(String value) { responseCode = value; return this; }
        DetailsBuilder timestamp(LocalDateTime value) { timestamp = value; return this; }
        DetailsBuilder tid(String value) { tid = value; return this; }
        DetailsBuilder panMasked(String value) { panMasked = value; return this; }
        DetailsBuilder cardBrand(String value) { cardBrand = value; return this; }
        DetailsBuilder rrn(String value) { rrn = value; return this; }
        DetailsBuilder acquirerRrn(String value) { acquirerRrn = value; return this; }
        DetailsBuilder installmentNumber(Integer value) { installmentNumber = value; return this; }

        SevenPayTransactionDetails build() {
            return new SevenPayTransactionDetails("tx-1", transactionType, amount, currency, validity,
                    "authorized", responseCode, "071035", "03", tid, "SED000000003", timestamp, tid,
                    "SED000000003", panMasked, cardBrand, rrn, acquirerRrn, "NO_RATES", installmentNumber,
                    null);
        }
    }
}

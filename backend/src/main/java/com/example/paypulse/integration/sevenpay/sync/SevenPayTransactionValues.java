package com.example.paypulse.integration.sevenpay.sync;

import com.example.paypulse.integration.sevenpay.dto.SevenPayTransactionDetails;
import com.example.paypulse.model.CardBrand;
import com.example.paypulse.model.TransactionStatus;
import com.example.paypulse.model.TransactionType;

import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

final class SevenPayTransactionValues {

    static final String APPROVED_RESPONSE_CODE = "00";
    static final String EUR = "EUR";

    private static final Set<String> EURO_CODES = Set.of("978", EUR);
    private static final Set<String> CANCELLED_VALIDITY = Set.of("voided", "cancelled");
    private static final Set<CardBrand> DUMMY_BRANDS =
            Set.of(CardBrand.NOT_KNOWN, CardBrand.CLOSED_LOOP_PREPAID, CardBrand.CLOSED_LOOP_POSTPAID);
    private static final Map<String, TransactionType> TYPES = Map.of(
            "sale", TransactionType.SALE,
            "sale_with_installments", TransactionType.SALE_WITH_INSTALLMENTS,
            "refund", TransactionType.REFUND,
            "storno", TransactionType.STORNO,
            "technical_reversal", TransactionType.TECHNICAL_REVERSAL);

    private SevenPayTransactionValues() {
    }

    static Optional<TransactionType> type(SevenPayTransactionDetails details) {
        TransactionType type = TYPES.get(normalise(details.transactionType()));
        if (type == TransactionType.SALE && details.installmentNumber() != null) {
            return Optional.of(TransactionType.SALE_WITH_INSTALLMENTS);
        }
        return Optional.ofNullable(type);
    }

    static TransactionStatus status(SevenPayTransactionDetails details, TransactionType type) {
        if (CANCELLED_VALIDITY.contains(normalise(details.validity()))) {
            return TransactionStatus.CANCELLED;
        }
        if (!APPROVED_RESPONSE_CODE.equals(trimmed(details.responseCode()))) {
            return TransactionStatus.DECLINED;
        }
        return switch (type) {
            case REFUND -> TransactionStatus.REFUNDED;
            case STORNO, TECHNICAL_REVERSAL -> TransactionStatus.CANCELLED;
            case SALE, SALE_WITH_INSTALLMENTS -> TransactionStatus.APPROVED;
        };
    }

    static Optional<CardBrand> cardBrand(String value) {
        String name = normalise(value).toUpperCase(Locale.ROOT);
        return Arrays.stream(CardBrand.values()).filter(brand -> brand.name().equals(name)).findFirst();
    }

    static boolean isDummy(CardBrand brand) {
        return DUMMY_BRANDS.contains(brand);
    }

    static boolean isEuro(String currency) {
        return EURO_CODES.contains(trimmed(currency));
    }

    static String trimmed(String value) {
        return value == null ? "" : value.strip();
    }

    private static String normalise(String value) {
        return trimmed(value).toLowerCase(Locale.ROOT);
    }
}

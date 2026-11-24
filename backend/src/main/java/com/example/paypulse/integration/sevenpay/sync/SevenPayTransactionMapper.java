package com.example.paypulse.integration.sevenpay.sync;

import com.example.paypulse.integration.sevenpay.dto.SevenPayTransaction;
import com.example.paypulse.integration.sevenpay.dto.SevenPayTransactionDetails;
import com.example.paypulse.integration.sevenpay.sync.SevenPayTransactionMapping.Mapped;
import com.example.paypulse.integration.sevenpay.sync.SevenPayTransactionMapping.Skipped;
import com.example.paypulse.model.CardBrand;
import com.example.paypulse.model.TransactionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.RoundingMode;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

@Component
public class SevenPayTransactionMapper {

    public Optional<String> statusOf(SevenPayTransaction transaction) {
        return SevenPayTransactionValues.type(transaction.transactionType(), null)
                .map(type -> SevenPayTransactionValues.status(transaction.validity(), transaction.responseCode(), type)
                        .name());
    }

    private static final Logger log = LoggerFactory.getLogger(SevenPayTransactionMapper.class);
    private static final ZoneId ZAGREB = ZoneId.of("Europe/Zagreb");
    private static final Pattern MASKED_PAN = Pattern.compile("^[0-9]{6}\\*{2,9}[0-9]{4}$");

    public SevenPayTransactionMapping map(SevenPayTransactionDetails details) {
        String sourceId = SevenPayTransactionValues.trimmed(details.transactionId());
        if (sourceId.isEmpty() || details.timestamp() == null || details.amount() == null
                || SevenPayTransactionValues.trimmed(details.tid()).isEmpty()) {
            return new Skipped(sourceId, SyncSkipReason.MISSING_DATA);
        }
        Optional<TransactionType> type =
                SevenPayTransactionValues.type(details.transactionType(), details.installmentNumber());
        if (type.isEmpty()) {
            return new Skipped(sourceId, SyncSkipReason.UNSUPPORTED_TYPE);
        }
        if (!SevenPayTransactionValues.isEuro(details.currency())) {
            return new Skipped(sourceId, SyncSkipReason.UNSUPPORTED_CURRENCY);
        }
        Optional<CardBrand> brand = SevenPayTransactionValues.cardBrand(details.cardBrand());
        if (brand.isEmpty()) {
            log.warn("7Pay transaction {} has an unknown card brand '{}'", sourceId, details.cardBrand());
            return new Skipped(sourceId, SyncSkipReason.UNKNOWN_CARD_BRAND);
        }
        if (SevenPayTransactionValues.isDummy(brand.get())) {
            return new Skipped(sourceId, SyncSkipReason.DUMMY_CARD_BRAND);
        }
        return mapped(sourceId, details, type.get(), brand.get());
    }

    private Mapped mapped(String sourceId, SevenPayTransactionDetails details, TransactionType type,
                          CardBrand brand) {
        return new Mapped(
                sourceId,
                details.tid().strip(),
                SevenPayTransactionValues.trimmed(details.acquirerHost()),
                details.amount().setScale(2, RoundingMode.HALF_UP),
                SevenPayTransactionValues.EUR,
                SevenPayTransactionValues.status(details.validity(), details.responseCode(), type).name(),
                rawData(details, type, brand));
    }

    private Map<String, Object> rawData(SevenPayTransactionDetails details, TransactionType type, CardBrand brand) {
        Map<String, Object> rawData = new LinkedHashMap<>();
        rawData.put("transaction_at",
                details.timestamp().atZone(ZAGREB).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
        rawData.put("type", type.name());
        rawData.put("card_brand", brand.name());
        putIfPresent(rawData, "masked_pan", maskedPan(details.panMasked()));
        putIfPresent(rawData, "rrn", firstPresent(details.rrn(), details.acquirerRrn()));
        putIfPresent(rawData, "auth_code", SevenPayTransactionValues.trimmed(details.authNumber()));
        return rawData;
    }

    private static String maskedPan(String panMasked) {
        String pan = SevenPayTransactionValues.trimmed(panMasked);
        return MASKED_PAN.matcher(pan).matches() ? pan : "";
    }

    private static String firstPresent(String first, String second) {
        String value = SevenPayTransactionValues.trimmed(first);
        return value.isEmpty() ? SevenPayTransactionValues.trimmed(second) : value;
    }

    private static void putIfPresent(Map<String, Object> rawData, String key, String value) {
        if (!value.isEmpty()) {
            rawData.put(key, value);
        }
    }
}

package com.example.paypulse.seed.generator;

import com.example.paypulse.model.Acquirer;
import com.example.paypulse.model.Terminal;
import com.example.paypulse.seed.util.DataGenerator;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

@Component
@Profile("seed")
public class TransactionFactory {

    public static final String SOURCE_ID_PREFIX = "SEED-TXN-";

    private static final String[] CARD_BRANDS = {"VISA", "MASTERCARD", "AMEX", "DINERS", "MAESTRO"};
    private static final String[] STATUSES = {"APPROVED", "DECLINED", "REFUNDED", "CANCELLED"};
    private static final String[] TYPES = {"SALE", "REFUND", "VOID"};
    private static final String[] SALES_CHANNELS = {"POS", "ECOM", "MOTO", "CONTACTLESS"};

    private final DataGenerator dataGenerator = new DataGenerator(new Random(98765));

    public SeedTransaction create(Random random, long sequence, List<Terminal> terminals, List<Acquirer> acquirers,
                                  LocalDateTime startDate, LocalDateTime endDate) {
        Terminal terminal = terminals.get(random.nextInt(terminals.size()));
        Acquirer acquirer = acquirers.get(random.nextInt(acquirers.size()));

        long secondsBetween = Duration.between(startDate, endDate).getSeconds();
        long randomSeconds = (long) (random.nextDouble() * secondsBetween);
        LocalDateTime transactionTime = startDate.plusSeconds(randomSeconds);

        String brand = CARD_BRANDS[random.nextInt(CARD_BRANDS.length)];
        String type = TYPES[random.nextInt(TYPES.length)];
        String channel = SALES_CHANNELS[random.nextInt(SALES_CHANNELS.length)];

        Map<String, Object> rawData = new HashMap<>();
        rawData.put("transaction_at", transactionTime.atOffset(ZoneOffset.UTC).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
        rawData.put("type", type);
        rawData.put("card_brand", brand);
        rawData.put("sales_channel", channel);
        rawData.put("auth_code", dataGenerator.randomAuthCode());
        rawData.put("rrn", dataGenerator.randomRrn());
        rawData.put("masked_pan", dataGenerator.randomMaskedPan(brand));

        return new SeedTransaction(
                terminal.getIdTerminal(),
                acquirer.getIdAcquirer(),
                SOURCE_ID_PREFIX + sequence,
                BigDecimal.valueOf(random.nextDouble() * 1000 + 1).setScale(2, RoundingMode.HALF_UP),
                "EUR",
                STATUSES[random.nextInt(STATUSES.length)],
                rawData
        );
    }
}

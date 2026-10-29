package com.example.paypulse.seed.generator;

import com.example.paypulse.model.Acquirer;
import com.example.paypulse.model.Terminal;
import com.example.paypulse.model.Transaction;
import com.example.paypulse.repository.TransactionRepository;
import com.example.paypulse.seed.config.SeedProperties;
import com.example.paypulse.seed.util.DataGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

@Service
public class TransactionGenerator {
    private static final Logger log = LoggerFactory.getLogger(TransactionGenerator.class);
    private final TransactionRepository transactionRepository;
    private final SeedProperties seedProperties;
    private final DataGenerator dataGenerator;

    public TransactionGenerator(TransactionRepository transactionRepository, SeedProperties seedProperties) {
        this.transactionRepository = transactionRepository;
        this.seedProperties = seedProperties;
        this.dataGenerator = new DataGenerator(new Random(98765));
    }

    @Transactional
    public void generateTransactions(List<Terminal> terminals, List<Acquirer> acquirers) {
        if (terminals.isEmpty() || acquirers.isEmpty()) {
            log.warn("No terminals or acquirers available for transaction generation");
            return;
        }
        String[] cardBrands = {"VISA", "MASTERCARD", "AMEX", "DINERS", "MAESTRO"};
        String[] statuses = {"APPROVED", "DECLINED", "REFUNDED", "CANCELLED"};
        String[] types = {"SALE", "REFUND", "VOID"};
        String[] salesChannels = {"POS", "ECOM", "MOTO", "CONTACTLESS"};

        Random rand = new Random(98765);
        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate = endDate.minusMonths(13);

        int batchSize = seedProperties.getBatchSize();
        int total = seedProperties.getTransactionCount();
        List<Transaction> batch = new ArrayList<>(batchSize);

        for (int i = 0; i < total; i++) {
            Terminal terminal = terminals.get(rand.nextInt(terminals.size()));
            Acquirer acquirer = acquirers.get(rand.nextInt(acquirers.size()));

            long secondsBetween = Duration.between(startDate, endDate).getSeconds();
            long randomSeconds = (long) (rand.nextDouble() * secondsBetween);
            LocalDateTime transactionTime = startDate.plusSeconds(randomSeconds);

            String sourceId = "TXN-" + UUID.randomUUID().toString();

            Transaction txn = new Transaction();
            txn.setTerminal(terminal);
            txn.setAcquirer(acquirer);
            txn.setSourceId(sourceId);
            txn.setAmount(BigDecimal.valueOf(rand.nextDouble() * 1000 + 1).setScale(2, RoundingMode.HALF_UP));
            txn.setCurrency("EUR");
            txn.setStatus(statuses[rand.nextInt(statuses.length)]);

            String brand = cardBrands[rand.nextInt(cardBrands.length)];
            String type = types[rand.nextInt(types.length)];
            String channel = salesChannels[rand.nextInt(salesChannels.length)];

            Map<String, Object> rawData = new HashMap<>();
            rawData.put("transaction_at", transactionTime.atOffset(ZoneOffset.UTC).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
            rawData.put("type", type);
            rawData.put("card_brand", brand);
            rawData.put("sales_channel", channel);
            rawData.put("auth_code", dataGenerator.randomAuthCode());
            rawData.put("rrn", dataGenerator.randomRrn());
            rawData.put("masked_pan", dataGenerator.randomMaskedPan(brand));
            txn.setRawData(rawData);

            batch.add(txn);
            if (batch.size() >= batchSize) {
                transactionRepository.saveAll(batch);
                batch.clear();
            }
        }
        if (!batch.isEmpty()) {
            transactionRepository.saveAll(batch);
        }
        log.info("Generated {} transactions", total);
    }
}

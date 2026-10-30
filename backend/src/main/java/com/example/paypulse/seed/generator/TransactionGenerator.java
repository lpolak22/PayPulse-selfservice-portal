package com.example.paypulse.seed.generator;

import com.example.paypulse.model.Acquirer;
import com.example.paypulse.model.Terminal;
import com.example.paypulse.model.Transaction;
import com.example.paypulse.repository.TransactionRepository;
import com.example.paypulse.seed.config.SeedProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

@Service
@Profile("seed")
public class TransactionGenerator {
    private static final Logger log = LoggerFactory.getLogger(TransactionGenerator.class);
    private final TransactionRepository transactionRepository;
    private final SeedProperties seedProperties;
    private final TransactionFactory transactionFactory;
    private final TransactionTemplate batchTransaction;

    public TransactionGenerator(TransactionRepository transactionRepository, SeedProperties seedProperties,
                                TransactionFactory transactionFactory,
                                PlatformTransactionManager transactionManager) {
        this.transactionRepository = transactionRepository;
        this.seedProperties = seedProperties;
        this.transactionFactory = transactionFactory;
        this.batchTransaction = new TransactionTemplate(transactionManager);
        this.batchTransaction.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    public void generateTransactions(List<Terminal> terminals, List<Acquirer> acquirers) {
        if (terminals.isEmpty() || acquirers.isEmpty()) {
            log.warn("No terminals or acquirers available for transaction generation");
            return;
        }

        int total = seedProperties.getTransactionCount();
        Set<String> existing = new HashSet<>(
                transactionRepository.findSourceIdsByPrefix(TransactionFactory.SOURCE_ID_PREFIX));
        if (existing.isEmpty() && transactionRepository.count() > 0) {
            log.info("Skipping transaction generation, transactions already exist");
            return;
        }
        if (existing.size() >= total) {
            log.info("Transaction seed is complete, {} transactions exist", existing.size());
            return;
        }
        if (!existing.isEmpty()) {
            log.info("Resuming transaction seed, {} of {} transactions exist", existing.size(), total);
        }

        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate = endDate.minusMonths(13);
        Random random = new Random(98765);
        int batchSize = seedProperties.getBatchSize();
        List<Transaction> batch = new ArrayList<>(batchSize);
        int created = 0;

        for (long sequence = 0; sequence < total; sequence++) {
            Transaction transaction = transactionFactory.create(
                    random, sequence, terminals, acquirers, startDate, endDate);
            if (existing.contains(transaction.getSourceId())) {
                continue;
            }
            batch.add(transaction);
            created++;
            if (batch.size() >= batchSize) {
                saveBatch(batch);
                batch.clear();
            }
        }
        if (!batch.isEmpty()) {
            saveBatch(batch);
        }
        log.info("Transaction seed finished, {} of {} transactions exist", existing.size() + created, total);
    }

    private void saveBatch(List<Transaction> batch) {
        List<Transaction> toSave = List.copyOf(batch);
        batchTransaction.executeWithoutResult(status -> transactionRepository.saveAll(toSave));
    }
}

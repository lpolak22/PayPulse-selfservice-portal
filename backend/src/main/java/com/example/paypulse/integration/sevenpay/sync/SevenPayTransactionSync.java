package com.example.paypulse.integration.sevenpay.sync;

import com.example.paypulse.integration.sevenpay.SevenPayClient;
import com.example.paypulse.integration.sevenpay.SevenPayException;
import com.example.paypulse.integration.sevenpay.SevenPayPages;
import com.example.paypulse.integration.sevenpay.dto.SevenPayTransaction;
import com.example.paypulse.integration.sevenpay.dto.SevenPayTransactionQuery;
import com.example.paypulse.integration.sevenpay.sync.SevenPayTransactionMapping.Mapped;
import com.example.paypulse.integration.sevenpay.sync.SevenPayTransactionMapping.Skipped;
import com.example.paypulse.model.Organisation;
import com.example.paypulse.model.Terminal;
import com.example.paypulse.model.Transaction;
import com.example.paypulse.repository.MerchantRepository;
import com.example.paypulse.repository.TerminalRepository;
import com.example.paypulse.repository.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class SevenPayTransactionSync {

    private static final Logger log = LoggerFactory.getLogger(SevenPayTransactionSync.class);
    private static final LocalTime END_OF_DAY = LocalTime.of(23, 59, 59);

    private final SevenPayClient sevenPayClient;
    private final MerchantRepository merchantRepository;
    private final TerminalRepository terminalRepository;
    private final TransactionRepository transactionRepository;
    private final SevenPayTransactionMapper mapper;
    private final SevenPayTransactionWriter writer;
    private final Map<String, SyncSkipReason> skippedIds = new ConcurrentHashMap<>();
    private volatile Set<String> lastTerminalIds = Set.of();

    public SevenPayTransactionSync(SevenPayClient sevenPayClient, MerchantRepository merchantRepository,
                                   TerminalRepository terminalRepository,
                                   TransactionRepository transactionRepository,
                                   SevenPayTransactionMapper mapper, SevenPayTransactionWriter writer) {
        this.sevenPayClient = sevenPayClient;
        this.merchantRepository = merchantRepository;
        this.terminalRepository = terminalRepository;
        this.transactionRepository = transactionRepository;
        this.mapper = mapper;
        this.writer = writer;
    }

    public void sync(Organisation organisation, LocalDate from, LocalDate until, SyncCounts counts) {
        Map<String, Terminal> terminalsByTid = terminalsByTid(organisation);
        for (String oib : merchantRepository.findRegistrationNumbersInOrganisation(organisation.getIdOrganisation())) {
            for (LocalDate day = from; !day.isAfter(until); day = day.plusDays(1)) {
                syncDay(oib, day, terminalsByTid, counts);
            }
        }
        log.info("7Pay transaction sync {} - {}: {}", from, until, counts);
    }

    private Map<String, Terminal> terminalsByTid(Organisation organisation) {
        Map<String, Terminal> terminals = terminalRepository.findAllInOrganisation(organisation.getIdOrganisation())
                .stream()
                .filter(terminal -> terminal.getSourceId() != null)
                .collect(Collectors.toMap(Terminal::getSourceId, Function.identity(), (first, second) -> first));
        if (!terminals.keySet().equals(lastTerminalIds)) {
            skippedIds.clear();
            lastTerminalIds = Set.copyOf(terminals.keySet());
        }
        return terminals;
    }

    private void syncDay(String oib, LocalDate day, Map<String, Terminal> terminalsByTid, SyncCounts counts) {
        SevenPayTransactionQuery query =
                new SevenPayTransactionQuery(oib, day.atStartOfDay(), day.atTime(END_OF_DAY));
        try {
            for (SevenPayTransaction transaction
                    : SevenPayPages.fetchAll(page -> sevenPayClient.searchTransactions(query, page))) {
                syncOne(transaction, terminalsByTid, counts);
            }
        } catch (SevenPayException exception) {
            failOrRethrow("search " + oib + " " + day, exception, counts);
        }
    }

    private void syncOne(SevenPayTransaction transaction, Map<String, Terminal> terminalsByTid, SyncCounts counts) {
        String sourceId = transaction.transactionId();
        if (sourceId == null || sourceId.isBlank()) {
            counts.skip(SyncSkipReason.MISSING_DATA);
            return;
        }
        SyncSkipReason knownSkip = skippedIds.get(sourceId);
        if (knownSkip != null) {
            counts.skip(knownSkip);
            return;
        }
        try {
            Optional<Transaction> existing = transactionRepository.findBySourceId(sourceId);
            if (existing.isPresent()) {
                refresh(existing.get(), transaction, counts);
            } else {
                store(mapper.map(sevenPayClient.getTransactionDetails(sourceId)), terminalsByTid, counts);
            }
        } catch (RuntimeException exception) {
            failOrRethrow(sourceId, exception, counts);
        }
    }

    private void refresh(Transaction existing, SevenPayTransaction transaction, SyncCounts counts) {
        Optional<String> status = mapper.statusOf(transaction);
        counts.add(status.map(value -> writer.refreshStatus(existing, value)).orElse(WriteOutcome.UNCHANGED));
    }

    private void store(SevenPayTransactionMapping mapping, Map<String, Terminal> terminalsByTid, SyncCounts counts) {
        switch (mapping) {
            case Skipped skipped -> skip(skipped.sourceId(), skipped.reason(), counts);
            case Mapped mapped -> {
                Terminal terminal = terminalsByTid.get(mapped.tid());
                if (terminal == null) {
                    skip(mapped.sourceId(), SyncSkipReason.UNKNOWN_TERMINAL, counts);
                } else {
                    counts.add(writer.write(mapped, terminal));
                }
            }
        }
    }

    private void skip(String sourceId, SyncSkipReason reason, SyncCounts counts) {
        skippedIds.put(sourceId, reason);
        counts.skip(reason);
    }

    private static void failOrRethrow(String what, RuntimeException exception, SyncCounts counts) {
        if (exception instanceof SevenPayException sevenPay && (sevenPay.isAccessDenied() || sevenPay.isRetryable())) {
            throw sevenPay;
        }
        log.warn("7Pay {} could not be synced: {}", what, exception.getMessage());
        counts.fail();
    }
}

package com.example.paypulse.integration.sevenpay.sync;

import com.example.paypulse.integration.sevenpay.sync.SevenPayTransactionMapping.Mapped;
import com.example.paypulse.model.Acquirer;
import com.example.paypulse.model.Terminal;
import com.example.paypulse.model.Transaction;
import com.example.paypulse.repository.TransactionRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.Optional;

@Component
public class SevenPayTransactionWriter {

    private final TransactionRepository transactionRepository;
    private final SevenPayAcquirers acquirers;

    public SevenPayTransactionWriter(TransactionRepository transactionRepository, SevenPayAcquirers acquirers) {
        this.transactionRepository = transactionRepository;
        this.acquirers = acquirers;
    }

    @Transactional
    public WriteOutcome write(Mapped mapped, Terminal terminal) {
        Acquirer acquirer = acquirers.resolve(mapped.acquirerCode());
        Optional<Transaction> existing = transactionRepository.findBySourceId(mapped.sourceId());
        if (existing.isPresent() && isUnchanged(existing.get(), mapped, terminal, acquirer)) {
            return WriteOutcome.UNCHANGED;
        }
        Transaction transaction = existing.orElseGet(Transaction::new);
        transaction.setSourceId(mapped.sourceId());
        transaction.setTerminal(terminal);
        transaction.setAcquirer(acquirer);
        transaction.setAmount(mapped.amount());
        transaction.setCurrency(mapped.currency());
        transaction.setStatus(mapped.status());
        transaction.setRawData(mapped.rawData());
        transactionRepository.save(transaction);
        return existing.isPresent() ? WriteOutcome.UPDATED : WriteOutcome.CREATED;
    }

    private static boolean isUnchanged(Transaction transaction, Mapped mapped, Terminal terminal, Acquirer acquirer) {
        return Objects.equals(transaction.getTerminal().getIdTerminal(), terminal.getIdTerminal())
                && Objects.equals(transaction.getAcquirer().getIdAcquirer(), acquirer.getIdAcquirer())
                && transaction.getAmount().compareTo(mapped.amount()) == 0
                && transaction.getCurrency().equals(mapped.currency())
                && transaction.getStatus().equals(mapped.status())
                && Objects.equals(transaction.getRawData(), mapped.rawData());
    }
}

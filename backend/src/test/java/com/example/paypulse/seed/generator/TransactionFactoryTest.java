package com.example.paypulse.seed.generator;

import com.example.paypulse.model.Acquirer;
import com.example.paypulse.model.Terminal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;

class TransactionFactoryTest {

    private final TransactionFactory factory = new TransactionFactory();

    private SeedTransaction createdAt(LocalDateTime time) {
        Terminal terminal = new Terminal();
        terminal.setIdTerminal(11L);
        Acquirer acquirer = new Acquirer();
        acquirer.setIdAcquirer(22L);
        return factory.create(new Random(1), 5, List.of(terminal), List.of(acquirer), time, time);
    }

    @ParameterizedTest
    @CsvSource({
            "2026-01-15T10:00, 2026-01-15T10:00:00+01:00",
            "2026-07-15T10:00, 2026-07-15T10:00:00+02:00"
    })
    void transactionTimeIsWrittenWithTheZagrebOffset(LocalDateTime time, String expected) {
        assertThat(createdAt(time).rawData()).containsEntry("transaction_at", expected);
    }

    @Test
    void transactionUsesTheGivenTerminalAcquirerAndSequence() {
        SeedTransaction transaction = createdAt(LocalDateTime.of(2026, 3, 1, 12, 0));

        assertThat(transaction.terminalId()).isEqualTo(11L);
        assertThat(transaction.acquirerId()).isEqualTo(22L);
        assertThat(transaction.sourceId()).isEqualTo(TransactionFactory.SOURCE_ID_PREFIX + 5);
        assertThat(transaction.currency()).isEqualTo("EUR");
    }

    @Test
    void transactionTimeStaysInsideTheRange() {
        LocalDateTime start = LocalDateTime.of(2026, 3, 1, 0, 0);
        LocalDateTime end = start.plusDays(7);
        Terminal terminal = new Terminal();
        terminal.setIdTerminal(1L);
        Acquirer acquirer = new Acquirer();
        acquirer.setIdAcquirer(1L);
        Random random = new Random(42);

        for (int i = 0; i < 50; i++) {
            SeedTransaction transaction = factory.create(random, i, List.of(terminal), List.of(acquirer), start, end);
            String at = (String) transaction.rawData().get("transaction_at");
            LocalDateTime time = LocalDateTime.parse(at.substring(0, 19));
            assertThat(time).isBetween(start, end);
        }
    }
}

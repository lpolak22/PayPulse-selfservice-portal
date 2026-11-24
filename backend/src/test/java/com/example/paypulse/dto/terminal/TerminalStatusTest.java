package com.example.paypulse.dto.terminal;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class TerminalStatusTest {

    @ParameterizedTest
    @CsvSource({"ACTIVE, ACTIVE", "prepared, PREPARED", "' Blocked ', BLOCKED", "unknown, UNKNOWN"})
    void knownStatusesAreFoundIgnoringCaseAndSpaces(String param, TerminalStatus expected) {
        assertThat(TerminalStatus.fromParam(param)).contains(expected);
    }

    @ParameterizedTest
    @ValueSource(strings = {"ACTVE", "SOMETHING_NEW", "HOLD"})
    void unknownStatusesAreNotFound(String param) {
        assertThat(TerminalStatus.fromParam(param)).isEmpty();
    }
}

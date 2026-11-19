package com.example.paypulse.dto.terminal;

import com.example.paypulse.exception.ApiException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.data.domain.Sort;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TerminalSortTest {

    @ParameterizedTest
    @CsvSource({"tid, terminalId", "TID, terminalId", "type, type", "Status, status",
            "outlet, outlet.name", "merchant, outlet.merchant.name"})
    void sortParameterIsMappedToTheEntityProperty(String param, String property) {
        assertThat(TerminalSort.of(param, "asc").field().property()).isEqualTo(property);
    }

    @ParameterizedTest
    @CsvSource({"asc, ASC", "DESC, DESC"})
    void directionIgnoresLetterCase(String param, Sort.Direction expected) {
        assertThat(TerminalSort.of("tid", param).direction()).isEqualTo(expected);
    }

    @Test
    void sortAlwaysEndsWithTidAndIdSoPagesAreStable() {
        Sort sort = TerminalSort.of("merchant", "desc").toSort();

        assertThat(sort).containsExactly(Sort.Order.desc("outlet.merchant.name"),
                Sort.Order.asc("terminalId"), Sort.Order.asc("idTerminal"));
    }

    @Test
    void sortByTidAddsOnlyTheIdTieBreak() {
        Sort sort = TerminalSort.of("tid", "desc").toSort();

        assertThat(sort).containsExactly(Sort.Order.desc("terminalId"), Sort.Order.asc("idTerminal"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"mcc", "", "id"})
    void unknownSortIsRejected(String param) {
        assertThatThrownBy(() -> TerminalSort.of(param, "asc"))
                .isInstanceOf(ApiException.class)
                .hasMessage("sort must be tid, type, status, outlet or merchant");
    }

    @Test
    void unknownDirectionIsRejected() {
        assertThatThrownBy(() -> TerminalSort.of("tid", "up"))
                .isInstanceOf(ApiException.class)
                .hasMessage("direction must be asc or desc");
    }
}

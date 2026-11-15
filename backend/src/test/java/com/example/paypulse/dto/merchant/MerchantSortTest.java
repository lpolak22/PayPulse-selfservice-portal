package com.example.paypulse.dto.merchant;

import com.example.paypulse.exception.ApiException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.data.domain.Sort;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MerchantSortTest {

    @ParameterizedTest
    @CsvSource({"name, name", "NAME, name", "oib, registrationNumber", "Status, status"})
    void sortParameterIsMappedToTheEntityProperty(String param, String property) {
        assertThat(MerchantSort.of(param, "asc").field().property()).isEqualTo(property);
    }

    @ParameterizedTest
    @CsvSource({"asc, ASC", "DESC, DESC"})
    void directionIgnoresLetterCase(String param, Sort.Direction expected) {
        assertThat(MerchantSort.of("name", param).direction()).isEqualTo(expected);
    }

    @Test
    void sortAlwaysEndsWithNameAndIdSoPagesAreStable() {
        Sort sort = MerchantSort.of("oib", "desc").toSort();

        assertThat(sort).containsExactly(
                Sort.Order.desc("registrationNumber"), Sort.Order.asc("name"), Sort.Order.asc("idMerchant"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"city", "", "id"})
    void unknownSortIsRejected(String param) {
        assertThatThrownBy(() -> MerchantSort.of(param, "asc"))
                .isInstanceOf(ApiException.class)
                .hasMessage("sort must be name, oib or status");
    }

    @Test
    void unknownDirectionIsRejected() {
        assertThatThrownBy(() -> MerchantSort.of("name", "up"))
                .isInstanceOf(ApiException.class)
                .hasMessage("direction must be asc or desc");
    }

    @ParameterizedTest
    @ValueSource(strings = {"ACTIVE", "deactivated", "Hold"})
    void knownStatusesAreValidInAnyCase(String status) {
        assertThat(MerchantStatus.isValid(status)).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {"ACTVE", "CLOSED", ""})
    void unknownStatusesAreInvalid(String status) {
        assertThat(MerchantStatus.isValid(status)).isFalse();
    }
}

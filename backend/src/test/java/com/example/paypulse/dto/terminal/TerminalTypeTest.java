package com.example.paypulse.dto.terminal;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class TerminalTypeTest {

    @ParameterizedTest
    @CsvSource({"SoftPOS V1, SOFTPOS_V1", "softpos v1, SOFTPOS_V1", "SOFTPOS_V2, SOFTPOS_V2", "softposv2, SOFTPOS_V2",
            "pos, POS", "Mobile, MOBILE", "ecr, ECR", "VIRTUAL, VIRTUAL"})
    void knownTypesAreFoundIgnoringCaseSpacesAndUnderscores(String param, TerminalType expected) {
        assertThat(TerminalType.fromParam(param)).contains(expected);
    }

    @ParameterizedTest
    @ValueSource(strings = {"SoftPOS V3", "SATURN 1000 Mobile", "PO S X", "terminal"})
    void unknownTypesAreNotFound(String param) {
        assertThat(TerminalType.fromParam(param)).isEmpty();
    }

    @ParameterizedTest
    @CsvSource({"SOFTPOS_V1, softposv1", "POS, pos"})
    void keyIsTheLabelWithoutSpacesInLowerCase(TerminalType type, String key) {
        assertThat(type.key()).isEqualTo(key);
    }
}

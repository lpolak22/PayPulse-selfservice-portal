package com.example.paypulse.integration.sevenpay.importer;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class SevenPayTerminalTypeTest {

    @ParameterizedTest
    @ValueSource(strings = {"SoftPOS V1", "SoftPOS V2", "softpos v1", "SOFTPOS V2", "SoftPOSV1"})
    void softPosV1AndV2AreAccepted(String type) {
        assertThat(SevenPayTerminalImporter.isSoftPos(type)).isTrue();
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"SoftPOS V3", "SATURN 1000 Mobile", "SATURN 1000 Countertop ECR F3", "ICT22x", "S1F2"})
    void otherTypesAreRejected(String type) {
        assertThat(SevenPayTerminalImporter.isSoftPos(type)).isFalse();
    }
}

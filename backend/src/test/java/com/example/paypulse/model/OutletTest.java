package com.example.paypulse.model;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class OutletTest {

    @ParameterizedTest
    @CsvSource({"ACTIVE, true", "HOLD, false", "DEACTIVATED, false", "UNKNOWN, false", "active, false"})
    void isActiveFollowsStatus(String status, boolean active) {
        Outlet outlet = new Outlet();

        outlet.setStatus(status);

        assertThat(outlet.getIsActive()).isEqualTo(active);
    }

    @ParameterizedTest
    @CsvSource({"ACTIVE, HOLD, false", "HOLD, ACTIVE, true"})
    void changingStatusUpdatesIsActive(String before, String after, boolean active) {
        Outlet outlet = new Outlet();
        outlet.setStatus(before);

        outlet.setStatus(after);

        assertThat(outlet.getIsActive()).isEqualTo(active);
    }
}

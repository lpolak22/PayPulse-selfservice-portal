package com.example.paypulse.api;

import com.example.paypulse.model.UserRole;
import com.example.paypulse.model.UserStatus;
import com.example.paypulse.support.ApiIntegrationTest;
import com.example.paypulse.support.UserFixtures.TestUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * An organisation must always keep at least one active Admin: the last one cannot be deleted, deactivated
 * or demoted. The guard itself belongs to PULSE-35; until it is merged those tests are disabled. The tests
 * that must keep passing once the guard exists (other admins remain) run now.
 */
class LastAdminGuardApiTest extends ApiIntegrationTest {

    private static final String PENDING_PULSE_35 =
            "PULSE-35 (last-Admin guard) is not implemented yet: enable once it is merged";

    private long organisationId;
    private TestUser admin;

    @BeforeEach
    void setUp() {
        organisationId = fixtures.organisation();
        admin = fixtures.user(organisationId, UserRole.ADMIN, UserStatus.ACTIVE);
        fixtures.user(organisationId, UserRole.USER, UserStatus.ACTIVE);
    }

    @Test
    void anAdminCanBeDeletedWhileAnotherActiveAdminRemains() throws Exception {
        TestUser other = fixtures.user(organisationId, UserRole.ADMIN, UserStatus.ACTIVE);

        performAs(admin, delete("/api/users/{id}", other.id())).andExpect(status().isNoContent());
    }

    @Test
    void anAdminCanBeDeactivatedWhileAnotherActiveAdminRemains() throws Exception {
        TestUser other = fixtures.user(organisationId, UserRole.ADMIN, UserStatus.ACTIVE);

        performAs(admin, withJson(patch("/api/users/{id}", other.id()), Map.of("status", "INACTIVE")))
                .andExpect(status().isOk());
    }

    @Test
    void anAdminCanBeDemotedWhileAnotherActiveAdminRemains() throws Exception {
        TestUser other = fixtures.user(organisationId, UserRole.ADMIN, UserStatus.ACTIVE);

        performAs(admin, withJson(patch("/api/users/{id}", other.id()), Map.of("role", "USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("USER"));
    }

    @Test
    void theLastAdminCanStillEditTheirOtherFields() throws Exception {
        performAs(admin, withJson(patch("/api/users/{id}", admin.id()), Map.of("firstName", "Renamed")))
                .andExpect(status().isOk());
    }

    @Test
    @Disabled(PENDING_PULSE_35)
    void theLastActiveAdminCannotBeDeleted() throws Exception {
        performAs(admin, delete("/api/users/{id}", admin.id())).andExpect(status().isConflict());

        assertThat(fixtures.row(admin.id())).containsEntry("deleted_at", null);
    }

    @Test
    @Disabled(PENDING_PULSE_35)
    void theLastActiveAdminCannotBeDeactivated() throws Exception {
        performAs(admin, withJson(patch("/api/users/{id}", admin.id()), Map.of("status", "INACTIVE")))
                .andExpect(status().isConflict());

        assertThat(fixtures.row(admin.id())).containsEntry("status", "ACTIVE");
    }

    @Test
    @Disabled(PENDING_PULSE_35)
    void theLastActiveAdminCannotBeDemoted() throws Exception {
        performAs(admin, withJson(patch("/api/users/{id}", admin.id()), Map.of("role", "USER")))
                .andExpect(status().isConflict());

        assertThat(fixtures.row(admin.id())).containsEntry("role", "ADMIN");
    }

    @Test
    @Disabled(PENDING_PULSE_35)
    void inactiveOrPendingAdminsDoNotCountAsRemainingAdmins() throws Exception {
        fixtures.user(organisationId, UserRole.ADMIN, UserStatus.INACTIVE);
        fixtures.user(organisationId, UserRole.ADMIN, UserStatus.PENDING_ACTIVATION);

        performAs(admin, withJson(patch("/api/users/{id}", admin.id()), Map.of("role", "USER")))
                .andExpect(status().isConflict());
    }

    @Test
    @Disabled(PENDING_PULSE_35)
    void adminsOfAnotherOrganisationDoNotCountAsRemainingAdmins() throws Exception {
        fixtures.user(fixtures.organisation(), UserRole.ADMIN, UserStatus.ACTIVE);

        performAs(admin, delete("/api/users/{id}", admin.id())).andExpect(status().isConflict());
    }
}

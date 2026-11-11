package com.example.paypulse.api;

import com.example.paypulse.dto.auth.TokenResponse;
import com.example.paypulse.model.UserRole;
import com.example.paypulse.model.UserStatus;
import com.example.paypulse.support.ApiIntegrationTest;
import com.example.paypulse.support.UserFixtures.TestUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.ResultActions;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class LastAdminGuardApiTest extends ApiIntegrationTest {

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
    void anAdminCanBeDeactivatedThroughTheDeactivateRouteWhileAnotherActiveAdminRemains() throws Exception {
        TestUser other = fixtures.user(organisationId, UserRole.ADMIN, UserStatus.ACTIVE);

        performAs(admin, post("/api/users/{id}/deactivate", other.id()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));
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
    void theLastAdminCanKeepTheirRoleAndStatusInAnUpdate() throws Exception {
        performAs(admin, withJson(patch("/api/users/{id}", admin.id()), Map.of("role", "ADMIN", "status", "ACTIVE")))
                .andExpect(status().isOk());
    }

    @Test
    void theLastActiveAdminCannotBeDeleted() throws Exception {
        expectLastAdmin(performAs(admin, delete("/api/users/{id}", admin.id())));

        assertThat(fixtures.row(admin.id())).containsEntry("deleted_at", null).containsEntry("is_active", true);
    }

    @Test
    void theLastActiveAdminCannotBeDeactivated() throws Exception {
        expectLastAdmin(performAs(admin, withJson(patch("/api/users/{id}", admin.id()), Map.of("status", "INACTIVE"))));

        assertThat(fixtures.row(admin.id())).containsEntry("status", "ACTIVE").containsEntry("is_active", true);
    }

    @Test
    void theLastActiveAdminCannotBeDeactivatedThroughTheDeactivateRoute() throws Exception {
        expectLastAdmin(performAs(admin, post("/api/users/{id}/deactivate", admin.id())));

        assertThat(fixtures.row(admin.id())).containsEntry("status", "ACTIVE").containsEntry("is_active", true);
    }

    @Test
    void theLastActiveAdminKeepsTheirSessionsWhenDeactivationIsRefused() throws Exception {
        TokenResponse tokens = login(admin);

        expectLastAdmin(performAs(admin, post("/api/users/{id}/deactivate", admin.id())));

        assertThat(openRefreshTokensOf(admin)).isEqualTo(1);
        performWithToken(tokens.accessToken(), get("/api/users")).andExpect(status().isOk());
    }

    @Test
    void theLastActiveAdminCannotBeDemoted() throws Exception {
        expectLastAdmin(performAs(admin, withJson(patch("/api/users/{id}", admin.id()), Map.of("role", "USER"))));

        assertThat(fixtures.row(admin.id())).containsEntry("role", "ADMIN");
    }

    @Test
    void inactiveOrPendingAdminsDoNotCountAsRemainingAdmins() throws Exception {
        fixtures.user(organisationId, UserRole.ADMIN, UserStatus.INACTIVE);
        fixtures.user(organisationId, UserRole.ADMIN, UserStatus.PENDING_ACTIVATION);

        expectLastAdmin(performAs(admin, withJson(patch("/api/users/{id}", admin.id()), Map.of("role", "USER"))));
    }

    @Test
    void deletedAdminsDoNotCountAsRemainingAdmins() throws Exception {
        fixtures.markDeleted(fixtures.user(organisationId, UserRole.ADMIN, UserStatus.ACTIVE));

        expectLastAdmin(performAs(admin, delete("/api/users/{id}", admin.id())));
    }

    @Test
    void adminsOfAnotherOrganisationDoNotCountAsRemainingAdmins() throws Exception {
        fixtures.user(fixtures.organisation(), UserRole.ADMIN, UserStatus.ACTIVE);

        expectLastAdmin(performAs(admin, delete("/api/users/{id}", admin.id())));
    }

    private static void expectLastAdmin(ResultActions result) throws Exception {
        result.andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("LAST_ADMIN"));
    }
}

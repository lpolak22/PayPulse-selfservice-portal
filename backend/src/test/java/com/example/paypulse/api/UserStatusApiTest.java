package com.example.paypulse.api;

import com.example.paypulse.dto.auth.TokenResponse;
import com.example.paypulse.model.UserRole;
import com.example.paypulse.model.UserStatus;
import com.example.paypulse.support.ApiIntegrationTest;
import com.example.paypulse.support.UserFixtures;
import com.example.paypulse.support.UserFixtures.TestUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.ResultActions;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class UserStatusApiTest extends ApiIntegrationTest {

    private long organisationId;
    private TestUser admin;

    @BeforeEach
    void setUp() {
        organisationId = fixtures.organisation();
        admin = fixtures.user(organisationId, UserRole.ADMIN, UserStatus.ACTIVE);
    }

    @Test
    void deactivateSetsStatusInactiveAndClearsIsActive() throws Exception {
        TestUser user = fixtures.user(organisationId, UserRole.USER, UserStatus.ACTIVE);

        deactivate(user)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(user.id()))
                .andExpect(jsonPath("$.status").value("INACTIVE"))
                .andExpect(jsonPath("$.active").value(false));

        assertThat(fixtures.row(user.id())).containsEntry("status", "INACTIVE").containsEntry("is_active", false);
    }

    @Test
    void deactivationRevokesEveryRefreshTokenOfTheUser() throws Exception {
        TestUser user = fixtures.user(organisationId, UserRole.USER, UserStatus.ACTIVE);
        TokenResponse phone = login(user);
        TokenResponse laptop = login(user);

        deactivate(user).andExpect(status().isOk());

        assertThat(openRefreshTokensOf(user)).isZero();
        expectInvalidRefreshToken(postRefresh(phone.refreshToken()));
        expectInvalidRefreshToken(postRefresh(laptop.refreshToken()));
    }

    @Test
    void aDeactivatedUsersAccessTokenIsRejectedOnTheNextRequest() throws Exception {
        TestUser user = fixtures.user(organisationId, UserRole.USER, UserStatus.ACTIVE);
        TokenResponse tokens = login(user);
        performWithToken(tokens.accessToken(), get("/api/auth/me")).andExpect(status().isOk());

        deactivate(user).andExpect(status().isOk());

        performWithToken(tokens.accessToken(), get("/api/auth/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
    }

    @Test
    void aDeactivatedUserCannotLogIn() throws Exception {
        TestUser user = fixtures.user(organisationId, UserRole.USER, UserStatus.ACTIVE);
        deactivate(user).andExpect(status().isOk());

        postLogin(user.email(), UserFixtures.PASSWORD)
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("ACCOUNT_INACTIVE"));
    }

    @Test
    void activateSetsStatusActiveAndIsActive() throws Exception {
        TestUser user = fixtures.user(organisationId, UserRole.USER, UserStatus.INACTIVE);

        activate(user)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.active").value(true));

        assertThat(fixtures.row(user.id())).containsEntry("status", "ACTIVE").containsEntry("is_active", true);
    }

    @Test
    void aReactivatedUserCanLogInWithTheirExistingPassword() throws Exception {
        TestUser user = fixtures.user(organisationId, UserRole.USER, UserStatus.ACTIVE);
        deactivate(user).andExpect(status().isOk());
        postLogin(user.email(), UserFixtures.PASSWORD).andExpect(status().isForbidden());

        activate(user).andExpect(status().isOk());

        TokenResponse tokens = login(user);
        performWithToken(tokens.accessToken(), get("/api/auth/me")).andExpect(status().isOk());
    }

    @Test
    void aUserWhoHasNotCompletedActivationCannotBeActivatedByAnAdmin() throws Exception {
        TestUser pending = fixtures.user(organisationId, UserRole.USER, UserStatus.PENDING_ACTIVATION);

        activate(pending)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("CONFLICT"));

        assertThat(fixtures.row(pending.id()))
                .containsEntry("status", "PENDING_ACTIVATION")
                .containsEntry("is_active", false);
    }

    @Test
    void activatingAnActiveUserChangesNothingAndIsNotAudited() throws Exception {
        TestUser user = fixtures.user(organisationId, UserRole.USER, UserStatus.ACTIVE);

        activate(user).andExpect(status().isOk()).andExpect(jsonPath("$.active").value(true));

        assertThat(auditActionsFor(user)).isEmpty();
    }

    @Test
    void deactivatingAnInactiveUserChangesNothingAndIsNotAudited() throws Exception {
        TestUser user = fixtures.user(organisationId, UserRole.USER, UserStatus.INACTIVE);

        deactivate(user).andExpect(status().isOk()).andExpect(jsonPath("$.active").value(false));

        assertThat(auditActionsFor(user)).isEmpty();
    }

    @Test
    void deactivationAndActivationAreRecordedInTheAuditLog() throws Exception {
        TestUser user = fixtures.user(organisationId, UserRole.USER, UserStatus.ACTIVE);

        deactivate(user).andExpect(status().isOk());
        activate(user).andExpect(status().isOk());

        List<Map<String, Object>> entries = jdbc.queryForList("""
                select action, id_user, entity_type, old_value ->> 'status' as old_status,
                       new_value ->> 'status' as new_status
                from audit_log where entity_id = ? order by id_audit_log""", user.id());
        assertThat(entries).extracting(e -> e.get("action")).containsExactly("USER_DEACTIVATED", "USER_ACTIVATED");
        assertThat(entries).allSatisfy(e -> assertThat(e).containsEntry("id_user", admin.id())
                .containsEntry("entity_type", "USER"));
        assertThat(entries).extracting(e -> e.get("old_status")).containsExactly("ACTIVE", "INACTIVE");
        assertThat(entries).extracting(e -> e.get("new_status")).containsExactly("INACTIVE", "ACTIVE");
    }

    @Test
    void unknownIdReturns404() throws Exception {
        performAs(admin, post("/api/users/{id}/activate", Long.MAX_VALUE)).andExpect(status().isNotFound());
        performAs(admin, post("/api/users/{id}/deactivate", Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    void usersOfAnotherOrganisationCannotBeDeactivated() throws Exception {
        TestUser foreign = fixtures.user(fixtures.organisation(), UserRole.USER, UserStatus.ACTIVE);

        deactivate(foreign).andExpect(status().isNotFound());

        assertThat(fixtures.row(foreign.id())).containsEntry("is_active", true);
    }

    private ResultActions activate(TestUser user) throws Exception {
        return performAs(admin, post("/api/users/{id}/activate", user.id()));
    }

    private ResultActions deactivate(TestUser user) throws Exception {
        return performAs(admin, post("/api/users/{id}/deactivate", user.id()));
    }

    private List<String> auditActionsFor(TestUser user) {
        return jdbc.queryForList("select action from audit_log where entity_id = ?", String.class, user.id());
    }
}

package com.example.paypulse.api;

import com.example.paypulse.model.UserRole;
import com.example.paypulse.model.UserStatus;
import com.example.paypulse.support.ApiIntegrationTest;
import com.example.paypulse.support.UserFixtures.TestUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.ResultActions;

import java.util.Locale;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class UserEmailUniquenessApiTest extends ApiIntegrationTest {

    private long organisationId;
    private TestUser admin;
    private TestUser existing;

    @BeforeEach
    void setUp() {
        organisationId = fixtures.organisation();
        admin = fixtures.user(organisationId, UserRole.ADMIN, UserStatus.ACTIVE);
        existing = fixtures.user(organisationId, UserRole.USER, UserStatus.ACTIVE);
    }

    @Test
    void creatingWithAnExistingEmailReturns409() throws Exception {
        expectEmailExists(create(existing.email()));
    }

    @Test
    void emailComparisonIgnoresCase() throws Exception {
        expectEmailExists(create(existing.email().toUpperCase(Locale.ROOT)));
    }

    @Test
    void emailOfAUserInAnotherOrganisationIsAlsoTaken() throws Exception {
        TestUser foreign = fixtures.user(fixtures.organisation(), UserRole.USER, UserStatus.ACTIVE);

        expectEmailExists(create(foreign.email()));
    }

    @Test
    void emailOfAnInactiveUserIsStillTaken() throws Exception {
        TestUser inactive = fixtures.user(organisationId, UserRole.USER, UserStatus.INACTIVE);

        expectEmailExists(create(inactive.email()));
    }

    @Test
    void emailOfADeletedUserCanBeReused() throws Exception {
        fixtures.markDeleted(existing);

        create(existing.email()).andExpect(status().isCreated());
    }

    @Test
    void changingToAnotherUsersEmailReturns409AndChangesNothing() throws Exception {
        TestUser other = fixtures.user(organisationId, UserRole.USER, UserStatus.ACTIVE);

        expectEmailExists(performAs(admin, withJson(patch("/api/users/{id}", other.id()),
                Map.of("email", existing.email().toUpperCase(Locale.ROOT)))));
        assertThat(fixtures.row(other.id())).containsEntry("email", other.email());
    }

    @Test
    void keepingOrRecasingTheOwnEmailIsAllowed() throws Exception {
        String recased = existing.email().toUpperCase(Locale.ROOT);

        performAs(admin, withJson(patch("/api/users/{id}", existing.id()), Map.of("email", recased)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(recased));
    }

    private ResultActions create(String email) throws Exception {
        return performAs(admin, withJson(post("/api/users"),
                Map.of("firstName", "Iva", "lastName", "Babic", "email", email, "role", "USER")));
    }

    private static void expectEmailExists(ResultActions result) throws Exception {
        result.andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("USER_EMAIL_EXISTS"));
    }
}

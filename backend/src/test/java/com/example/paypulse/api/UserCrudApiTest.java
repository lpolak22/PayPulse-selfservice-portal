package com.example.paypulse.api;

import com.example.paypulse.model.UserRole;
import com.example.paypulse.model.UserStatus;
import com.example.paypulse.support.ApiIntegrationTest;
import com.example.paypulse.support.UserFixtures;
import com.example.paypulse.support.UserFixtures.TestUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class UserCrudApiTest extends ApiIntegrationTest {

    private long organisationId;
    private TestUser admin;

    @BeforeEach
    void setUp() {
        organisationId = fixtures.organisation();
        admin = fixtures.user(organisationId, "Ana", "Admin", UserRole.ADMIN, UserStatus.ACTIVE);
    }

    @Test
    void createReturns201WithAPendingUserInTheAdminsOrganisation() throws Exception {
        String email = UserFixtures.uniqueEmail();

        performAs(admin, withJson(post("/api/users"), createBody(" Iva ", "Babic", email, "+385911234567")))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", endsWith("/api/users/" + createdId(email))))
                .andExpect(jsonPath("$.firstName").value("Iva"))
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.phone").value("+385911234567"))
                .andExpect(jsonPath("$.role").value("USER"))
                .andExpect(jsonPath("$.status").value("PENDING_ACTIVATION"))
                .andExpect(jsonPath("$.active").value(false))
                .andExpect(jsonPath("$.organisationId").value(organisationId));
    }

    @Test
    void createdUserCanBeReadBack() throws Exception {
        String email = UserFixtures.uniqueEmail();
        performAs(admin, withJson(post("/api/users"), createBody("Iva", "Babic", email, null)))
                .andExpect(status().isCreated());

        performAs(admin, get("/api/users/{id}", createdId(email)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.phone").doesNotExist());
    }

    @Test
    void getReturnsTheUser() throws Exception {
        TestUser user = fixtures.user(organisationId, "Iva", "Babic", UserRole.USER, UserStatus.ACTIVE);

        performAs(admin, get("/api/users/{id}", user.id()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(user.id()))
                .andExpect(jsonPath("$.lastName").value("Babic"))
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    void getUnknownIdReturns404() throws Exception {
        performAs(admin, get("/api/users/{id}", Long.MAX_VALUE))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }

    @Test
    void listReturnsTheOrganisationsUndeletedUsersSortedByName() throws Exception {
        TestUser zec = fixtures.user(organisationId, "Zoran", "Zec", UserRole.USER, UserStatus.ACTIVE);
        TestUser babic = fixtures.user(organisationId, "Iva", "Babic", UserRole.USER, UserStatus.ACTIVE);
        TestUser horvat = fixtures.user(organisationId, "Petar", "Horvat", UserRole.USER, UserStatus.INACTIVE);
        fixtures.markDeleted(fixtures.user(organisationId, "Dora", "Deleted", UserRole.USER, UserStatus.ACTIVE));
        fixtures.user(fixtures.organisation(), "Olga", "Other", UserRole.USER, UserStatus.ACTIVE);

        performAs(admin, get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(4))
                .andExpect(jsonPath("$.items[*].id", equalTo(
                        List.of((int) admin.id(), (int) babic.id(), (int) horvat.id(), (int) zec.id()))));
    }

    @Test
    void listFiltersBySearchRoleAndStatus() throws Exception {
        TestUser zec = fixtures.user(organisationId, "Zoran", "Zec", UserRole.USER, UserStatus.ACTIVE);
        TestUser horvat = fixtures.user(organisationId, "Petar", "Horvat", UserRole.USER, UserStatus.INACTIVE);

        expectListedIds(list().param("search", "ZORAN zec"), zec);
        expectListedIds(list().param("search", horvat.email().substring(0, 20)), horvat);
        expectListedIds(list().param("role", "ADMIN"), admin);
        expectListedIds(list().param("status", "INACTIVE"), horvat);
        expectListedIds(list().param("role", "USER").param("status", "ACTIVE"), zec);
        expectListedIds(list().param("search", "%"));
    }

    @Test
    void listIsPaginated() throws Exception {
        fixtures.user(organisationId, "Iva", "Babic", UserRole.USER, UserStatus.ACTIVE);
        TestUser horvat = fixtures.user(organisationId, "Petar", "Horvat", UserRole.USER, UserStatus.ACTIVE);
        TestUser zec = fixtures.user(organisationId, "Zoran", "Zec", UserRole.USER, UserStatus.ACTIVE);

        performAs(admin, get("/api/users?page=1&pageSize=2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page").value(1))
                .andExpect(jsonPath("$.pageSize").value(2))
                .andExpect(jsonPath("$.total").value(4))
                .andExpect(jsonPath("$.items[*].id", equalTo(List.of((int) horvat.id(), (int) zec.id()))));
    }

    @Test
    void updateChangesOnlyTheFieldsInTheBody() throws Exception {
        TestUser user = fixtures.user(organisationId, "Iva", "Babic", UserRole.USER, UserStatus.ACTIVE);

        performAs(admin, withJson(patch("/api/users/{id}", user.id()), Map.of("phone", "+385919999999")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.phone").value("+385919999999"))
                .andExpect(jsonPath("$.firstName").value("Iva"))
                .andExpect(jsonPath("$.email").value(user.email()))
                .andExpect(jsonPath("$.role").value("USER"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void updateCanChangeEveryEditableField() throws Exception {
        TestUser user = fixtures.user(organisationId, UserRole.USER, UserStatus.ACTIVE);
        String email = UserFixtures.uniqueEmail();
        Map<String, Object> body = Map.of("firstName", "Marta", "lastName", "Maric", "email", email,
                "phone", "+385915550101", "role", "ADMIN", "status", "INACTIVE");

        performAs(admin, withJson(patch("/api/users/{id}", user.id()), body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Marta"))
                .andExpect(jsonPath("$.lastName").value("Maric"))
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.role").value("ADMIN"))
                .andExpect(jsonPath("$.status").value("INACTIVE"))
                .andExpect(jsonPath("$.active").value(false));
    }

    @Test
    void activatingAnInactiveUserSetsActive() throws Exception {
        TestUser user = fixtures.user(organisationId, UserRole.USER, UserStatus.INACTIVE);

        performAs(admin, withJson(patch("/api/users/{id}", user.id()), Map.of("status", "ACTIVE")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    void emptyUpdateChangesNothing() throws Exception {
        TestUser user = fixtures.user(organisationId, "Iva", "Babic", UserRole.USER, UserStatus.ACTIVE);

        performAs(admin, withJson(patch("/api/users/{id}", user.id()), Map.of()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Iva"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void updateUnknownIdReturns404() throws Exception {
        performAs(admin, withJson(patch("/api/users/{id}", Long.MAX_VALUE), Map.of("firstName", "X")))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteSoftDeletesTheUser() throws Exception {
        TestUser user = fixtures.user(organisationId, UserRole.USER, UserStatus.ACTIVE);

        performAs(admin, delete("/api/users/{id}", user.id())).andExpect(status().isNoContent());

        performAs(admin, get("/api/users/{id}", user.id())).andExpect(status().isNotFound());
        performAs(admin, get("/api/users")).andExpect(jsonPath("$.items[*].id", not(hasItem((int) user.id()))));
        assertThat(fixtures.row(user.id()).get("deleted_at")).isNotNull();
        assertThat(fixtures.row(user.id())).containsEntry("is_active", false);
    }

    @Test
    void deletingTwiceReturns404() throws Exception {
        TestUser user = fixtures.user(organisationId, UserRole.USER, UserStatus.ACTIVE);
        performAs(admin, delete("/api/users/{id}", user.id())).andExpect(status().isNoContent());

        performAs(admin, delete("/api/users/{id}", user.id())).andExpect(status().isNotFound());
    }

    @Test
    void usersOfAnotherOrganisationAreInvisibleAndUntouchable() throws Exception {
        TestUser foreign = fixtures.user(fixtures.organisation(), "Olga", "Other", UserRole.USER, UserStatus.ACTIVE);

        performAs(admin, get("/api/users/{id}", foreign.id())).andExpect(status().isNotFound());
        performAs(admin, withJson(patch("/api/users/{id}", foreign.id()), Map.of("firstName", "Hacked")))
                .andExpect(status().isNotFound());
        performAs(admin, delete("/api/users/{id}", foreign.id())).andExpect(status().isNotFound());

        assertThat(fixtures.row(foreign.id())).containsEntry("first_name", "Olga").containsEntry("deleted_at", null);
    }

    @Test
    void createUpdateAndDeleteAreRecordedInTheAuditLog() throws Exception {
        String email = UserFixtures.uniqueEmail();
        performAs(admin, withJson(post("/api/users"), createBody("Iva", "Babic", email, null)))
                .andExpect(status().isCreated());
        long id = createdId(email);
        performAs(admin, withJson(patch("/api/users/{id}", id), Map.of("role", "ADMIN"))).andExpect(status().isOk());
        performAs(admin, delete("/api/users/{id}", id)).andExpect(status().isNoContent());

        List<Map<String, Object>> entries = jdbc.queryForList("""
                select action, id_user, entity_type, old_value is null as no_old, new_value is null as no_new
                from audit_log where entity_id = ? order by id_audit_log""", id);

        assertThat(entries).extracting(e -> e.get("action"))
                .containsExactly("USER_CREATED", "USER_UPDATED", "USER_DELETED");
        assertThat(entries).allSatisfy(e -> assertThat(e).containsEntry("id_user", admin.id())
                .containsEntry("entity_type", "USER"));
        assertThat(entries).extracting(e -> e.get("no_old")).containsExactly(true, false, false);
        assertThat(entries).extracting(e -> e.get("no_new")).containsExactly(false, false, true);
    }

    private void expectListedIds(MockHttpServletRequestBuilder request, TestUser... expected) throws Exception {
        List<Integer> ids = Arrays.stream(expected).map(u -> (int) u.id()).toList();
        performAs(admin, request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[*].id", equalTo(ids)));
    }

    private static MockHttpServletRequestBuilder list() {
        return get("/api/users");
    }

    private long createdId(String email) {
        return jdbc.queryForObject("select id_user from app_user where email = ? and deleted_at is null",
                Long.class, email);
    }

    private static Map<String, Object> createBody(String firstName, String lastName, String email, String phone) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("firstName", firstName);
        body.put("lastName", lastName);
        body.put("email", email);
        body.put("phone", phone);
        body.put("role", "USER");
        return body;
    }
}

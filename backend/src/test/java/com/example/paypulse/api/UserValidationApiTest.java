package com.example.paypulse.api;

import com.example.paypulse.model.UserRole;
import com.example.paypulse.model.UserStatus;
import com.example.paypulse.support.ApiIntegrationTest;
import com.example.paypulse.support.UserFixtures.TestUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.MediaType;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class UserValidationApiTest extends ApiIntegrationTest {

    private long organisationId;
    private TestUser admin;

    @BeforeEach
    void setUp() {
        organisationId = fixtures.organisation();
        admin = fixtures.user(organisationId, UserRole.ADMIN, UserStatus.ACTIVE);
    }

    static Stream<Arguments> invalidCreateFields() {
        return Stream.of(
                Arguments.of("firstName", null),
                Arguments.of("firstName", "   "),
                Arguments.of("firstName", "a".repeat(51)),
                Arguments.of("lastName", ""),
                Arguments.of("lastName", "a".repeat(51)),
                Arguments.of("email", null),
                Arguments.of("email", "not-an-email"),
                Arguments.of("email", "ana@localhost"),
                Arguments.of("phone", "0911234567"),
                Arguments.of("phone", "+385 91 123 4567"),
                Arguments.of("role", null));
    }

    @ParameterizedTest(name = "{0} = \"{1}\"")
    @MethodSource("invalidCreateFields")
    void createWithAnInvalidFieldReturns400AndSavesNothing(String field, String value) throws Exception {
        Map<String, Object> body = validCreateBody();
        body.put(field, value);

        performAs(admin, withJson(post("/api/users"), body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.fieldErrors[*].field", hasItem(field)));
        assertThat(usersWithEmail((String) validCreateBody().get("email"))).isZero();
    }

    @Test
    void createWithAnUnknownRoleReturns400() throws Exception {
        Map<String, Object> body = validCreateBody();
        body.put("role", "SUPERUSER");

        performAs(admin, withJson(post("/api/users"), body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("BAD_REQUEST"));
    }

    @Test
    void malformedJsonReturns400() throws Exception {
        performAs(admin, post("/api/users").contentType(MediaType.APPLICATION_JSON).content("{\"firstName\": "))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("BAD_REQUEST"));
    }

    static Stream<Arguments> invalidUpdateFields() {
        return Stream.of(
                Arguments.of("firstName", " "),
                Arguments.of("lastName", ""),
                Arguments.of("email", "iva@"),
                Arguments.of("phone", "091"));
    }

    @ParameterizedTest(name = "{0} = \"{1}\"")
    @MethodSource("invalidUpdateFields")
    void updateWithAnInvalidFieldReturns400AndChangesNothing(String field, String value) throws Exception {
        TestUser user = fixtures.user(organisationId, "Iva", "Babic", UserRole.USER, UserStatus.ACTIVE);

        performAs(admin, withJson(patch("/api/users/{id}", user.id()), Map.of(field, value)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.fieldErrors[0].field").value(field));
        assertThat(fixtures.row(user.id())).containsEntry("first_name", "Iva").containsEntry("email", user.email());
    }

    @Test
    void statusCannotGoBackToPendingActivation() throws Exception {
        TestUser user = fixtures.user(organisationId, UserRole.USER, UserStatus.ACTIVE);

        performAs(admin, withJson(patch("/api/users/{id}", user.id()), Map.of("status", "PENDING_ACTIVATION")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
        assertThat(fixtures.row(user.id())).containsEntry("status", "ACTIVE");
    }

    @Test
    void unknownRoleFilterReturns400() throws Exception {
        performAs(admin, get("/api/users").param("role", "GOD"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("BAD_REQUEST"));
    }

    @Test
    void nonNumericIdReturns400() throws Exception {
        performAs(admin, get("/api/users/abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("BAD_REQUEST"));
    }

    private Map<String, Object> validCreateBody() {
        Map<String, Object> body = new HashMap<>();
        body.put("firstName", "Iva");
        body.put("lastName", "Babic");
        body.put("email", "validation-" + organisationId + "@paypulse-test.example");
        body.put("phone", "+385911234567");
        body.put("role", "USER");
        return body;
    }

    private long usersWithEmail(String email) {
        return jdbc.queryForObject("select count(*) from app_user where email = ?", Long.class, email);
    }
}

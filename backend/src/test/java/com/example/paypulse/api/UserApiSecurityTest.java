package com.example.paypulse.api;

import com.example.paypulse.controller.AppUserController;
import com.example.paypulse.model.UserRole;
import com.example.paypulse.model.UserStatus;
import com.example.paypulse.support.ApiIntegrationTest;
import com.example.paypulse.support.UserFixtures.TestUser;
import com.example.paypulse.support.UserRoutes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.Map;
import java.util.function.LongFunction;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Only signed-in, active admins may use /api/users: the User role gets 403, and anonymous requests, invalid
 * tokens and the access tokens of deactivated or deleted users get 401 on every route.
 */
class UserApiSecurityTest extends ApiIntegrationTest {

    @Autowired
    @Qualifier("requestMappingHandlerMapping")
    private RequestMappingHandlerMapping handlerMapping;

    private long organisationId;
    private TestUser admin;
    private TestUser target;

    @BeforeEach
    void setUp() {
        organisationId = fixtures.organisation();
        admin = fixtures.user(organisationId, UserRole.ADMIN, UserStatus.ACTIVE);
        target = fixtures.user(organisationId, UserRole.USER, UserStatus.ACTIVE);
    }

    static Stream<?> routes() {
        return UserRoutes.all();
    }

    @Test
    void routeListCoversEveryUserManagementEndpoint() {
        Stream<String> controllerRoutes = handlerMapping.getHandlerMethods().entrySet().stream()
                .filter(entry -> entry.getValue().getBeanType().equals(AppUserController.class))
                .map(Map.Entry::getKey)
                .flatMap(UserApiSecurityTest::routeNames);

        assertThat(controllerRoutes).containsExactlyInAnyOrderElementsOf(UserRoutes.names());
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("routes")
    void adminIsAllowed(LongFunction<MockHttpServletRequestBuilder> route) throws Exception {
        int status = performAs(admin, route.apply(target.id())).andReturn().getResponse().getStatus();

        assertThat(status).isBetween(200, 299);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("routes")
    void userRoleIsForbidden(LongFunction<MockHttpServletRequestBuilder> route) throws Exception {
        TestUser user = fixtures.user(organisationId, UserRole.USER, UserStatus.ACTIVE);

        performAs(user, route.apply(target.id()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));
    }

    @Test
    void forbiddenUserChangesNothing() throws Exception {
        TestUser user = fixtures.user(organisationId, UserRole.USER, UserStatus.ACTIVE);

        performAs(user, delete("/api/users/{id}", target.id())).andExpect(status().isForbidden());

        assertThat(fixtures.row(target.id())).containsEntry("deleted_at", null).containsEntry("is_active", true);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("routes")
    void anonymousRequestIsUnauthorized(LongFunction<MockHttpServletRequestBuilder> route) throws Exception {
        expectUnauthorized(mockMvc.perform(route.apply(target.id())));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("routes")
    void invalidTokenIsUnauthorized(LongFunction<MockHttpServletRequestBuilder> route) throws Exception {
        expectUnauthorized(performWithToken("not-a-jwt", route.apply(target.id())));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("routes")
    void deactivatedAdminIsUnauthorized(LongFunction<MockHttpServletRequestBuilder> route) throws Exception {
        TestUser deactivated = fixtures.user(organisationId, UserRole.ADMIN, UserStatus.INACTIVE);

        expectUnauthorized(performAs(deactivated, route.apply(target.id())));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("routes")
    void deletedAdminIsUnauthorized(LongFunction<MockHttpServletRequestBuilder> route) throws Exception {
        TestUser deleted = fixtures.user(organisationId, UserRole.ADMIN, UserStatus.ACTIVE);
        fixtures.markDeleted(deleted);

        expectUnauthorized(performAs(deleted, route.apply(target.id())));
    }

    @Test
    void pendingAdminTokenIsUnauthorized() throws Exception {
        TestUser pending = fixtures.user(organisationId, UserRole.ADMIN, UserStatus.PENDING_ACTIVATION);

        expectUnauthorized(performAs(pending, get("/api/users")));
    }

    @Test
    void deactivatedUserRoleGets401Not403() throws Exception {
        TestUser deactivated = fixtures.user(organisationId, UserRole.USER, UserStatus.INACTIVE);

        expectUnauthorized(performAs(deactivated, get("/api/users")));
    }

    @Test
    void deactivationTakesEffectOnTheVeryNextRequest() throws Exception {
        TestUser otherAdmin = fixtures.user(organisationId, UserRole.ADMIN, UserStatus.ACTIVE);
        String token = accessToken(otherAdmin);
        performWithToken(token, get("/api/users")).andExpect(status().isOk());

        performAs(admin, withJson(patch("/api/users/{id}", otherAdmin.id()), Map.of("status", "INACTIVE")))
                .andExpect(status().isOk());

        expectUnauthorized(performWithToken(token, get("/api/users")));
    }

    @Test
    void deletionTakesEffectOnTheVeryNextRequest() throws Exception {
        TestUser otherAdmin = fixtures.user(organisationId, UserRole.ADMIN, UserStatus.ACTIVE);
        String token = accessToken(otherAdmin);

        performAs(admin, delete("/api/users/{id}", otherAdmin.id())).andExpect(status().isNoContent());

        expectUnauthorized(performWithToken(token, get("/api/users")));
    }

    @Test
    void demotionTakesEffectOnTheVeryNextRequest() throws Exception {
        TestUser otherAdmin = fixtures.user(organisationId, UserRole.ADMIN, UserStatus.ACTIVE);
        String token = accessToken(otherAdmin);

        performAs(admin, withJson(patch("/api/users/{id}", otherAdmin.id()), Map.of("role", "USER")))
                .andExpect(status().isOk());

        performWithToken(token, get("/api/users")).andExpect(status().isForbidden());
    }

    private static void expectUnauthorized(ResultActions result) throws Exception {
        result.andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"))
                .andExpect(header().exists(HttpHeaders.WWW_AUTHENTICATE));
    }

    private static Stream<String> routeNames(RequestMappingInfo info) {
        return info.getMethodsCondition().getMethods().stream()
                .flatMap(method -> info.getPatternValues().stream().map(pattern -> method.name() + " " + pattern));
    }
}

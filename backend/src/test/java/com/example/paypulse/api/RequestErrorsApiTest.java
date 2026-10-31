package com.example.paypulse.api;

import com.example.paypulse.model.UserRole;
import com.example.paypulse.model.UserStatus;
import com.example.paypulse.support.ApiIntegrationTest;
import com.example.paypulse.support.UserFixtures;
import com.example.paypulse.support.UserFixtures.TestUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;

import java.util.Map;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class RequestErrorsApiTest extends ApiIntegrationTest {

    private TestUser admin;
    private TestUser user;

    @BeforeEach
    void setUp() {
        long organisationId = fixtures.organisation();
        admin = fixtures.user(organisationId, UserRole.ADMIN, UserStatus.ACTIVE);
        user = fixtures.user(organisationId, UserRole.USER, UserStatus.ACTIVE);
    }

    @Test
    void getOnAPostOnlyRouteReturns405AndNamesTheAllowedMethod() throws Exception {
        expectError(performAs(user, get("/api/auth/refresh")), 405, "METHOD_NOT_ALLOWED")
                .andExpect(header().string(HttpHeaders.ALLOW, "POST"));
    }

    @Test
    void postOnAGetOnlyRouteReturns405() throws Exception {
        expectError(performAs(user, post("/api/auth/me")), 405, "METHOD_NOT_ALLOWED")
                .andExpect(header().string(HttpHeaders.ALLOW, containsString("GET")));
    }

    @Test
    void putOnAUserReturns405AndNamesEveryAllowedMethod() throws Exception {
        expectError(performAs(admin, put("/api/users/{id}", user.id())), 405, "METHOD_NOT_ALLOWED")
                .andExpect(header().string(HttpHeaders.ALLOW,
                        allOf(containsString("GET"), containsString("PATCH"), containsString("DELETE"))));
    }

    @Test
    void deleteOnTheUserListReturns405() throws Exception {
        expectError(performAs(admin, delete("/api/users")), 405, "METHOD_NOT_ALLOWED");
    }

    @Test
    void aWrongMethodDoesNotTellAnAnonymousClientThatTheRouteExists() throws Exception {
        expectError(mockMvc.perform(get("/api/auth/refresh")), 401, "UNAUTHORIZED");
    }

    @Test
    void aWrongMethodOnAnAdminRouteIsStillForbiddenForTheUserRole() throws Exception {
        expectError(performAs(user, put("/api/users/{id}", user.id())), 403, "FORBIDDEN");
    }

    @ParameterizedTest
    @ValueSource(strings = {"/api/auth/login", "/api/auth/refresh", "/api/auth/logout", "/api/auth/activation"})
    void aPlainTextBodyReturns415(String route) throws Exception {
        ResultActions result = mockMvc.perform(post(route).contentType(MediaType.TEXT_PLAIN).content("hello"));

        expectError(result, 415, "UNSUPPORTED_MEDIA_TYPE")
                .andExpect(header().string(HttpHeaders.ACCEPT, containsString(MediaType.APPLICATION_JSON_VALUE)));
    }

    @Test
    void aFormBodyReturns415() throws Exception {
        expectError(mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("email", user.email()).param("password", UserFixtures.PASSWORD)), 415,
                "UNSUPPORTED_MEDIA_TYPE");
    }

    @Test
    void aBodyWithoutAContentTypeReturns415() throws Exception {
        expectError(mockMvc.perform(post("/api/auth/login").content("{}")), 415, "UNSUPPORTED_MEDIA_TYPE");
    }

    @Test
    void aPlainTextBodyOnAnAdminRouteReturns415() throws Exception {
        expectError(performAs(admin, patch("/api/users/{id}", user.id()).contentType(MediaType.TEXT_PLAIN)
                .content("hello")), 415, "UNSUPPORTED_MEDIA_TYPE");
    }

    @Test
    void askingForXmlReturns406AsJson() throws Exception {
        expectError(performAs(user, get("/api/auth/me").accept(MediaType.APPLICATION_XML)), 406, "NOT_ACCEPTABLE");
    }

    @Test
    void askingForXmlStillGetsTheRealErrorAsJson() throws Exception {
        expectError(mockMvc.perform(withJson(post("/api/auth/login"),
                Map.of("email", user.email(), "password", "Wrong-Password-1")).accept(MediaType.APPLICATION_XML)),
                401, "INVALID_CREDENTIALS");
    }

    @Test
    void aValidationErrorIsJsonWhateverTheClientAsksFor() throws Exception {
        expectError(mockMvc.perform(withJson(post("/api/auth/login"), Map.of("email", "", "password", "x"))
                .accept(MediaType.TEXT_PLAIN)), 400, "VALIDATION_ERROR");
    }

    @Test
    void anUnknownRouteReturns404ToASignedInUser() throws Exception {
        expectError(performAs(user, get("/api/nothing-here")), 404, "NOT_FOUND");
    }

    @Test
    void anUnknownRouteReturns401ToAnAnonymousClient() throws Exception {
        expectError(mockMvc.perform(get("/api/nothing-here")), 401, "UNAUTHORIZED");
    }

    private static ResultActions expectError(ResultActions result, int status, String code) throws Exception {
        return result.andExpect(status().is(status))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(code))
                .andExpect(jsonPath("$.message").isNotEmpty());
    }
}

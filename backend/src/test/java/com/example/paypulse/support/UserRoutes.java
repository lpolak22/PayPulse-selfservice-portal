package com.example.paypulse.support;

import org.junit.jupiter.api.Named;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.List;
import java.util.function.LongFunction;
import java.util.stream.Stream;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

public final class UserRoutes {

    public static final String CREATE = "POST /api/users";
    public static final String LIST = "GET /api/users";
    public static final String GET = "GET /api/users/{id}";
    public static final String UPDATE = "PATCH /api/users/{id}";
    public static final String DELETE = "DELETE /api/users/{id}";
    public static final String ACTIVATE = "POST /api/users/{id}/activate";
    public static final String DEACTIVATE = "POST /api/users/{id}/deactivate";
    public static final String RESEND_ACTIVATION = "POST /api/users/{id}/resend-activation";

    private UserRoutes() {
    }

    public static List<String> names() {
        return List.of(CREATE, LIST, GET, UPDATE, DELETE, ACTIVATE, DEACTIVATE, RESEND_ACTIVATION);
    }

    public static Stream<Named<LongFunction<MockHttpServletRequestBuilder>>> all() {
        return Stream.concat(forActiveTarget(),
                Stream.of(Named.of(RESEND_ACTIVATION, id -> post("/api/users/{id}/resend-activation", id))));
    }

    public static Stream<Named<LongFunction<MockHttpServletRequestBuilder>>> forActiveTarget() {
        return Stream.of(
                Named.of(CREATE, id -> post("/api/users").contentType(MediaType.APPLICATION_JSON).content("""
                        {"firstName": "Nina", "lastName": "Novak", "email": "%s", "role": "USER"}
                        """.formatted(UserFixtures.uniqueEmail()))),
                Named.of(LIST, id -> get("/api/users")),
                Named.of(GET, id -> get("/api/users/{id}", id)),
                Named.of(UPDATE, id -> patch("/api/users/{id}", id).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"firstName\": \"Changed\"}")),
                Named.of(DELETE, id -> delete("/api/users/{id}", id)),
                Named.of(ACTIVATE, id -> post("/api/users/{id}/activate", id)),
                Named.of(DEACTIVATE, id -> post("/api/users/{id}/deactivate", id)));
    }
}

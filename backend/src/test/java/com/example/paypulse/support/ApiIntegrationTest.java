package com.example.paypulse.support;

import com.example.paypulse.config.JwtProperties;
import com.example.paypulse.dto.auth.TokenResponse;
import com.example.paypulse.repository.AppUserRepository;
import com.example.paypulse.service.AccessTokenService;
import com.example.paypulse.support.UserFixtures.TestUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.json.JsonMapper;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Import(RequestBoundaryConfig.class)
@Transactional
@Tag("integration")
public abstract class ApiIntegrationTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected JsonMapper jsonMapper;

    @Autowired
    protected JdbcTemplate jdbc;

    @Autowired
    protected JwtProperties jwtProperties;

    @Autowired
    private AccessTokenService accessTokenService;

    @Autowired
    private AppUserRepository appUserRepository;

    protected UserFixtures fixtures;

    @DynamicPropertySource
    static void database(DynamicPropertyRegistry registry) {
        TestDatabase.register(registry);
    }

    @BeforeEach
    void createFixtures() {
        fixtures = new UserFixtures(jdbc);
    }

    protected ResultActions performAs(TestUser user, MockHttpServletRequestBuilder request) throws Exception {
        return performWithToken(accessToken(user), request);
    }

    protected ResultActions performWithToken(String accessToken, MockHttpServletRequestBuilder request)
            throws Exception {
        return mockMvc.perform(request.header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken));
    }

    protected String accessToken(TestUser user) {
        return accessTokenService.issue(appUserRepository.findById(user.id()).orElseThrow());
    }

    protected MockHttpServletRequestBuilder withJson(MockHttpServletRequestBuilder request, Object body) {
        return request.contentType(MediaType.APPLICATION_JSON).content(jsonMapper.writeValueAsString(body));
    }

    protected ResultActions postLogin(String email, String password) throws Exception {
        return mockMvc.perform(withJson(post("/api/auth/login"), Map.of("email", email, "password", password)));
    }

    protected TokenResponse login(TestUser user) throws Exception {
        String body = postLogin(user.email(), UserFixtures.PASSWORD)
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return jsonMapper.readValue(body, TokenResponse.class);
    }

    protected ResultActions postRefresh(String refreshToken) throws Exception {
        return mockMvc.perform(withJson(post("/api/auth/refresh"), Map.of("refreshToken", refreshToken)));
    }

    protected TokenResponse refreshed(ResultActions refreshResult) throws Exception {
        String body = refreshResult.andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        return jsonMapper.readValue(body, TokenResponse.class);
    }

    protected ResultActions postLogout(String refreshToken) throws Exception {
        return mockMvc.perform(withJson(post("/api/auth/logout"), Map.of("refreshToken", refreshToken)));
    }

    protected static void expectInvalidRefreshToken(ResultActions result) throws Exception {
        result.andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("INVALID_REFRESH_TOKEN"));
    }

    protected long openRefreshTokensOf(TestUser owner) {
        return jdbc.queryForObject("select count(*) from refresh_token where id_user = ? and used_at is null",
                Long.class, owner.id());
    }
}

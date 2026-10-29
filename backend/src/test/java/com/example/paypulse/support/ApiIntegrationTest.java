package com.example.paypulse.support;

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
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.json.JsonMapper;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Base class for API tests: the full application with security, a real PostgreSQL database (see
 * {@link TestDatabase}) and MockMvc. Each test runs in a transaction that is rolled back afterwards.
 */
@SpringBootTest(properties = {
        "spring.jpa.show-sql=false",
        "jwt.secret=" + ApiIntegrationTest.JWT_SECRET
})
@AutoConfigureMockMvc
@Import(RequestBoundaryConfig.class)
@Transactional
@Tag("integration")
public abstract class ApiIntegrationTest {

    /** Signing key used only by the tests; the application reads the real one from JWT_SECRET. */
    public static final String JWT_SECRET = "paypulse-test-only-jwt-secret-0123456789";

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected JsonMapper jsonMapper;

    @Autowired
    protected JdbcTemplate jdbc;

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

    /**
     * Sends the request with a freshly issued access token for {@code user}. The token is issued whatever the
     * user's status, like a token that was issued before the user was deactivated or deleted.
     */
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

    /** Logs {@code user} in through the API with the fixture password and returns the issued tokens. */
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

    protected long openRefreshTokensOf(TestUser owner) {
        return jdbc.queryForObject("select count(*) from refresh_token where id_user = ? and used_at is null",
                Long.class, owner.id());
    }
}

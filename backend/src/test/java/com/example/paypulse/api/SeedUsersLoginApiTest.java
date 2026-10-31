package com.example.paypulse.api;

import com.example.paypulse.dto.auth.TokenResponse;
import com.example.paypulse.model.Organisation;
import com.example.paypulse.repository.AppUserRepository;
import com.example.paypulse.repository.OrganisationRepository;
import com.example.paypulse.seed.config.SeedProperties;
import com.example.paypulse.seed.service.UserSeedService;
import com.example.paypulse.support.ApiIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class SeedUsersLoginApiTest extends ApiIntegrationTest {

    @Value("${SEED_TEST_PASSWORD:Password123!}")
    private String seedPassword;

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private OrganisationRepository organisationRepository;

    private long organisationId;

    @BeforeEach
    void seedUsers() {
        organisationId = fixtures.organisation();
        Organisation organisation = organisationRepository.findById(organisationId).orElseThrow();
        new UserSeedService(appUserRepository, new BCryptPasswordEncoder(4), new SeedProperties(seedPassword, 0, 0))
                .createUsersForOrganisation(organisation);
        appUserRepository.flush();
    }

    @ParameterizedTest(name = "{0} {1}")
    @CsvSource({"Admin, One", "Admin, Two", "User, One", "User, Two", "User, Three"})
    void activeSeedUsersLogIn(String firstName, String lastName) throws Exception {
        postLogin(seededEmail(firstName, lastName), seedPassword)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tokenType").value("Bearer"));
    }

    @Test
    void seededAdminMayManageUsers() throws Exception {
        performWithToken(logIn("Admin", "One").accessToken(), get("/api/users")).andExpect(status().isOk());
    }

    @Test
    void seededUserIsDeniedTheAdminRoutes() throws Exception {
        performWithToken(logIn("User", "One").accessToken(), get("/api/users")).andExpect(status().isForbidden());
    }

    @Test
    void wrongPasswordIsRejected() throws Exception {
        postLogin(seededEmail("User", "One"), seedPassword + "-wrong")
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("INVALID_CREDENTIALS"));
    }

    @Test
    void deactivatedSeedUserIsToldTheAccountIsInactive() throws Exception {
        postLogin(seededEmail("Deactivated", "User"), seedPassword)
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("ACCOUNT_INACTIVE"));
    }

    @Test
    void pendingSeedUserHasNoPasswordYet() throws Exception {
        postLogin(seededEmail("Pending", "User"), seedPassword)
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("INVALID_CREDENTIALS"));
    }

    private TokenResponse logIn(String firstName, String lastName) throws Exception {
        String body = postLogin(seededEmail(firstName, lastName), seedPassword)
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return jsonMapper.readValue(body, TokenResponse.class);
    }

    private String seededEmail(String firstName, String lastName) {
        return jdbc.queryForObject("select email from app_user where id_organisation = ? and first_name = ? "
                + "and last_name = ?", String.class, organisationId, firstName, lastName);
    }
}

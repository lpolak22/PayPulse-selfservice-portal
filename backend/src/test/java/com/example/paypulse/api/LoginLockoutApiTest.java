package com.example.paypulse.api;

import com.example.paypulse.config.LoginLockoutProperties;
import com.example.paypulse.model.UserRole;
import com.example.paypulse.model.UserStatus;
import com.example.paypulse.service.UnknownLoginAttemptService;
import com.example.paypulse.support.ApiIntegrationTest;
import com.example.paypulse.support.UserFixtures;
import com.example.paypulse.support.UserFixtures.TestUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.ResultActions;

import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class LoginLockoutApiTest extends ApiIntegrationTest {

    private static final String WRONG_PASSWORD = "Wrong-Password-1";

    @Autowired
    private LoginLockoutProperties lockoutProperties;

    @Autowired
    private UnknownLoginAttemptService unknownLoginAttemptService;

    private TestUser user;

    @BeforeEach
    void setUp() {
        user = fixtures.user(fixtures.organisation(), UserRole.USER, UserStatus.ACTIVE);
    }

    @Test
    void theDefaultsAreFiveAttemptsAndAFifteenMinuteLock() {
        assertThat(lockoutProperties.maxAttempts()).isEqualTo(5);
        assertThat(lockoutProperties.lockoutDuration()).isEqualTo(Duration.ofMinutes(15));
    }

    @Test
    void remainingAttemptsCountDownFromFiveToZeroAndTheAccountLocks() throws Exception {
        for (int remaining = 4; remaining >= 1; remaining--) {
            postLogin(user.email(), WRONG_PASSWORD)
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.code").value("INVALID_CREDENTIALS"))
                    .andExpect(jsonPath("$.remainingAttempts").value(remaining));
        }

        postLogin(user.email(), WRONG_PASSWORD)
                .andExpect(status().isLocked())
                .andExpect(jsonPath("$.code").value("ACCOUNT_LOCKED"))
                .andExpect(jsonPath("$.remainingAttempts").doesNotExist());
    }

    @Test
    void eachWrongPasswordIsCountedInTheDatabase() throws Exception {
        postLogin(user.email(), WRONG_PASSWORD).andExpect(status().isUnauthorized());
        postLogin(user.email(), WRONG_PASSWORD).andExpect(status().isUnauthorized());

        assertThat(failedLoginCount()).isEqualTo(2);
    }

    @Test
    void lockingStoresWhenTheLockEnds() throws Exception {
        Duration lockoutDuration = lockoutProperties.lockoutDuration();
        LocalDateTime before = LocalDateTime.now().withNano(0);

        lockTheAccount();

        assertThat(lockedUntil()).isBetween(before.plus(lockoutDuration), LocalDateTime.now().plus(lockoutDuration));
        assertThat(failedLoginCount()).isEqualTo(lockoutProperties.maxAttempts());
    }

    @Test
    void lockingIsAuditedOnceAsAccountLocked() throws Exception {
        lockTheAccount();
        postLogin(user.email(), UserFixtures.PASSWORD).andExpect(status().isLocked());

        assertThat(accountLockedEntries()).singleElement().satisfies(entry -> assertThat(entry)
                .containsEntry("id_user", user.id())
                .containsEntry("entity_id", user.id())
                .containsEntry("email", user.email())
                .containsEntry("failed_attempts", String.valueOf(lockoutProperties.maxAttempts()))
                .containsEntry("locked_until", lockedUntil().toString()));
    }

    @Test
    void aLockedAccountRejectsTheRightPassword() throws Exception {
        lockTheAccount();

        postLogin(user.email(), UserFixtures.PASSWORD)
                .andExpect(status().isLocked())
                .andExpect(jsonPath("$.code").value("ACCOUNT_LOCKED"));
        assertThat(openRefreshTokensOf(user)).isZero();
    }

    @Test
    void wrongPasswordsWhileLockedDoNotExtendTheLock() throws Exception {
        lockTheAccount();
        LocalDateTime lockedUntil = lockedUntil();

        postLogin(user.email(), WRONG_PASSWORD).andExpect(status().isLocked());

        assertThat(lockedUntil()).isEqualTo(lockedUntil);
        assertThat(failedLoginCount()).isEqualTo(lockoutProperties.maxAttempts());
    }

    @Test
    void successfulLoginResetsTheCountdown() throws Exception {
        postLogin(user.email(), WRONG_PASSWORD).andExpect(status().isUnauthorized());
        postLogin(user.email(), WRONG_PASSWORD).andExpect(status().isUnauthorized());

        login(user);

        assertThat(failedLoginCount()).isZero();
        postLogin(user.email(), WRONG_PASSWORD)
                .andExpect(jsonPath("$.remainingAttempts").value(lockoutProperties.maxAttempts() - 1));
    }

    @Test
    void theRightPasswordWorksOnceTheLockHasExpired() throws Exception {
        lockTheAccount();
        expireTheLock();

        login(user);

        Map<String, Object> row = fixtures.row(user.id());
        assertThat(((Number) row.get("failed_login_count")).intValue()).isZero();
        assertThat(row.get("locked_until")).isNull();
    }

    @Test
    void theCountdownStartsAgainOnceTheLockHasExpired() throws Exception {
        lockTheAccount();
        expireTheLock();

        postLogin(user.email(), WRONG_PASSWORD)
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.remainingAttempts").value(lockoutProperties.maxAttempts() - 1));
    }

    @Test
    void anUnknownEmailAnswersExactlyLikeARealAccountThroughTheLockout() throws Exception {
        String unknownEmail = UserFixtures.uniqueEmail();

        for (int attempt = 1; attempt <= lockoutProperties.maxAttempts() + 1; attempt++) {
            String realAnswer = answer(postLogin(user.email(), WRONG_PASSWORD));
            String unknownAnswer = answer(postLogin(unknownEmail, WRONG_PASSWORD));

            assertThat(unknownAnswer).as("attempt %d", attempt).isEqualTo(realAnswer);
        }
    }

    @Test
    void anUnknownEmailIsCountedRegardlessOfLetterCase() throws Exception {
        String unknownEmail = UserFixtures.uniqueEmail();
        postLogin(unknownEmail.toUpperCase(Locale.ROOT), WRONG_PASSWORD).andExpect(status().isUnauthorized());

        postLogin(" " + unknownEmail + " ", WRONG_PASSWORD)
                .andExpect(jsonPath("$.remainingAttempts").value(lockoutProperties.maxAttempts() - 2));
    }

    @Test
    void anUnknownEmailIsStoredOnlyAsAHash() throws Exception {
        String unknownEmail = UserFixtures.uniqueEmail();

        postLogin(unknownEmail, WRONG_PASSWORD).andExpect(status().isUnauthorized());

        Map<String, Object> row = jdbc.queryForMap("""
                select email_hash, failed_login_count from unknown_login_attempt
                where email_hash = encode(sha256(convert_to(?, 'UTF8')), 'hex')""", unknownEmail);
        assertThat((String) row.get("email_hash")).doesNotContain(unknownEmail);
        assertThat(((Number) row.get("failed_login_count")).intValue()).isEqualTo(1);
    }

    @Test
    void aLockedUnknownEmailWorksAgainOnceTheLockHasExpired() throws Exception {
        String unknownEmail = UserFixtures.uniqueEmail();
        for (int attempt = 1; attempt < lockoutProperties.maxAttempts(); attempt++) {
            postLogin(unknownEmail, WRONG_PASSWORD).andExpect(status().isUnauthorized());
        }
        postLogin(unknownEmail, WRONG_PASSWORD).andExpect(status().isLocked());

        jdbc.update("update unknown_login_attempt set locked_until = ? where email_hash = "
                        + "encode(sha256(convert_to(?, 'UTF8')), 'hex')",
                Timestamp.valueOf(LocalDateTime.now().minusSeconds(1)), unknownEmail);

        postLogin(unknownEmail, WRONG_PASSWORD)
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.remainingAttempts").value(lockoutProperties.maxAttempts() - 1));
    }

    @Test
    void cleanupForgetsUnknownEmailsUntouchedForAWeek() throws Exception {
        String staleEmail = UserFixtures.uniqueEmail();
        String recentEmail = UserFixtures.uniqueEmail();
        postLogin(staleEmail, WRONG_PASSWORD).andExpect(status().isUnauthorized());
        postLogin(recentEmail, WRONG_PASSWORD).andExpect(status().isUnauthorized());
        jdbc.update("update unknown_login_attempt set updated_at = current_timestamp - interval '8 days' "
                + "where email_hash = encode(sha256(convert_to(?, 'UTF8')), 'hex')", staleEmail);

        unknownLoginAttemptService.deleteStaleAttempts();

        assertThat(unknownAttemptRows(staleEmail)).isZero();
        assertThat(unknownAttemptRows(recentEmail)).isEqualTo(1);
    }

    @Test
    void otherLoginErrorsCarryNoRemainingAttempts() throws Exception {
        TestUser inactive = fixtures.user(fixtures.organisation(), UserRole.USER, UserStatus.INACTIVE);

        postLogin(inactive.email(), UserFixtures.PASSWORD)
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.remainingAttempts").doesNotExist());
    }

    private void lockTheAccount() throws Exception {
        for (int failed = 1; failed < lockoutProperties.maxAttempts(); failed++) {
            postLogin(user.email(), WRONG_PASSWORD).andExpect(status().isUnauthorized());
        }
        postLogin(user.email(), WRONG_PASSWORD).andExpect(status().isLocked());
    }

    private void expireTheLock() {
        jdbc.update("update app_user set locked_until = ? where id_user = ?",
                Timestamp.valueOf(LocalDateTime.now().minusSeconds(1)), user.id());
    }

    private LocalDateTime lockedUntil() {
        return ((Timestamp) fixtures.row(user.id()).get("locked_until")).toLocalDateTime();
    }

    private int failedLoginCount() {
        return ((Number) fixtures.row(user.id()).get("failed_login_count")).intValue();
    }

    private long unknownAttemptRows(String email) {
        return jdbc.queryForObject("""
                select count(*) from unknown_login_attempt
                where email_hash = encode(sha256(convert_to(?, 'UTF8')), 'hex')""", Long.class, email);
    }

    private static String answer(ResultActions result) throws Exception {
        MockHttpServletResponse response = result.andReturn().getResponse();
        return response.getStatus() + " " + response.getContentAsString(StandardCharsets.UTF_8);
    }

    private List<Map<String, Object>> accountLockedEntries() {
        return jdbc.queryForList("""
                select id_user, entity_id, new_value ->> 'email' as email,
                       new_value ->> 'failedAttempts' as failed_attempts,
                       new_value ->> 'lockedUntil' as locked_until
                from audit_log where action = 'ACCOUNT_LOCKED' and entity_id = ?""", user.id());
    }
}

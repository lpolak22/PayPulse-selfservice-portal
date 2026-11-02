package com.example.paypulse.api;

import com.example.paypulse.config.LoginLockoutProperties;
import com.example.paypulse.model.UserRole;
import com.example.paypulse.model.UserStatus;
import com.example.paypulse.support.ApiIntegrationTest;
import com.example.paypulse.support.UserFixtures;
import com.example.paypulse.support.UserFixtures.TestUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class LoginLockoutApiTest extends ApiIntegrationTest {

    private static final String WRONG_PASSWORD = "Wrong-Password-1";

    @Autowired
    private LoginLockoutProperties lockoutProperties;

    private TestUser user;

    @BeforeEach
    void setUp() {
        user = fixtures.user(fixtures.organisation(), UserRole.USER, UserStatus.ACTIVE);
    }

    @Test
    void theLastAllowedWrongPasswordLocksTheAccountFor423() throws Exception {
        failLoginsBeforeTheLimit();

        postLogin(user.email(), WRONG_PASSWORD)
                .andExpect(status().isLocked())
                .andExpect(jsonPath("$.code").value("ACCOUNT_LOCKED"))
                .andExpect(jsonPath("$.remainingAttempts").doesNotExist());
    }

    @Test
    void lockingStoresWhenTheLockEnds() throws Exception {
        Duration lockoutDuration = lockoutProperties.lockoutDuration();
        LocalDateTime before = LocalDateTime.now().withNano(0);

        lockTheAccount();

        LocalDateTime lockedUntil = lockedUntil();
        assertThat(lockedUntil).isBetween(before.plus(lockoutDuration), LocalDateTime.now().plus(lockoutDuration));
        assertThat(failedLoginCount()).isEqualTo(lockoutProperties.maxAttempts());
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

    private void failLoginsBeforeTheLimit() throws Exception {
        for (int failed = 1; failed < lockoutProperties.maxAttempts(); failed++) {
            postLogin(user.email(), WRONG_PASSWORD).andExpect(status().isUnauthorized());
        }
    }

    private void lockTheAccount() throws Exception {
        failLoginsBeforeTheLimit();
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
}

package com.example.paypulse.support;

import com.example.paypulse.model.UserRole;
import com.example.paypulse.model.UserStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Map;
import java.util.UUID;

/**
 * Inserts organisations and users straight into the database. Every call creates fresh rows with unique
 * names and emails, so tests never depend on the seed data or on each other.
 */
public final class UserFixtures {

    /** Password of every fixture user that has one (ACTIVE and INACTIVE users). */
    public static final String PASSWORD = "Test-Password-1";
    private static final String PASSWORD_HASH = new BCryptPasswordEncoder(4).encode(PASSWORD);

    private final JdbcTemplate jdbc;

    UserFixtures(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public long organisation() {
        return jdbc.queryForObject(
                "insert into organisation (name, status) values (?, 'ACTIVE') returning id_organisation",
                Long.class, "Test organisation " + UUID.randomUUID());
    }

    public TestUser user(long organisationId, UserRole role, UserStatus status) {
        return user(organisationId, "Test", role.name() + " " + status.name(), role, status);
    }

    public TestUser user(long organisationId, String firstName, String lastName, UserRole role, UserStatus status) {
        String email = uniqueEmail();
        boolean hasPassword = status != UserStatus.PENDING_ACTIVATION;
        long id = jdbc.queryForObject("""
                        insert into app_user (id_organisation, first_name, last_name, email, role, status,
                                              password_hash, is_active)
                        values (?, ?, ?, ?, ?, ?, ?, ?)
                        returning id_user
                        """, Long.class, organisationId, firstName, lastName, email, role.name(), status.name(),
                hasPassword ? PASSWORD_HASH : null, status == UserStatus.ACTIVE);
        return new TestUser(id, email);
    }

    public void markDeleted(TestUser user) {
        jdbc.update("update app_user set deleted_at = current_timestamp, is_active = false where id_user = ?",
                user.id());
    }

    public Map<String, Object> row(long userId) {
        return jdbc.queryForMap("select * from app_user where id_user = ?", userId);
    }

    public static String uniqueEmail() {
        return "user-" + UUID.randomUUID() + "@paypulse-test.example";
    }

    public record TestUser(long id, String email) {
    }
}

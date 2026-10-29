package com.example.paypulse.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class AppUserTest {

    @Test
    void newUserStartsPendingAndInactive() {
        AppUser user = newUser();

        assertThat(user.getStatus()).isEqualTo(UserStatus.PENDING_ACTIVATION);
        assertThat(user.isActive()).isFalse();
        assertThat(user.getDeletedAt()).isNull();
    }

    @Test
    void namesAndEmailAreStripped() {
        AppUser user = AppUser.createPending(new Organisation(), "  Iva ", " Babic  ", " iva@example.com ",
                "+385911234567", UserRole.USER);

        assertThat(user.getFirstName()).isEqualTo("Iva");
        assertThat(user.getLastName()).isEqualTo("Babic");
        assertThat(user.getEmail()).isEqualTo("iva@example.com");
    }

    @Test
    void activatingSetsTheActiveFlag() {
        AppUser user = newUser();

        user.changeStatus(UserStatus.ACTIVE);

        assertThat(user.isActive()).isTrue();
    }

    @ParameterizedTest
    @EnumSource(value = UserStatus.class, names = "ACTIVE", mode = EnumSource.Mode.EXCLUDE)
    void everyOtherStatusClearsTheActiveFlag(UserStatus status) {
        AppUser user = newUser();
        user.changeStatus(UserStatus.ACTIVE);

        user.changeStatus(status);

        assertThat(user.isActive()).isFalse();
    }

    @Test
    void deletingClearsTheActiveFlag() {
        AppUser user = newUser();
        user.changeStatus(UserStatus.ACTIVE);
        LocalDateTime deletedAt = LocalDateTime.of(2026, 10, 28, 10, 0);

        user.markDeleted(deletedAt);

        assertThat(user.getDeletedAt()).isEqualTo(deletedAt);
        assertThat(user.isActive()).isFalse();
    }

    @Test
    void aDeletedUserStaysInactiveWhenTheStatusChanges() {
        AppUser user = newUser();
        user.markDeleted(LocalDateTime.of(2026, 10, 28, 10, 0));

        user.changeStatus(UserStatus.ACTIVE);

        assertThat(user.isActive()).isFalse();
    }

    private static AppUser newUser() {
        return AppUser.createPending(new Organisation(), "Iva", "Babic", "iva@example.com", null, UserRole.USER);
    }
}

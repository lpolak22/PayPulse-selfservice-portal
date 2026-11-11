package com.example.paypulse.service.impl;

import com.example.paypulse.exception.ApiException;
import com.example.paypulse.exception.ErrorCode;
import com.example.paypulse.exception.LastAdminException;
import com.example.paypulse.model.AppUser;
import com.example.paypulse.model.Organisation;
import com.example.paypulse.model.UserRole;
import com.example.paypulse.model.UserStatus;
import com.example.paypulse.repository.AppUserRepository;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class LastAdminGuardImplTest {

    private static final long ORGANISATION_ID = 10L;
    private static final long USER_ID = 7L;

    private final AppUserRepository appUserRepository = mock(AppUserRepository.class);
    private final LastAdminGuardImpl guard = new LastAdminGuardImpl(appUserRepository);

    @Test
    void theLastActiveAdminCannotBeDeleted() {
        AppUser admin = lastActiveAdmin();

        expectLastAdmin(() -> guard.ensureCanDelete(admin));
    }

    @ParameterizedTest
    @EnumSource(value = UserStatus.class, names = "ACTIVE", mode = EnumSource.Mode.EXCLUDE)
    void theLastActiveAdminCannotLoseTheActiveStatus(UserStatus newStatus) {
        AppUser admin = lastActiveAdmin();

        expectLastAdmin(() -> guard.ensureCanChangeStatus(admin, newStatus));
    }

    @Test
    void theLastActiveAdminCannotBeDemoted() {
        AppUser admin = lastActiveAdmin();

        expectLastAdmin(() -> guard.ensureCanChangeRole(admin, UserRole.USER));
    }

    @Test
    void theErrorIs409WithCodeLastAdmin() {
        AppUser admin = lastActiveAdmin();

        assertThatThrownBy(() -> guard.ensureCanDelete(admin))
                .isInstanceOf(LastAdminException.class)
                .extracting(e -> ((ApiException) e).getErrorCode()).isEqualTo(ErrorCode.LAST_ADMIN);
        assertThat(ErrorCode.LAST_ADMIN.getStatus()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void anAdminCanBeRemovedWhileAnotherActiveAdminRemains() {
        AppUser admin = user(UserRole.ADMIN, UserStatus.ACTIVE);
        when(appUserRepository.existsOtherActiveUserWithRole(ORGANISATION_ID, UserRole.ADMIN, USER_ID))
                .thenReturn(true);

        assertThatCode(() -> guard.ensureCanDelete(admin)).doesNotThrowAnyException();
        assertThatCode(() -> guard.ensureCanChangeStatus(admin, UserStatus.INACTIVE)).doesNotThrowAnyException();
        assertThatCode(() -> guard.ensureCanChangeRole(admin, UserRole.USER)).doesNotThrowAnyException();
    }

    @Test
    void onlyActiveAdminsOfTheSameOrganisationCountAsRemaining() {
        AppUser admin = user(UserRole.ADMIN, UserStatus.ACTIVE);
        when(appUserRepository.existsOtherActiveUserWithRole(anyLong(), any(), anyLong())).thenReturn(true);

        guard.ensureCanDelete(admin);

        verify(appUserRepository).existsOtherActiveUserWithRole(ORGANISATION_ID, UserRole.ADMIN, USER_ID);
    }

    @Test
    void keepingTheStatusActiveNeedsNoCheck() {
        guard.ensureCanChangeStatus(lastActiveAdmin(), UserStatus.ACTIVE);

        verifyNoInteractions(appUserRepository);
    }

    @Test
    void keepingTheRoleAdminNeedsNoCheck() {
        guard.ensureCanChangeRole(lastActiveAdmin(), UserRole.ADMIN);

        verifyNoInteractions(appUserRepository);
    }

    @Test
    void aUserRoleIsNeverGuarded() {
        AppUser user = user(UserRole.USER, UserStatus.ACTIVE);

        guard.ensureCanDelete(user);
        guard.ensureCanChangeStatus(user, UserStatus.INACTIVE);
        guard.ensureCanChangeRole(user, UserRole.USER);

        verifyNoInteractions(appUserRepository);
    }

    @ParameterizedTest
    @EnumSource(value = UserStatus.class, names = "ACTIVE", mode = EnumSource.Mode.EXCLUDE)
    void anAdminWhoIsNotActiveIsNeverGuarded(UserStatus status) {
        AppUser admin = user(UserRole.ADMIN, status);

        guard.ensureCanDelete(admin);
        guard.ensureCanChangeRole(admin, UserRole.USER);

        verifyNoInteractions(appUserRepository);
    }

    @Test
    void aDeletedAdminIsNeverGuarded() {
        AppUser admin = user(UserRole.ADMIN, UserStatus.ACTIVE);
        admin.markDeleted(LocalDateTime.of(2026, 10, 28, 10, 0));

        guard.ensureCanChangeRole(admin, UserRole.USER);

        verifyNoInteractions(appUserRepository);
    }

    private AppUser lastActiveAdmin() {
        when(appUserRepository.existsOtherActiveUserWithRole(ORGANISATION_ID, UserRole.ADMIN, USER_ID))
                .thenReturn(false);
        return user(UserRole.ADMIN, UserStatus.ACTIVE);
    }

    private static void expectLastAdmin(ThrowingCallable check) {
        assertThatThrownBy(check).isInstanceOf(LastAdminException.class);
    }

    private static AppUser user(UserRole role, UserStatus status) {
        Organisation organisation = new Organisation();
        organisation.setIdOrganisation(ORGANISATION_ID);
        AppUser user = AppUser.createPending(organisation, "Iva", "Babic", "iva@example.com", null, role);
        user.changeStatus(status);
        ReflectionTestUtils.setField(user, "idUser", USER_ID);
        return user;
    }
}

package com.example.paypulse.service.impl;

import com.example.paypulse.dto.AppUserResponse;
import com.example.paypulse.dto.AppUserSearchCriteria;
import com.example.paypulse.dto.CreateAppUserRequest;
import com.example.paypulse.dto.UpdateAppUserRequest;
import com.example.paypulse.dto.common.PageQuery;
import com.example.paypulse.dto.common.PageResponse;
import com.example.paypulse.exception.ApiException;
import com.example.paypulse.exception.ErrorCode;
import com.example.paypulse.exception.ResourceNotFoundException;
import com.example.paypulse.exception.UserEmailAlreadyExistsException;
import com.example.paypulse.mapper.AppUserMapper;
import com.example.paypulse.model.AppUser;
import com.example.paypulse.model.AuditAction;
import com.example.paypulse.model.Organisation;
import com.example.paypulse.model.UserRole;
import com.example.paypulse.model.UserStatus;
import com.example.paypulse.repository.AppUserRepository;
import com.example.paypulse.repository.OrganisationRepository;
import com.example.paypulse.security.CurrentUser;
import com.example.paypulse.security.CurrentUserProvider;
import com.example.paypulse.service.AuditLogService;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.util.ReflectionTestUtils;

import java.sql.SQLException;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AppUserServiceImplTest {

    private static final long ADMIN_ID = 1L;
    private static final long ORGANISATION_ID = 10L;
    private static final long USER_ID = 7L;
    private static final Instant NOW = Instant.parse("2026-10-28T09:30:00Z");

    @Mock
    private AppUserRepository appUserRepository;
    @Mock
    private OrganisationRepository organisationRepository;
    @Mock
    private AuditLogService auditLogService;
    @Mock
    private CurrentUserProvider currentUserProvider;

    private AppUserServiceImpl service;
    private Organisation organisation;

    @BeforeEach
    void setUp() {
        service = new AppUserServiceImpl(appUserRepository, organisationRepository, new AppUserMapper(),
                auditLogService, currentUserProvider, Clock.fixed(NOW, ZoneOffset.UTC));
        organisation = new Organisation();
        organisation.setIdOrganisation(ORGANISATION_ID);
        organisation.setName("PayPulse Demo d.o.o.");
        when(currentUserProvider.getCurrentUser()).thenReturn(new CurrentUser(ADMIN_ID, ORGANISATION_ID));
        when(organisationRepository.getReferenceById(ORGANISATION_ID)).thenReturn(organisation);
        when(appUserRepository.saveAndFlush(any())).thenAnswer(invocation -> withId(invocation.getArgument(0)));
    }

    @Test
    void createUserRejectsAnEmailThatIsAlreadyUsed() {
        when(appUserRepository.existsUndeletedByEmail("ana@example.com")).thenReturn(true);

        assertThatThrownBy(() -> service.createUser(createRequest("ana@example.com")))
                .isInstanceOf(UserEmailAlreadyExistsException.class)
                .extracting(e -> ((ApiException) e).getErrorCode()).isEqualTo(ErrorCode.USER_EMAIL_EXISTS);
        verify(appUserRepository, never()).saveAndFlush(any());
        verifyNoInteractions(auditLogService);
    }

    @Test
    void createUserSavesAPendingUserInTheAdminsOrganisation() {
        AppUserResponse created = service.createUser(createRequest("iva@example.com"));

        assertThat(created.id()).isEqualTo(USER_ID);
        assertThat(created.status()).isEqualTo(UserStatus.PENDING_ACTIVATION);
        assertThat(created.active()).isFalse();
        assertThat(created.organisationId()).isEqualTo(ORGANISATION_ID);
        assertThat(created.email()).isEqualTo("iva@example.com");
    }

    @Test
    void createUserWritesAnAuditEntry() {
        AppUserResponse created = service.createUser(createRequest("iva@example.com"));

        verify(auditLogService).record(eq(AuditAction.USER_CREATED), eq(USER_ID), isNull(), eq(created));
    }

    @Test
    void createUserTurnsAUniqueIndexRaceIntoEmailExists() {
        doThrow(integrityViolation(AppUser.EMAIL_UNIQUE_INDEX)).when(appUserRepository).saveAndFlush(any());

        assertThatThrownBy(() -> service.createUser(createRequest("iva@example.com")))
                .isInstanceOf(UserEmailAlreadyExistsException.class);
        verifyNoInteractions(auditLogService);
    }

    @Test
    void createUserRethrowsOtherIntegrityViolations() {
        DataIntegrityViolationException other = integrityViolation("fk_user_organisation");
        doThrow(other).when(appUserRepository).saveAndFlush(any());

        assertThatThrownBy(() -> service.createUser(createRequest("iva@example.com"))).isSameAs(other);
    }

    @Test
    void getUserThrowsNotFoundForAUserOutsideTheOrganisation() {
        when(appUserRepository.findUndeletedInOrganisation(USER_ID, ORGANISATION_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getUser(USER_ID))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(String.valueOf(USER_ID));
    }

    @Test
    void getUsersSearchesWithTheDefaultSort() {
        AppUser user = withId(pendingUser("iva@example.com"));
        when(appUserRepository.findAll(ArgumentMatchers.<Specification<AppUser>>any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(user)));

        PageResponse<AppUserResponse> page =
                service.getUsers(new AppUserSearchCriteria(null, null, null), new PageQuery(0, 5));

        ArgumentCaptor<Pageable> pageable = ArgumentCaptor.forClass(Pageable.class);
        verify(appUserRepository).findAll(ArgumentMatchers.<Specification<AppUser>>any(), pageable.capture());
        assertThat(pageable.getValue().getPageSize()).isEqualTo(5);
        assertThat(pageable.getValue().getSort()).isEqualTo(Sort.by("lastName", "firstName", "idUser"));
        assertThat(page.items()).extracting(AppUserResponse::email).containsExactly("iva@example.com");
    }

    @Test
    void updateUserChangesOnlyTheFieldsThatArePresent() {
        AppUser user = existingUser(UserStatus.ACTIVE);

        AppUserResponse updated = service.updateUser(USER_ID, updateRequest(null, "+385911111111", null, null));

        assertThat(updated.phone()).isEqualTo("+385911111111");
        assertThat(updated.firstName()).isEqualTo("Iva");
        assertThat(updated.email()).isEqualTo(user.getEmail());
        assertThat(updated.role()).isEqualTo(UserRole.USER);
        assertThat(updated.status()).isEqualTo(UserStatus.ACTIVE);
    }

    @Test
    void updateUserRejectsAnEmailUsedByAnotherUser() {
        existingUser(UserStatus.ACTIVE);
        when(appUserRepository.existsUndeletedByEmailExcludingUser("taken@example.com", USER_ID)).thenReturn(true);

        assertThatThrownBy(() -> service.updateUser(USER_ID, updateRequest("taken@example.com", null, null, null)))
                .isInstanceOf(UserEmailAlreadyExistsException.class);
        verify(appUserRepository, never()).saveAndFlush(any());
    }

    @Test
    void updateUserChecksEmailUniquenessExcludingTheUserItself() {
        existingUser(UserStatus.ACTIVE);

        AppUserResponse updated = service.updateUser(USER_ID, updateRequest("IVA@example.com", null, null, null));

        verify(appUserRepository).existsUndeletedByEmailExcludingUser("IVA@example.com", USER_ID);
        assertThat(updated.email()).isEqualTo("IVA@example.com");
    }

    @Test
    void updateUserDoesNotCheckEmailUniquenessWhenEmailIsAbsent() {
        existingUser(UserStatus.ACTIVE);

        service.updateUser(USER_ID, updateRequest(null, null, UserRole.ADMIN, null));

        verify(appUserRepository, never()).existsUndeletedByEmailExcludingUser(anyString(), anyLong());
    }

    @Test
    void updateUserRejectsChangingTheStatusBackToPendingActivation() {
        existingUser(UserStatus.ACTIVE);

        assertThatThrownBy(() -> service.updateUser(USER_ID,
                updateRequest(null, null, null, UserStatus.PENDING_ACTIVATION)))
                .isInstanceOf(ApiException.class)
                .extracting(e -> ((ApiException) e).getErrorCode()).isEqualTo(ErrorCode.VALIDATION_ERROR);
    }

    @Test
    void updateUserAllowsKeepingPendingActivation() {
        existingUser(UserStatus.PENDING_ACTIVATION);

        AppUserResponse updated = service.updateUser(USER_ID,
                updateRequest(null, null, null, UserStatus.PENDING_ACTIVATION));

        assertThat(updated.status()).isEqualTo(UserStatus.PENDING_ACTIVATION);
    }

    @Test
    void deactivatingAUserClearsTheActiveFlag() {
        existingUser(UserStatus.ACTIVE);

        AppUserResponse updated = service.updateUser(USER_ID, updateRequest(null, null, null, UserStatus.INACTIVE));

        assertThat(updated.status()).isEqualTo(UserStatus.INACTIVE);
        assertThat(updated.active()).isFalse();
    }

    @Test
    void updateUserAuditsTheValuesBeforeAndAfter() {
        existingUser(UserStatus.ACTIVE);

        service.updateUser(USER_ID, updateRequest(null, null, UserRole.ADMIN, null));

        ArgumentCaptor<AppUserResponse> before = ArgumentCaptor.forClass(AppUserResponse.class);
        ArgumentCaptor<AppUserResponse> after = ArgumentCaptor.forClass(AppUserResponse.class);
        verify(auditLogService).record(eq(AuditAction.USER_UPDATED), eq(USER_ID), before.capture(), after.capture());
        assertThat(before.getValue().role()).isEqualTo(UserRole.USER);
        assertThat(after.getValue().role()).isEqualTo(UserRole.ADMIN);
    }

    @Test
    void updateUserThrowsNotFoundForAMissingUser() {
        when(appUserRepository.findUndeletedInOrganisation(USER_ID, ORGANISATION_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.updateUser(USER_ID, updateRequest(null, null, null, null)))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void deleteUserSoftDeletesAtTheCurrentTimeAndAudits() {
        AppUser user = existingUser(UserStatus.ACTIVE);

        service.deleteUser(USER_ID);

        assertThat(user.getDeletedAt()).isEqualTo(LocalDateTime.ofInstant(NOW, ZoneOffset.UTC));
        assertThat(user.isActive()).isFalse();
        verify(auditLogService).record(eq(AuditAction.USER_DELETED), eq(USER_ID), any(AppUserResponse.class), isNull());
    }

    @Test
    void deleteUserThrowsNotFoundForAMissingUser() {
        when(appUserRepository.findUndeletedInOrganisation(USER_ID, ORGANISATION_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.deleteUser(USER_ID)).isInstanceOf(ResourceNotFoundException.class);
        verifyNoInteractions(auditLogService);
    }

    private AppUser existingUser(UserStatus status) {
        AppUser user = withId(pendingUser("iva@example.com"));
        user.changeStatus(status);
        when(appUserRepository.findUndeletedInOrganisation(USER_ID, ORGANISATION_ID)).thenReturn(Optional.of(user));
        return user;
    }

    private AppUser pendingUser(String email) {
        return AppUser.createPending(organisation, "Iva", "Babic", email, null, UserRole.USER);
    }

    private static AppUser withId(AppUser user) {
        ReflectionTestUtils.setField(user, "idUser", USER_ID);
        return user;
    }

    private static CreateAppUserRequest createRequest(String email) {
        return new CreateAppUserRequest("Iva", "Babic", email, null, UserRole.USER);
    }

    private static UpdateAppUserRequest updateRequest(String email, String phone, UserRole role, UserStatus status) {
        return new UpdateAppUserRequest(null, null, email, phone, role, status);
    }

    private static DataIntegrityViolationException integrityViolation(String constraintName) {
        ConstraintViolationException cause =
                new ConstraintViolationException("violation", new SQLException("violation"), constraintName);
        return new DataIntegrityViolationException("could not execute statement", cause);
    }
}

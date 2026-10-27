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
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AppUserServiceImplTest {

    private static final Long ORGANISATION_ID = 7L;
    private static final Long ADMIN_ID = 1L;
    private static final Long USER_ID = 42L;
    private static final Instant NOW = Instant.parse("2026-10-27T10:15:30Z");

    @Mock
    private AppUserRepository appUserRepository;

    @Mock
    private OrganisationRepository organisationRepository;

    @Mock
    private AuditLogService auditLogService;

    @Mock
    private CurrentUserProvider currentUserProvider;

    private final Clock clock = Clock.fixed(NOW, ZoneOffset.UTC);
    private final AppUserMapper appUserMapper = new AppUserMapper();
    private Organisation organisation;
    private AppUserServiceImpl service;

    @BeforeEach
    void setUp() {
        organisation = new Organisation();
        organisation.setIdOrganisation(ORGANISATION_ID);
        organisation.setName("PayPulse Demo d.o.o.");
        service = new AppUserServiceImpl(appUserRepository, organisationRepository, appUserMapper,
                auditLogService, currentUserProvider, clock);
        lenient().when(currentUserProvider.getCurrentUser()).thenReturn(new CurrentUser(ADMIN_ID, ORGANISATION_ID));
    }

    @Nested
    class CreateUser {

        private final CreateAppUserRequest request = new CreateAppUserRequest(
                " Luka ", "Marić", "luka.maric@example.com", "+385915550101", UserRole.USER);

        @Test
        void savesPendingInactiveUserInCurrentOrganisationAndAuditsIt() {
            when(appUserRepository.existsUndeletedByEmail("luka.maric@example.com")).thenReturn(false);
            when(organisationRepository.getReferenceById(ORGANISATION_ID)).thenReturn(organisation);
            when(appUserRepository.saveAndFlush(any(AppUser.class)))
                    .thenAnswer(invocation -> withId(invocation.getArgument(0), USER_ID));

            AppUserResponse created = service.createUser(request);

            assertThat(created.id()).isEqualTo(USER_ID);
            assertThat(created.firstName()).isEqualTo("Luka");
            assertThat(created.status()).isEqualTo(UserStatus.PENDING_ACTIVATION);
            assertThat(created.active()).isFalse();
            assertThat(created.organisationId()).isEqualTo(ORGANISATION_ID);
            verify(auditLogService).record(AuditAction.USER_CREATED, USER_ID, null, created);
        }

        @Test
        void rejectsEmailThatIsAlreadyUsed() {
            when(appUserRepository.existsUndeletedByEmail("luka.maric@example.com")).thenReturn(true);

            assertThatThrownBy(() -> service.createUser(request))
                    .isInstanceOfSatisfying(UserEmailAlreadyExistsException.class, exception ->
                            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.USER_EMAIL_EXISTS));

            verify(appUserRepository, never()).saveAndFlush(any());
            verifyNoInteractions(auditLogService);
        }

        @Test
        void treatsUniqueIndexViolationAsEmailConflict() {
            when(appUserRepository.existsUndeletedByEmail(anyString())).thenReturn(false);
            when(organisationRepository.getReferenceById(ORGANISATION_ID)).thenReturn(organisation);
            when(appUserRepository.saveAndFlush(any(AppUser.class))).thenThrow(emailUniqueViolation());

            assertThatThrownBy(() -> service.createUser(request))
                    .isInstanceOf(UserEmailAlreadyExistsException.class);

            verifyNoInteractions(auditLogService);
        }
    }

    @Nested
    class GetUsers {

        @Test
        void returnsRequestedPageSortedByName() {
            AppUser user = existingUser(UserStatus.ACTIVE);
            ArgumentCaptor<Pageable> pageable = ArgumentCaptor.forClass(Pageable.class);
            when(appUserRepository.findAll(ArgumentMatchers.<Specification<AppUser>>any(), pageable.capture()))
                    .thenAnswer(invocation -> new PageImpl<>(List.of(user), invocation.getArgument(1), 11));

            PageResponse<AppUserResponse> page = service.getUsers(
                    new AppUserSearchCriteria("bab", UserRole.USER, UserStatus.ACTIVE), new PageQuery(1, 10));

            assertThat(pageable.getValue().getPageNumber()).isEqualTo(1);
            assertThat(pageable.getValue().getPageSize()).isEqualTo(10);
            assertThat(pageable.getValue().getSort()).isEqualTo(Sort.by("lastName", "firstName", "idUser"));
            assertThat(page.items()).extracting(AppUserResponse::id).containsExactly(USER_ID);
            assertThat(page.page()).isEqualTo(1);
            assertThat(page.pageSize()).isEqualTo(10);
            assertThat(page.total()).isEqualTo(11);
        }
    }

    @Nested
    class GetUser {

        @Test
        void returnsUserOfCurrentOrganisation() {
            givenExistingUser(existingUser(UserStatus.ACTIVE));

            AppUserResponse user = service.getUser(USER_ID);

            assertThat(user.id()).isEqualTo(USER_ID);
            assertThat(user.email()).isEqualTo("ivana.babic@example.com");
        }

        @Test
        void rejectsUnknownOrDeletedUser() {
            when(appUserRepository.findUndeletedInOrganisation(USER_ID, ORGANISATION_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.getUser(USER_ID)).isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    class UpdateUser {

        @Test
        void changesOnlyProvidedFieldsAndAuditsBeforeAndAfter() {
            AppUser user = givenExistingUser(existingUser(UserStatus.ACTIVE));
            givenSaveReturnsUser();
            UpdateAppUserRequest request = new UpdateAppUserRequest(null, "Horvat", null, null, UserRole.ADMIN, null);

            AppUserResponse updated = service.updateUser(USER_ID, request);

            assertThat(updated.firstName()).isEqualTo("Ivana");
            assertThat(updated.lastName()).isEqualTo("Horvat");
            assertThat(updated.email()).isEqualTo("ivana.babic@example.com");
            assertThat(updated.role()).isEqualTo(UserRole.ADMIN);
            assertThat(user.getStatus()).isEqualTo(UserStatus.ACTIVE);
            verify(appUserRepository, never()).existsUndeletedByEmailExcludingUser(anyString(), anyLong());

            ArgumentCaptor<AppUserResponse> before = ArgumentCaptor.forClass(AppUserResponse.class);
            verify(auditLogService).record(eq(AuditAction.USER_UPDATED), eq(USER_ID), before.capture(), eq(updated));
            assertThat(before.getValue().lastName()).isEqualTo("Babić");
        }

        @Test
        void changesEmailWhenNoOtherUserHasIt() {
            givenExistingUser(existingUser(UserStatus.ACTIVE));
            givenSaveReturnsUser();
            when(appUserRepository.existsUndeletedByEmailExcludingUser("ivana.horvat@example.com", USER_ID))
                    .thenReturn(false);

            AppUserResponse updated = service.updateUser(USER_ID, emailChange("ivana.horvat@example.com"));

            assertThat(updated.email()).isEqualTo("ivana.horvat@example.com");
        }

        @Test
        void rejectsEmailUsedByAnotherUser() {
            givenExistingUser(existingUser(UserStatus.ACTIVE));
            when(appUserRepository.existsUndeletedByEmailExcludingUser("ana.horvat@example.com", USER_ID))
                    .thenReturn(true);

            assertThatThrownBy(() -> service.updateUser(USER_ID, emailChange("ana.horvat@example.com")))
                    .isInstanceOf(UserEmailAlreadyExistsException.class);

            verify(appUserRepository, never()).saveAndFlush(any());
            verifyNoInteractions(auditLogService);
        }

        @ParameterizedTest
        @CsvSource({
                "PENDING_ACTIVATION, ACTIVE, true",
                "ACTIVE, INACTIVE, false",
                "INACTIVE, ACTIVE, true"
        })
        void keepsActiveFlagInSyncWithStatus(UserStatus from, UserStatus to, boolean expectedActive) {
            AppUser user = givenExistingUser(existingUser(from));
            givenSaveReturnsUser();

            AppUserResponse updated = service.updateUser(USER_ID, statusChange(to));

            assertThat(updated.status()).isEqualTo(to);
            assertThat(updated.active()).isEqualTo(expectedActive);
            assertThat(user.isActive()).isEqualTo(expectedActive);
        }

        @Test
        void rejectsMovingUserBackToPendingActivation() {
            givenExistingUser(existingUser(UserStatus.ACTIVE));

            assertThatThrownBy(() -> service.updateUser(USER_ID, statusChange(UserStatus.PENDING_ACTIVATION)))
                    .isInstanceOfSatisfying(ApiException.class, exception ->
                            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.VALIDATION_ERROR));

            verifyNoInteractions(auditLogService);
        }

        @Test
        void rejectsUnknownOrDeletedUser() {
            when(appUserRepository.findUndeletedInOrganisation(USER_ID, ORGANISATION_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.updateUser(USER_ID, statusChange(UserStatus.INACTIVE)))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    class DeleteUser {

        @Test
        void softDeletesUserAndAuditsPreviousState() {
            AppUser user = givenExistingUser(existingUser(UserStatus.ACTIVE));

            service.deleteUser(USER_ID);

            assertThat(user.getDeletedAt()).isEqualTo(LocalDateTime.ofInstant(NOW, ZoneOffset.UTC));
            assertThat(user.isActive()).isFalse();
            assertThat(user.getStatus()).isEqualTo(UserStatus.ACTIVE);
            verify(appUserRepository, never()).delete(any(AppUser.class));

            ArgumentCaptor<AppUserResponse> before = ArgumentCaptor.forClass(AppUserResponse.class);
            verify(auditLogService).record(eq(AuditAction.USER_DELETED), eq(USER_ID), before.capture(), isNull());
            assertThat(before.getValue().active()).isTrue();
        }

        @Test
        void rejectsUnknownOrAlreadyDeletedUser() {
            when(appUserRepository.findUndeletedInOrganisation(USER_ID, ORGANISATION_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.deleteUser(USER_ID)).isInstanceOf(ResourceNotFoundException.class);

            verifyNoInteractions(auditLogService);
        }
    }

    private AppUser existingUser(UserStatus status) {
        AppUser user = AppUser.createPending(organisation, "Ivana", "Babić", "ivana.babic@example.com",
                "+385981234000", UserRole.USER);
        user.changeStatus(status);
        return withId(user, USER_ID);
    }

    private AppUser givenExistingUser(AppUser user) {
        when(appUserRepository.findUndeletedInOrganisation(user.getIdUser(), ORGANISATION_ID))
                .thenReturn(Optional.of(user));
        return user;
    }

    private void givenSaveReturnsUser() {
        when(appUserRepository.saveAndFlush(any(AppUser.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    private static AppUser withId(AppUser user, Long id) {
        ReflectionTestUtils.setField(user, "idUser", id);
        return user;
    }

    private static UpdateAppUserRequest emailChange(String email) {
        return new UpdateAppUserRequest(null, null, email, null, null, null);
    }

    private static UpdateAppUserRequest statusChange(UserStatus status) {
        return new UpdateAppUserRequest(null, null, null, null, null, status);
    }

    private static DataIntegrityViolationException emailUniqueViolation() {
        return new DataIntegrityViolationException("duplicate email", new ConstraintViolationException(
                "duplicate key", new SQLException("duplicate key"), AppUser.EMAIL_UNIQUE_INDEX));
    }
}

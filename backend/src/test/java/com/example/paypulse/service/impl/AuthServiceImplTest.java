package com.example.paypulse.service.impl;

import com.example.paypulse.dto.AppUserResponse;
import com.example.paypulse.dto.auth.LoginRequest;
import com.example.paypulse.dto.auth.TokenResponse;
import com.example.paypulse.exception.ApiException;
import com.example.paypulse.exception.AuthException;
import com.example.paypulse.exception.ErrorCode;
import com.example.paypulse.exception.ResourceNotFoundException;
import com.example.paypulse.mapper.AppUserMapper;
import com.example.paypulse.model.AppUser;
import com.example.paypulse.model.AuditAction;
import com.example.paypulse.model.Organisation;
import com.example.paypulse.model.UserRole;
import com.example.paypulse.model.UserStatus;
import com.example.paypulse.repository.AppUserRepository;
import com.example.paypulse.security.CurrentUser;
import com.example.paypulse.security.CurrentUserProvider;
import com.example.paypulse.service.AccessTokenService;
import com.example.paypulse.service.AuditLogService;
import com.example.paypulse.service.RefreshTokenService;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AuthServiceImplTest {

    private static final long USER_ID = 7L;
    private static final long ORGANISATION_ID = 10L;
    private static final String EMAIL = "iva@example.com";
    private static final String PASSWORD = "Correct-Password-1";
    private static final BCryptPasswordEncoder PASSWORD_ENCODER = new BCryptPasswordEncoder(4);
    private static final String PASSWORD_HASH = PASSWORD_ENCODER.encode(PASSWORD);

    @Mock
    private AppUserRepository appUserRepository;
    @Mock
    private AccessTokenService accessTokenService;
    @Mock
    private RefreshTokenService refreshTokenService;
    @Mock
    private AuditLogService auditLogService;
    @Mock
    private CurrentUserProvider currentUserProvider;

    private AuthServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new AuthServiceImpl(appUserRepository, PASSWORD_ENCODER, accessTokenService, refreshTokenService,
                auditLogService, currentUserProvider, new AppUserMapper());
        when(accessTokenService.issue(any())).thenReturn("access-token");
        when(accessTokenService.getExpiresInSeconds()).thenReturn(900L);
        when(refreshTokenService.issue(any())).thenReturn("refresh-token");
    }

    @Test
    void loginWithValidCredentialsIssuesBothTokens() {
        existingUser(UserStatus.ACTIVE, PASSWORD_HASH);

        TokenResponse tokens = service.login(new LoginRequest(EMAIL, PASSWORD));

        assertThat(tokens).isEqualTo(new TokenResponse("access-token", "refresh-token", "Bearer", 900L));
    }

    @Test
    void loginLooksTheEmailUpWithoutSurroundingSpaces() {
        existingUser(UserStatus.ACTIVE, PASSWORD_HASH);

        service.login(new LoginRequest("  " + EMAIL + " ", PASSWORD));

        verify(appUserRepository).findUndeletedByEmail(EMAIL);
    }

    @Test
    void successfulLoginIsAudited() {
        existingUser(UserStatus.ACTIVE, PASSWORD_HASH);

        service.login(new LoginRequest(EMAIL, PASSWORD));

        verify(auditLogService).record(USER_ID, AuditAction.LOGIN_SUCCESS, USER_ID, null, null);
    }

    @Test
    void unknownEmailIsRejectedAndAuditedWithoutAUser() {
        when(appUserRepository.findUndeletedByEmail("ghost@example.com")).thenReturn(Optional.empty());

        expectAuthError(() -> service.login(new LoginRequest("ghost@example.com", PASSWORD)),
                ErrorCode.INVALID_CREDENTIALS);
        verify(auditLogService).record(isNull(), eq(AuditAction.LOGIN_FAILED), isNull(), isNull(),
                eq(Map.of("email", "ghost@example.com", "reason", "INVALID_CREDENTIALS")));
    }

    @Test
    void wrongPasswordIsRejectedAndAudited() {
        existingUser(UserStatus.ACTIVE, PASSWORD_HASH);

        expectAuthError(() -> service.login(new LoginRequest(EMAIL, "wrong")), ErrorCode.INVALID_CREDENTIALS);
        verify(auditLogService).record(eq(USER_ID), eq(AuditAction.LOGIN_FAILED), eq(USER_ID), isNull(),
                eq(Map.of("email", EMAIL, "reason", "INVALID_CREDENTIALS")));
        verify(refreshTokenService, never()).issue(any());
    }

    @Test
    void userWithoutPasswordCannotLogIn() {
        existingUser(UserStatus.PENDING_ACTIVATION, null);

        expectAuthError(() -> service.login(new LoginRequest(EMAIL, PASSWORD)), ErrorCode.INVALID_CREDENTIALS);
    }

    @Test
    void pendingUserWithTheRightPasswordIsToldToActivate() {
        existingUser(UserStatus.PENDING_ACTIVATION, PASSWORD_HASH);

        expectAuthError(() -> service.login(new LoginRequest(EMAIL, PASSWORD)),
                ErrorCode.ACCOUNT_PENDING_ACTIVATION);
    }

    @Test
    void deactivatedUserWithTheRightPasswordIsToldTheAccountIsInactive() {
        existingUser(UserStatus.INACTIVE, PASSWORD_HASH);

        expectAuthError(() -> service.login(new LoginRequest(EMAIL, PASSWORD)), ErrorCode.ACCOUNT_INACTIVE);
        verify(accessTokenService, never()).issue(any());
    }

    @Test
    void deactivatedUserWithAWrongPasswordLearnsNothingAboutTheStatus() {
        existingUser(UserStatus.INACTIVE, PASSWORD_HASH);

        expectAuthError(() -> service.login(new LoginRequest(EMAIL, "wrong")), ErrorCode.INVALID_CREDENTIALS);
    }

    @Test
    void refreshIssuesNewTokensForAnActiveUser() {
        when(refreshTokenService.rotate("old-refresh")).thenReturn(user(UserStatus.ACTIVE, PASSWORD_HASH));

        TokenResponse tokens = service.refresh("old-refresh");

        assertThat(tokens.accessToken()).isEqualTo("access-token");
        assertThat(tokens.refreshToken()).isEqualTo("refresh-token");
    }

    @Test
    void refreshForADeactivatedUserRevokesAllTheirTokens() {
        when(refreshTokenService.rotate("old-refresh")).thenReturn(user(UserStatus.INACTIVE, PASSWORD_HASH));

        expectAuthError(() -> service.refresh("old-refresh"), ErrorCode.ACCOUNT_INACTIVE);
        verify(refreshTokenService).revokeAllForUser(USER_ID);
        verify(accessTokenService, never()).issue(any());
    }

    @Test
    void refreshForADeletedUserRevokesAllTheirTokens() {
        AppUser deleted = user(UserStatus.ACTIVE, PASSWORD_HASH);
        deleted.markDeleted(LocalDateTime.of(2026, 10, 28, 10, 0));
        when(refreshTokenService.rotate("old-refresh")).thenReturn(deleted);

        expectAuthError(() -> service.refresh("old-refresh"), ErrorCode.ACCOUNT_INACTIVE);
        verify(refreshTokenService).revokeAllForUser(USER_ID);
    }

    @Test
    void refreshWithAnInvalidTokenIssuesNothing() {
        when(refreshTokenService.rotate("bad")).thenThrow(new AuthException(ErrorCode.INVALID_REFRESH_TOKEN));

        expectAuthError(() -> service.refresh("bad"), ErrorCode.INVALID_REFRESH_TOKEN);
        verify(accessTokenService, never()).issue(any());
        verify(refreshTokenService, never()).issue(any());
    }

    @Test
    void logoutRevokesTheRefreshToken() {
        service.logout("refresh-token");

        verify(refreshTokenService).revoke("refresh-token");
    }

    @Test
    void currentUserIsLoadedWithinTheirOrganisation() {
        when(currentUserProvider.getCurrentUser()).thenReturn(new CurrentUser(USER_ID, ORGANISATION_ID));
        when(appUserRepository.findUndeletedInOrganisation(USER_ID, ORGANISATION_ID))
                .thenReturn(Optional.of(user(UserStatus.ACTIVE, PASSWORD_HASH)));

        AppUserResponse me = service.getCurrentUser();

        assertThat(me.id()).isEqualTo(USER_ID);
        assertThat(me.email()).isEqualTo(EMAIL);
    }

    @Test
    void currentUserThatNoLongerExistsIsNotFound() {
        when(currentUserProvider.getCurrentUser()).thenReturn(new CurrentUser(USER_ID, ORGANISATION_ID));
        when(appUserRepository.findUndeletedInOrganisation(USER_ID, ORGANISATION_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(service::getCurrentUser).isInstanceOf(ResourceNotFoundException.class);
    }

    private void existingUser(UserStatus status, String passwordHash) {
        when(appUserRepository.findUndeletedByEmail(EMAIL)).thenReturn(Optional.of(user(status, passwordHash)));
    }

    private static AppUser user(UserStatus status, String passwordHash) {
        Organisation organisation = new Organisation();
        organisation.setIdOrganisation(ORGANISATION_ID);
        AppUser user = AppUser.createPending(organisation, "Iva", "Babic", EMAIL, null, UserRole.USER);
        user.changeStatus(status);
        ReflectionTestUtils.setField(user, "idUser", USER_ID);
        ReflectionTestUtils.setField(user, "passwordHash", passwordHash);
        return user;
    }

    private static void expectAuthError(ThrowingCallable call, ErrorCode expected) {
        assertThatThrownBy(call)
                .isInstanceOf(AuthException.class)
                .extracting(e -> ((ApiException) e).getErrorCode()).isEqualTo(expected);
    }
}

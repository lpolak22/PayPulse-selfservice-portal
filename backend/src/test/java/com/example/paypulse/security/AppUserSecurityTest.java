package com.example.paypulse.security;

import com.example.paypulse.model.AppUser;
import com.example.paypulse.model.Organisation;
import com.example.paypulse.model.UserRole;
import com.example.paypulse.model.UserStatus;
import com.example.paypulse.repository.AppUserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AppUserSecurityTest {

    private static final long ORGANISATION_ID = 10L;

    private final AppUserRepository appUserRepository = mock(AppUserRepository.class);
    private final AppUserDetailsService userDetailsService = new AppUserDetailsService(appUserRepository);
    private final SecurityContextCurrentUserProvider currentUserProvider = new SecurityContextCurrentUserProvider();

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void principalCarriesTheUsersIdOrganisationAndRole() {
        AppUserPrincipal principal = AppUserPrincipal.from(user(UserRole.ADMIN, UserStatus.ACTIVE));

        assertThat(principal.userId()).isEqualTo(7L);
        assertThat(principal.organisationId()).isEqualTo(ORGANISATION_ID);
        assertThat(principal.getUsername()).isEqualTo("iva@example.com");
        assertThat(principal.getAuthorities()).extracting(GrantedAuthority::getAuthority).containsExactly("ROLE_ADMIN");
    }

    @Test
    void userRoleMapsToRoleUser() {
        AppUserPrincipal principal = AppUserPrincipal.from(user(UserRole.USER, UserStatus.ACTIVE));

        assertThat(principal.getAuthorities()).extracting(GrantedAuthority::getAuthority).containsExactly("ROLE_USER");
    }

    @Test
    void activeUserIsEnabled() {
        assertThat(AppUserPrincipal.from(user(UserRole.ADMIN, UserStatus.ACTIVE)).isEnabled()).isTrue();
    }

    @Test
    void deactivatedUserIsDisabled() {
        assertThat(AppUserPrincipal.from(user(UserRole.ADMIN, UserStatus.INACTIVE)).isEnabled()).isFalse();
    }

    @Test
    void pendingUserIsDisabled() {
        assertThat(AppUserPrincipal.from(user(UserRole.USER, UserStatus.PENDING_ACTIVATION)).isEnabled()).isFalse();
    }

    @Test
    void userDetailsAreLoadedByEmail() {
        when(appUserRepository.findUndeletedByEmail("iva@example.com"))
                .thenReturn(Optional.of(user(UserRole.USER, UserStatus.ACTIVE)));

        UserDetails details = userDetailsService.loadUserByUsername("iva@example.com");

        assertThat(details.getUsername()).isEqualTo("iva@example.com");
        assertThat(details.isEnabled()).isTrue();
    }

    @Test
    void unknownOrDeletedEmailIsNotFound() {
        when(appUserRepository.findUndeletedByEmail("gone@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userDetailsService.loadUserByUsername("gone@example.com"))
                .isInstanceOf(UsernameNotFoundException.class);
    }

    @Test
    void currentUserComesFromTheSignedInPrincipal() {
        AppUserPrincipal principal = AppUserPrincipal.from(user(UserRole.ADMIN, UserStatus.ACTIVE));
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));

        assertThat(currentUserProvider.getCurrentUser()).isEqualTo(new CurrentUser(7L, ORGANISATION_ID));
    }

    @Test
    void currentUserFailsWhenNobodyIsSignedIn() {
        assertThatThrownBy(currentUserProvider::getCurrentUser)
                .isInstanceOf(AuthenticationCredentialsNotFoundException.class);
    }

    @Test
    void currentUserFailsForAForeignPrincipal() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("someone", null, List.of()));

        assertThatThrownBy(currentUserProvider::getCurrentUser)
                .isInstanceOf(AuthenticationCredentialsNotFoundException.class);
    }

    @Test
    void deletedUserIsDisabledEvenIfStatusIsActive() {
        AppUser user = user(UserRole.ADMIN, UserStatus.ACTIVE);
        user.markDeleted(LocalDateTime.of(2026, 10, 28, 10, 0));

        assertThat(AppUserPrincipal.from(user).isEnabled()).isFalse();
    }

    private static AppUser user(UserRole role, UserStatus status) {
        Organisation organisation = new Organisation();
        organisation.setIdOrganisation(ORGANISATION_ID);
        AppUser user = AppUser.createPending(organisation, "Iva", "Babic", "iva@example.com", null, role);
        user.changeStatus(status);
        ReflectionTestUtils.setField(user, "idUser", 7L);
        return user;
    }
}

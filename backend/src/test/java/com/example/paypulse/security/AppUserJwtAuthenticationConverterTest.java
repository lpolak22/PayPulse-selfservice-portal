package com.example.paypulse.security;

import com.example.paypulse.model.AppUser;
import com.example.paypulse.model.Organisation;
import com.example.paypulse.model.UserRole;
import com.example.paypulse.model.UserStatus;
import com.example.paypulse.repository.AppUserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Every request re-reads the user behind the token, so deactivation, deletion and role changes apply at once.
 */
class AppUserJwtAuthenticationConverterTest {

    private static final long USER_ID = 7L;

    private final AppUserRepository repository = mock(AppUserRepository.class);
    private final AppUserJwtAuthenticationConverter converter = new AppUserJwtAuthenticationConverter(repository);

    @Test
    void activeUserIsAuthenticatedWithTheirPrincipal() {
        Jwt jwt = jwt("ADMIN");
        stored(user(UserRole.ADMIN, UserStatus.ACTIVE));

        AbstractAuthenticationToken authentication = converter.convert(jwt);

        assertThat(authentication.isAuthenticated()).isTrue();
        assertThat(authentication.getCredentials()).isSameAs(jwt);
        assertThat(((AppUserPrincipal) authentication.getPrincipal()).userId()).isEqualTo(USER_ID);
        assertThat(((AppUserPrincipal) authentication.getPrincipal()).organisationId()).isEqualTo(10L);
    }

    @Test
    void roleComesFromTheDatabaseNotFromTheToken() {
        stored(user(UserRole.USER, UserStatus.ACTIVE));

        AbstractAuthenticationToken authentication = converter.convert(jwt("ADMIN"));

        assertThat(authentication.getAuthorities()).extracting(GrantedAuthority::getAuthority)
                .containsExactly("ROLE_USER");
    }

    @ParameterizedTest
    @EnumSource(value = UserStatus.class, names = "ACTIVE", mode = EnumSource.Mode.EXCLUDE)
    void userThatIsNotActiveIsRejected(UserStatus status) {
        stored(user(UserRole.ADMIN, status));

        assertThatThrownBy(() -> converter.convert(jwt("ADMIN"))).isInstanceOf(DisabledException.class);
    }

    @Test
    void deletedUserIsRejected() {
        AppUser user = user(UserRole.ADMIN, UserStatus.ACTIVE);
        user.markDeleted(LocalDateTime.of(2026, 10, 28, 10, 0));
        stored(user);

        assertThatThrownBy(() -> converter.convert(jwt("ADMIN"))).isInstanceOf(DisabledException.class);
    }

    @Test
    void unknownUserIsRejected() {
        when(repository.findById(USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> converter.convert(jwt("ADMIN"))).isInstanceOf(DisabledException.class);
    }

    private void stored(AppUser user) {
        when(repository.findById(USER_ID)).thenReturn(Optional.of(user));
    }

    private static Jwt jwt(String role) {
        return Jwt.withTokenValue("token").header("alg", "HS256")
                .subject(String.valueOf(USER_ID)).claim("role", role).build();
    }

    private static AppUser user(UserRole role, UserStatus status) {
        Organisation organisation = new Organisation();
        organisation.setIdOrganisation(10L);
        AppUser user = AppUser.createPending(organisation, "Ana", "Admin", "ana@example.com", null, role);
        user.changeStatus(status);
        ReflectionTestUtils.setField(user, "idUser", USER_ID);
        return user;
    }
}

package com.example.paypulse.service.impl;

import com.example.paypulse.config.JwtConfig;
import com.example.paypulse.config.JwtProperties;
import com.example.paypulse.model.AppUser;
import com.example.paypulse.model.Organisation;
import com.example.paypulse.model.UserRole;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Access tokens issued by {@link JwtAccessTokenService} and checked by the decoder from {@link JwtConfig}.
 */
class JwtAccessTokenServiceTest {

    private static final String SECRET = "unit-test-secret-that-is-long-enough-0123";
    private static final Duration TTL = Duration.ofMinutes(15);

    private final JwtConfig config = new JwtConfig();
    private final JwtProperties properties = properties(SECRET, "paypulse");
    private final JwtDecoder decoder = config.jwtDecoder(properties);

    @Test
    void tokenCarriesTheUserIdRoleOrganisationAndIssuer() {
        Jwt jwt = decoder.decode(service(properties, Clock.systemUTC()).issue(user()));

        assertThat(jwt.getSubject()).isEqualTo("7");
        assertThat(jwt.getClaimAsString("iss")).isEqualTo("paypulse");
        assertThat(jwt.getClaimAsString(JwtAccessTokenService.ROLE_CLAIM)).isEqualTo("ADMIN");
        assertThat(((Number) jwt.getClaim(JwtAccessTokenService.ORGANISATION_CLAIM)).longValue()).isEqualTo(10L);
        assertThat(jwt.getHeaders()).containsEntry("alg", "HS256");
    }

    @Test
    void tokenIsValidForTheConfiguredTtl() {
        Jwt jwt = decoder.decode(service(properties, Clock.systemUTC()).issue(user()));

        assertThat(Duration.between(jwt.getIssuedAt(), jwt.getExpiresAt())).isEqualTo(TTL);
        assertThat(service(properties, Clock.systemUTC()).getExpiresInSeconds()).isEqualTo(900);
    }

    @Test
    void expiredTokenIsRejected() {
        Clock anHourAgo = Clock.fixed(Instant.now().minus(Duration.ofHours(1)), ZoneOffset.UTC);
        String token = service(properties, anHourAgo).issue(user());

        assertThatThrownBy(() -> decoder.decode(token)).isInstanceOf(JwtException.class).hasMessageContaining("expired");
    }

    @Test
    void tokenSignedWithAnotherSecretIsRejected() {
        JwtProperties other = properties("another-secret-that-is-also-long-enough-99", "paypulse");
        String token = new JwtAccessTokenService(config.jwtEncoder(other), other, Clock.systemUTC()).issue(user());

        assertThatThrownBy(() -> decoder.decode(token)).isInstanceOf(JwtException.class);
    }

    @Test
    void tokenFromAnotherIssuerIsRejected() {
        JwtProperties otherIssuer = properties(SECRET, "someone-else");
        String token = service(otherIssuer, Clock.systemUTC()).issue(user());

        assertThatThrownBy(() -> decoder.decode(token)).isInstanceOf(JwtException.class);
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", "only-31-characters-long-secret!"})
    void missingOrShortSecretStopsTheApplication(String secret) {
        JwtProperties weak = properties(secret, "paypulse");

        assertThatThrownBy(() -> config.jwtEncoder(weak)).isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("JWT_SECRET");
        assertThatThrownBy(() -> config.jwtDecoder(weak)).isInstanceOf(IllegalStateException.class);
    }

    private JwtAccessTokenService service(JwtProperties jwtProperties, Clock clock) {
        return new JwtAccessTokenService(config.jwtEncoder(jwtProperties), jwtProperties, clock);
    }

    private static JwtProperties properties(String secret, String issuer) {
        return new JwtProperties(secret, issuer, TTL, Duration.ofMinutes(30));
    }

    private static AppUser user() {
        Organisation organisation = new Organisation();
        organisation.setIdOrganisation(10L);
        AppUser user = AppUser.createPending(organisation, "Ana", "Admin", "ana@example.com", null, UserRole.ADMIN);
        ReflectionTestUtils.setField(user, "idUser", 7L);
        return user;
    }
}

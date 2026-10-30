package com.example.paypulse.service.impl;

import com.example.paypulse.config.ActivationProperties;
import com.example.paypulse.config.JwtConfig;
import com.example.paypulse.config.JwtProperties;
import com.example.paypulse.dto.auth.ActivationTokenStatus;
import com.example.paypulse.exception.ApiException;
import com.example.paypulse.exception.ErrorCode;
import com.example.paypulse.model.AppUser;
import com.example.paypulse.model.UserStatus;
import com.example.paypulse.repository.AppUserRepository;
import com.example.paypulse.service.ActivationTokenService;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.JwtIssuerValidator;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Service
public class JwtActivationTokenService implements ActivationTokenService {

    private static final String ISSUER_SUFFIX = "-activation";
    private static final String TYPE_CLAIM = "type";
    private static final String TYPE_ACTIVATION = "ACTIVATION";
    private static final String UPDATED_AT_CLAIM = "updatedAt";

    private final JwtEncoder jwtEncoder;
    private final NimbusJwtDecoder activationDecoder;
    private final AppUserRepository appUserRepository;
    private final ActivationProperties activationProperties;
    private final String issuer;
    private final Clock clock;

    public JwtActivationTokenService(JwtEncoder jwtEncoder,
                                     JwtProperties jwtProperties,
                                     ActivationProperties activationProperties,
                                     AppUserRepository appUserRepository,
                                     Clock clock) {
        this.jwtEncoder = jwtEncoder;
        this.activationProperties = activationProperties;
        this.appUserRepository = appUserRepository;
        this.clock = clock;
        this.issuer = jwtProperties.issuer() + ISSUER_SUFFIX;
        this.activationDecoder = NimbusJwtDecoder.withSecretKey(JwtConfig.secretKey(jwtProperties))
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
        this.activationDecoder.setJwtValidator(new JwtIssuerValidator(issuer));
    }

    @Override
    public String issue(AppUser user) {
        Instant now = clock.instant().truncatedTo(ChronoUnit.SECONDS);
        LocalDateTime updatedAt = appUserRepository.findUpdatedAt(user.getIdUser()).orElseThrow();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(issuer)
                .subject(String.valueOf(user.getIdUser()))
                .issuedAt(now)
                .expiresAt(now.plus(activationProperties.tokenTtl()))
                .claim(TYPE_CLAIM, TYPE_ACTIVATION)
                .claim(UPDATED_AT_CLAIM, updatedAt.toString())
                .build();
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }

    @Override
    public ActivationTokenStatus statusOf(String token) {
        Jwt jwt = decode(token);
        AppUser user = findUser(jwt);
        if (isUsed(user, jwt)) {
            return ActivationTokenStatus.USED;
        }
        if (!jwt.getExpiresAt().isAfter(clock.instant())) {
            return ActivationTokenStatus.EXPIRED;
        }
        return ActivationTokenStatus.VALID;
    }

    @Override
    public AppUser requireValid(String token) {
        ActivationTokenStatus status = statusOf(token);
        if (status == ActivationTokenStatus.USED) {
            throw new ApiException(ErrorCode.ACTIVATION_TOKEN_USED);
        }
        if (status == ActivationTokenStatus.EXPIRED) {
            throw new ApiException(ErrorCode.ACTIVATION_TOKEN_EXPIRED);
        }
        return findUser(decode(token));
    }

    private Jwt decode(String token) {
        try {
            Jwt jwt = activationDecoder.decode(token);
            if (!TYPE_ACTIVATION.equals(jwt.getClaimAsString(TYPE_CLAIM)) || jwt.getExpiresAt() == null) {
                throw new ApiException(ErrorCode.ACTIVATION_TOKEN_INVALID);
            }
            return jwt;
        } catch (JwtException exception) {
            throw new ApiException(ErrorCode.ACTIVATION_TOKEN_INVALID);
        }
    }

    private AppUser findUser(Jwt jwt) {
        try {
            return appUserRepository.findById(Long.valueOf(jwt.getSubject()))
                    .filter(user -> user.getDeletedAt() == null)
                    .orElseThrow(() -> new ApiException(ErrorCode.ACTIVATION_TOKEN_INVALID));
        } catch (NumberFormatException exception) {
            throw new ApiException(ErrorCode.ACTIVATION_TOKEN_INVALID);
        }
    }

    private boolean isUsed(AppUser user, Jwt jwt) {
        if (!Boolean.TRUE.equals(user.getIsFirstLogin()) || user.getStatus() != UserStatus.PENDING_ACTIVATION) {
            return true;
        }
        return !String.valueOf(user.getUpdatedAt()).equals(jwt.getClaimAsString(UPDATED_AT_CLAIM));
    }
}

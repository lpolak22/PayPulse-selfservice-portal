package com.example.paypulse.service.impl;

import com.example.paypulse.config.JwtConfig;
import com.example.paypulse.config.JwtProperties;
import com.example.paypulse.config.PasswordResetProperties;
import com.example.paypulse.dto.auth.PasswordResetTokenStatus;
import com.example.paypulse.exception.ApiException;
import com.example.paypulse.exception.ErrorCode;
import com.example.paypulse.model.AppUser;
import com.example.paypulse.model.UserStatus;
import com.example.paypulse.repository.AppUserRepository;
import com.example.paypulse.service.PasswordResetTokenService;
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

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HexFormat;

@Service
public class JwtPasswordResetTokenService implements PasswordResetTokenService {

    private static final String ISSUER_SUFFIX = "-password-reset";
    private static final String TYPE_CLAIM = "type";
    private static final String TYPE_RESET = "RESET";
    private static final String PASSWORD_CLAIM = "pwd";

    private final JwtEncoder jwtEncoder;
    private final NimbusJwtDecoder resetDecoder;
    private final AppUserRepository appUserRepository;
    private final PasswordResetProperties resetProperties;
    private final String issuer;
    private final Clock clock;

    public JwtPasswordResetTokenService(JwtEncoder jwtEncoder,
                                        JwtProperties jwtProperties,
                                        PasswordResetProperties resetProperties,
                                        AppUserRepository appUserRepository,
                                        Clock clock) {
        this.jwtEncoder = jwtEncoder;
        this.resetProperties = resetProperties;
        this.appUserRepository = appUserRepository;
        this.clock = clock;
        this.issuer = jwtProperties.issuer() + ISSUER_SUFFIX;
        this.resetDecoder = NimbusJwtDecoder.withSecretKey(JwtConfig.secretKey(jwtProperties))
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
        this.resetDecoder.setJwtValidator(new JwtIssuerValidator(issuer));
    }

    @Override
    public String issue(AppUser user) {
        Instant now = clock.instant().truncatedTo(ChronoUnit.SECONDS);
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(issuer)
                .subject(String.valueOf(user.getIdUser()))
                .issuedAt(now)
                .expiresAt(now.plus(resetProperties.tokenTtl()))
                .claim(TYPE_CLAIM, TYPE_RESET)
                .claim(PASSWORD_CLAIM, fingerprint(user.getPasswordHash()))
                .build();
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }

    @Override
    public PasswordResetTokenStatus statusOf(String token) {
        Jwt jwt = decode(token);
        AppUser user = findUser(jwt);
        if (!fingerprint(user.getPasswordHash()).equals(jwt.getClaimAsString(PASSWORD_CLAIM))) {
            return PasswordResetTokenStatus.USED;
        }
        if (!jwt.getExpiresAt().isAfter(clock.instant())) {
            return PasswordResetTokenStatus.EXPIRED;
        }
        return PasswordResetTokenStatus.VALID;
    }

    @Override
    public AppUser requireValid(String token) {
        PasswordResetTokenStatus status = statusOf(token);
        if (status == PasswordResetTokenStatus.USED) {
            throw new ApiException(ErrorCode.PASSWORD_RESET_TOKEN_USED);
        }
        if (status == PasswordResetTokenStatus.EXPIRED) {
            throw new ApiException(ErrorCode.PASSWORD_RESET_TOKEN_EXPIRED);
        }
        return findUser(decode(token));
    }

    private Jwt decode(String token) {
        try {
            Jwt jwt = resetDecoder.decode(token);
            if (!TYPE_RESET.equals(jwt.getClaimAsString(TYPE_CLAIM)) || jwt.getExpiresAt() == null) {
                throw new ApiException(ErrorCode.PASSWORD_RESET_TOKEN_INVALID);
            }
            return jwt;
        } catch (JwtException exception) {
            throw new ApiException(ErrorCode.PASSWORD_RESET_TOKEN_INVALID);
        }
    }

    private AppUser findUser(Jwt jwt) {
        try {
            return appUserRepository.findById(Long.valueOf(jwt.getSubject()))
                    .filter(user -> user.getDeletedAt() == null)
                    .filter(user -> user.getStatus() == UserStatus.ACTIVE)
                    .orElseThrow(() -> new ApiException(ErrorCode.PASSWORD_RESET_TOKEN_INVALID));
        } catch (NumberFormatException exception) {
            throw new ApiException(ErrorCode.PASSWORD_RESET_TOKEN_INVALID);
        }
    }

    private static String fingerprint(String passwordHash) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(String.valueOf(passwordHash).getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is not available", exception);
        }
    }
}

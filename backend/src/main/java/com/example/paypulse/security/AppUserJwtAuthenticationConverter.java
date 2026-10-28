package com.example.paypulse.security;

import com.example.paypulse.model.AppUser;
import com.example.paypulse.repository.AppUserRepository;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class AppUserJwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private final AppUserRepository appUserRepository;

    public AppUserJwtAuthenticationConverter(AppUserRepository appUserRepository) {
        this.appUserRepository = appUserRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public AbstractAuthenticationToken convert(Jwt jwt) {
        AppUser user = appUserRepository.findById(Long.valueOf(jwt.getSubject()))
                .filter(candidate -> candidate.getDeletedAt() == null && candidate.isActive())
                .orElseThrow(() -> new DisabledException("User is not active"));
        AppUserPrincipal principal = AppUserPrincipal.from(user);
        return UsernamePasswordAuthenticationToken.authenticated(principal, jwt, principal.getAuthorities());
    }
}

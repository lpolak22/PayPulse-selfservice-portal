package com.example.paypulse.config;

import com.example.paypulse.model.UserRole;
import com.example.paypulse.security.AppUserJwtAuthenticationConverter;
import com.example.paypulse.security.SecurityErrorResponseWriter;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@SecurityScheme(name = SecurityConfig.BEARER_AUTH, type = SecuritySchemeType.HTTP, scheme = "bearer", bearerFormat = "JWT")
public class SecurityConfig {

    public static final String BEARER_AUTH = "bearerAuth";

    private static final int BCRYPT_STRENGTH = 12;

    private static final String[] OPEN_API_PATHS = {
            "/api/docs", "/api/docs/**", "/api/swagger-ui/**", "/swagger-ui/**", "/swagger-ui.html"
    };

    private static final String[] PUBLIC_AUTH_PATHS = {
            "/api/auth/login", "/api/auth/refresh", "/api/auth/logout"
    };

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,SecurityErrorResponseWriter errorWriter,AppUserJwtAuthenticationConverter jwtConverter)
            throws Exception {http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(errorWriter)
                        .accessDeniedHandler(errorWriter))
                .oauth2ResourceServer(resourceServer -> resourceServer
                        .authenticationEntryPoint(errorWriter)
                        .accessDeniedHandler(errorWriter)
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtConverter)))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(OPEN_API_PATHS).permitAll()
                        .requestMatchers(HttpMethod.POST, PUBLIC_AUTH_PATHS).permitAll()
                        .requestMatchers("/api/users/**").hasRole(UserRole.ADMIN.name())
                        .anyRequest().authenticated());

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(BCRYPT_STRENGTH);
    }
}

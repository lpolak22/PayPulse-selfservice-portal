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
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.security.oauth2.server.resource.web.DefaultBearerTokenResolver;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatchers;

import static org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher.pathPattern;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@SecurityScheme(name = SecurityConfig.BEARER_AUTH, type = SecuritySchemeType.HTTP, scheme = "bearer", bearerFormat = "JWT")
public class SecurityConfig {

    public static final String BEARER_AUTH = "bearerAuth";

    private static final int BCRYPT_STRENGTH = 12;

    private static final RequestMatcher PUBLIC_REQUESTS = RequestMatchers.anyOf(
            pathPattern("/api/docs"),
            pathPattern("/api/docs/**"),
            pathPattern("/api/swagger-ui/**"),
            pathPattern("/swagger-ui/**"),
            pathPattern("/swagger-ui.html"),
            pathPattern(HttpMethod.POST, "/api/auth/login"),
            pathPattern(HttpMethod.POST, "/api/auth/refresh"),
            pathPattern(HttpMethod.POST, "/api/auth/logout"),
            pathPattern(HttpMethod.POST, "/api/auth/activation"),
            pathPattern(HttpMethod.GET, "/api/auth/activation/**")
    );

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,SecurityErrorResponseWriter errorWriter,AppUserJwtAuthenticationConverter jwtConverter)
            throws Exception {http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(errorWriter)
                        .accessDeniedHandler(errorWriter))
                .oauth2ResourceServer(resourceServer -> resourceServer
                        .bearerTokenResolver(bearerTokenResolver())
                        .authenticationEntryPoint(errorWriter)
                        .accessDeniedHandler(errorWriter)
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtConverter)))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(PUBLIC_REQUESTS).permitAll()
                        .requestMatchers("/api/users/**").hasRole(UserRole.ADMIN.name())
                        .anyRequest().authenticated());

        return http.build();
    }

    private static BearerTokenResolver bearerTokenResolver() {
        BearerTokenResolver resolver = new DefaultBearerTokenResolver();
        return request -> PUBLIC_REQUESTS.matches(request) ? null : resolver.resolve(request);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(BCRYPT_STRENGTH);
    }
}

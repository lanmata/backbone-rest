/*
 *  @(#)SecurityConfig.java
 *
 *  Copyright (c) Luis Antonio Mata Mata. All rights reserved.
 *
 *   All rights to this product are owned by Luis Antonio Mata Mata and may only
 *  be used under the terms of its associated license document. You may NOT
 *  copy, modify, sublicense, or distribute this source file or portions of
 *  it unless previously authorized in writing by Luis Antonio Mata Mata.
 *  In any event, this notice and the above copyright must always be included
 *  verbatim with this file.
 */
package com.umdc.backoffice.security.config;

import com.umdc.backoffice.security.filter.ManagedClientTokenFilter;
import com.umdc.backoffice.security.filter.SessionJwtAuthenticationFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/// Security configuration for the backbone-rest application.
/// <p>
/// Uses the application's own session-token JWT as the sole authentication mechanism.
/// OAuth2 / Keycloak / Supabase are intentionally excluded.
/// </p>
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(SecurityConfig.class);

    private static final String[] SWAGGER_PATHS = {
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/swagger-resources/**",
            "/swagger-resources"
    };

    private final SessionJwtAuthenticationFilter sessionJwtAuthenticationFilter;
    private final ManagedClientTokenFilter managedClientTokenFilter;
//    private final String apiExcludes;
    private final String allowedOrigins;

    /// Constructs a new {@code SecurityConfig}.
    ///
    /// @param sessionJwtAuthenticationFilter the filter that validates the session-token header
    /// @param managedClientTokenFilter       the filter that validates M2M Bearer tokens
//    / @param apiExcludes                    comma-separated public paths from {@code umdc.api.excludes}
    /// @param allowedOrigins                 comma-separated allowed CORS origins (defaults to {@code *})
    public SecurityConfig(SessionJwtAuthenticationFilter sessionJwtAuthenticationFilter,
                          ManagedClientTokenFilter managedClientTokenFilter,
//                          @Value("${umdc.api.excludes}") String apiExcludes,
                          @Value("${umdc.cors.allowed-origins:*}") String allowedOrigins) {
        this.sessionJwtAuthenticationFilter = sessionJwtAuthenticationFilter;
        this.managedClientTokenFilter = managedClientTokenFilter;
//        this.apiExcludes = apiExcludes;
        this.allowedOrigins = allowedOrigins;
    }

    /// Configures the application security filter chain.
    /// <p>
    /// Disables CSRF (stateless REST API), sets stateless session management,
    /// permits Swagger, public session endpoints, and {@code umdc.api.excludes} paths,
    /// and requires authentication for everything else.
    /// The {@link SessionJwtAuthenticationFilter} is inserted before
    /// {@link UsernamePasswordAuthenticationFilter}.
    /// </p>
    ///
    /// @param http the {@link HttpSecurity} to configure
    /// @return the configured {@link SecurityFilterChain}
    /// @throws Exception if an error occurs during configuration
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable) // lgtm[java/spring-disabled-csrf-protection]
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> {
                auth.requestMatchers(SWAGGER_PATHS).permitAll();
                // Session endpoints — alias login, email login, validate, renew
                auth.requestMatchers(HttpMethod.POST, "/api/v1/session").permitAll();
                auth.requestMatchers(HttpMethod.POST, "/api/v1/session/token").permitAll();
                auth.requestMatchers(HttpMethod.GET,  "/api/v1/session/validate").permitAll();
                auth.requestMatchers(HttpMethod.GET,  "/api/v1/session/renew").permitAll();
                // Refresh endpoint — public because it IS the mechanism to obtain a new session
                // token when the access token has expired. Analogous to the login endpoints.
                auth.requestMatchers(HttpMethod.POST, "/api/v1/session/refresh").permitAll();
                // MCAM M2M public endpoints — token issuance and introspection (Phase 2 pre-registration)
                auth.requestMatchers(HttpMethod.POST, "/api/v1/managed-clients/token").permitAll();
                auth.requestMatchers(HttpMethod.POST, "/api/v1/managed-clients/introspect").permitAll();
                auth.anyRequest().authenticated();
            })
            .addFilterBefore(managedClientTokenFilter, UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(sessionJwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        LOGGER.info("SecurityFilterChain built successfully");
        return http.build();
    }

    /// Produces a CORS configuration source that applies to all paths.
    /// Allowed origins are controlled by the {@code umdc.cors.allowed-origins} property.
    ///
    /// @return the configured {@link CorsConfigurationSource}
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of(allowedOrigins.split(",")));
        configuration.setAllowedMethods(List.of(HttpMethod.GET.name(), HttpMethod.POST.name(), HttpMethod.PUT.name(),
                HttpMethod.DELETE.name(), HttpMethod.OPTIONS.name(), HttpMethod.PATCH.name()));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(false);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}

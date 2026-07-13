/*
 *  @(#)SessionJwtAuthenticationFilter.java
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
package com.umdc.backoffice.security.filter;

import com.umdc.backoffice.constant.keys.AuthKey;
import com.umdc.backoffice.security.util.RolesClaimParser;
import com.umdc.backoffice.v1.session.services.SessionService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

import static com.umdc.backoffice.v1.session.services.SessionJwtService.*;

/**
 * {@code OncePerRequestFilter} that validates the application's own session-token JWT.
 * <p>
 * If the {@code session-token} header is absent the request passes through unauthenticated
 * and the downstream filter chain decides whether authentication is required.
 * If the header is present but the token is invalid, the filter short-circuits with HTTP 401.
 * </p>
 */
@Component
public class SessionJwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(SessionJwtAuthenticationFilter.class);

    private final SessionService sessionService;

    /**
     * Constructs a new {@code SessionJwtAuthenticationFilter}.
     *
     * @param sessionService the session service used for token validation and claims extraction
     */
    public SessionJwtAuthenticationFilter(SessionService sessionService) {
        super();
        this.sessionService = sessionService;
    }

    /**
     * Filters each request once, validating the session-token header when present.
     *
     * @param request     the incoming HTTP request
     * @param response    the HTTP response
     * @param filterChain the remaining filter chain
     * @throws ServletException if a servlet error occurs
     * @throws IOException      if an I/O error occurs
     */
    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = request.getHeader(AUTHORIZATION_HEADER);

        if (token == null || token.isBlank() || !token.contains(BEARER_PREFIX)) {
            LOGGER.debug("No {} header present — passing request through unauthenticated", AUTHORIZATION_HEADER);
            filterChain.doFilter(request, response);
            return;
        }
        token = token.replace(BEARER_PREFIX, "");

        LOGGER.debug("{} header detected — validating token", AUTHORIZATION_HEADER);
        try {
            boolean valid = sessionService.isValid(token);
            LOGGER.debug("Token validity result: {}", valid);

            if (!valid) {
                LOGGER.warn("Invalid or expired session token received from {}", request.getRemoteAddr());
                rejectUnauthorized(response, "invalid_token");
                return;
            }

            Claims claims = sessionService.getTokenClaims(token);
            String subject = claims.getSubject();
            List<SimpleGrantedAuthority> authorities = extractAuthorities(claims);

            LOGGER.debug("Token valid for subject='{}', authorities={}", subject, authorities);

            var authentication = new UsernamePasswordAuthenticationToken(subject, null, authorities);
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } catch (Exception e) {
            LOGGER.error("Session token processing failed from {}: {}", request.getRemoteAddr(), e.getMessage(), e);
            SecurityContextHolder.clearContext();
            rejectUnauthorized(response, "token_processing_error");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private void rejectUnauthorized(HttpServletResponse response, String errorCode) throws IOException {
        SecurityContextHolder.clearContext();
        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.getWriter().write("{\"error\":\"" + errorCode + "\"}");
    }

    /**
     * Extracts role-based granted authorities from the token claims.
     * The {@code roles} claim is stored as the string representation of a list,
     * e.g. {@code [ROLE_ADMIN, ROLE_USER]}.
     *
     * @param claims the JWT claims
     * @return list of {@link SimpleGrantedAuthority} instances
     */
    private List<SimpleGrantedAuthority> extractAuthorities(Claims claims) {
        Object rolesObj = claims.get(AuthKey.ROLES_ID.value);
        return RolesClaimParser.parseRoles(rolesObj).stream()
                .map(SimpleGrantedAuthority::new)
                .toList();
    }
}

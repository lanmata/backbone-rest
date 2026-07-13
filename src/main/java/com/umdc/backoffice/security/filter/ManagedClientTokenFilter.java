/*
 *  @(#)ManagedClientTokenFilter.java
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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.umdc.backoffice.v1.managedclient.service.ManagedClientTokenService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * {@code OncePerRequestFilter} that validates M2M Bearer tokens issued by the MCAM subsystem.
 * <p>
 * Only intercepts tokens whose JWT payload contains {@code "type":"M2M"}.
 * If the token is invalid or revoked, the filter returns HTTP 401 immediately.
 * All other tokens (no M2M type claim) are passed through to the next filter.
 * </p>
 */
@Component
public class ManagedClientTokenFilter extends OncePerRequestFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(ManagedClientTokenFilter.class);
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String TOKEN_TYPE_M2M = "M2M";
    private static final String CLAIM_TYPE = "type";
    private static final String CLAIM_SUB = "sub";
    private static final String CLAIM_SCOPES = "scopes";
    private static final int JWT_MIN_PART_COUNT = 2;
    private static final int BASE64_NEEDS_TWO_PADS = 2;
    private static final int BASE64_NEEDS_ONE_PAD = 3;

    private final ManagedClientTokenService tokenService;
    private final ObjectMapper objectMapper;

    /**
     * Constructs a new {@code ManagedClientTokenFilter}.
     *
     * @param tokenService the M2M token validation service
     * @param objectMapper the Jackson mapper for JWT payload parsing
     */
    public ManagedClientTokenFilter(ManagedClientTokenService tokenService, ObjectMapper objectMapper) {
        super();
        this.tokenService = tokenService;
        this.objectMapper = objectMapper;
    }

    /** {@inheritDoc} */
    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            filterChain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }

        String rawToken = authHeader.substring(BEARER_PREFIX.length());
        if (!isManagedClientToken(rawToken)) {
            filterChain.doFilter(request, response);
            return;
        }

        LOGGER.debug("M2M token detected from {}", request.getRemoteAddr());

        try {
            if (!tokenService.isTokenActive(rawToken)) {
                LOGGER.warn("Invalid or revoked M2M token from {}", request.getRemoteAddr());
                rejectUnauthorized(response, "invalid_token");
                return;
            }

            Map<String, Object> payload = decodePayload(rawToken);
            String subject = (String) payload.getOrDefault(CLAIM_SUB, "");
            @SuppressWarnings("unchecked")
            List<String> scopes = (List<String>) payload.getOrDefault(CLAIM_SCOPES, Collections.emptyList());

            List<SimpleGrantedAuthority> authorities = scopes.stream()
                    .map(SimpleGrantedAuthority::new)
                    .toList();

            var authentication = new UsernamePasswordAuthenticationToken(subject, null, authorities);
            SecurityContextHolder.getContext().setAuthentication(authentication);
            LOGGER.debug("M2M authentication set for clientId='{}'", subject);
        } catch (Exception e) {
            LOGGER.error("M2M token processing failed from {}: {}", request.getRemoteAddr(), e.getMessage(), e);
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

    private boolean isManagedClientToken(String rawToken) {
        try {
            Map<String, Object> payload = decodePayload(rawToken);
            return TOKEN_TYPE_M2M.equals(payload.get(CLAIM_TYPE));
        } catch (Exception e) {
            return false;
        }
    }

    private Map<String, Object> decodePayload(String rawToken) throws IOException {
        String[] parts = rawToken.split("\\.");
        if (parts.length < JWT_MIN_PART_COUNT) {
            return Collections.emptyMap();
        }
        byte[] payloadBytes = Base64.getUrlDecoder().decode(padBase64(parts[1]));
        return objectMapper.readValue(payloadBytes, new TypeReference<Map<String, Object>>() {});
    }

    private static String padBase64(String encoded) {
        int pad = encoded.length() % 4;
        if (pad == BASE64_NEEDS_TWO_PADS) return encoded + "==";
        if (pad == BASE64_NEEDS_ONE_PAD) return encoded + "=";
        return encoded;
    }
}

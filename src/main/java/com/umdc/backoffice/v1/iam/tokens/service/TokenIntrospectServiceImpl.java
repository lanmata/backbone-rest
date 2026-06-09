/*
 *  @(#)TokenIntrospectServiceImpl.java
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
package com.umdc.backoffice.v1.iam.tokens.service;

import com.umdc.backoffice.constant.keys.AuthKey;
import com.umdc.backoffice.security.util.RolesClaimParser;
import com.umdc.backoffice.v1.iam.tokens.api.to.TokenIntrospectRequest;
import com.umdc.backoffice.v1.iam.tokens.api.to.TokenIntrospectResponse;
import com.umdc.backoffice.v1.session.services.SessionService;
import io.jsonwebtoken.Claims;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

/// Implementation of {@link TokenIntrospectService}.
/// <p>
/// Returns an {@code active: false} response for blank or invalid tokens,
/// and a fully populated response for valid tokens.
/// </p>
@Service
public class TokenIntrospectServiceImpl implements TokenIntrospectService {

    private static final Logger LOGGER = LoggerFactory.getLogger(TokenIntrospectServiceImpl.class);
    private static final TokenIntrospectResponse INACTIVE =
            new TokenIntrospectResponse(false, null, null, null, 0L, 0L, null, null);

    private final SessionService sessionService;

    /// Constructs a new {@code TokenIntrospectServiceImpl}.
    ///
    /// @param sessionService the session service used for token validation and claims extraction
    public TokenIntrospectServiceImpl(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    /// {@inheritDoc}
    @Override
    public ResponseEntity<TokenIntrospectResponse> introspect(TokenIntrospectRequest request) {
        String token = request.token();

        if (token == null || token.isBlank()) {
            LOGGER.debug("Introspect called with blank token");
            return ResponseEntity.badRequest().body(INACTIVE);
        }

        if (!sessionService.isValid(token)) {
            LOGGER.debug("Introspect — token invalid or expired");
            return ResponseEntity.ok(INACTIVE);
        }

        Claims claims = sessionService.getTokenClaims(token);
        String subject = claims.getSubject();
        String issuer = claims.getIssuer();

        // Audience is a Set<String> in JJWT 0.12.x
        String audience = null;
        Set<String> audienceSet = claims.getAudience();
        if (audienceSet != null && !audienceSet.isEmpty()) {
            audience = String.join(",", audienceSet);
        }

        long expiresAt = claims.getExpiration() != null ? claims.getExpiration().getTime() : 0L;
        long issuedAt  = claims.getIssuedAt()   != null ? claims.getIssuedAt().getTime()   : 0L;

        Object typeObj = claims.get(AuthKey.TYPE.value);
        String tokenType = typeObj != null ? typeObj.toString() : null;

        List<String> roles = RolesClaimParser.parseRoles(claims.get(AuthKey.ROLES_ID.value));

        LOGGER.debug("Introspect — subject='{}', roles={}", subject, roles);

        TokenIntrospectResponse response =
                new TokenIntrospectResponse(true, subject, issuer, audience, expiresAt, issuedAt, tokenType, roles);
        return ResponseEntity.ok(response);
    }
}

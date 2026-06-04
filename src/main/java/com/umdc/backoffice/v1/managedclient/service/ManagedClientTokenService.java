/*
 *  @(#)ManagedClientTokenService.java
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
package com.umdc.backoffice.v1.managedclient.service;

import com.umdc.backoffice.v1.managedclient.api.to.ManagedClientTokenRequest;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

/// Service interface for MCAM M2M authentication token operations.
public interface ManagedClientTokenService {

    /// Issues an RS256 M2M access token after validating credentials and rate limits.
    ///
    /// @param request the token issuance request containing clientId, secret, and scopes
    /// @return HTTP 200 with {@code ManagedClientTokenResponse}, or HTTP 401/429 on failure
    ResponseEntity<?> issueToken(ManagedClientTokenRequest request);

    /// Revokes all active tokens for the given managed client.
    ///
    /// @param clientId the managed client whose tokens are to be revoked
    /// @return HTTP 204 on success, or HTTP 404 if the client does not exist
    ResponseEntity<?> revokeAllTokens(UUID clientId);

    /// Introspects the given raw JWT.
    /// Always returns HTTP 200; {@code active=false} for invalid, expired, or revoked tokens.
    ///
    /// @param rawToken the JWT string to introspect
    /// @return HTTP 200 with {@code ManagedClientTokenIntrospectResponse}
    ResponseEntity<?> introspectToken(String rawToken);

    /// Returns {@code true} if the raw JWT is valid, not expired, and not revoked.
    /// Used internally by {@link ManagedClientTokenFilter}.
    ///
    /// @param rawToken the JWT string to validate
    /// @return {@code true} if the token is active
    boolean isTokenActive(String rawToken);
}

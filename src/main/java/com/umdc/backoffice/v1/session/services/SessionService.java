/*
 *  @(#)SessionService.java
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

package com.umdc.backoffice.v1.session.services;

import com.umdc.backoffice.v1.session.to.SessionEmailRequest;
import com.umdc.backoffice.v1.session.to.SessionRequest;
import com.umdc.backoffice.v1.session.to.SessionResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * Service for managing session-related operations.
 * Provides methods for generating and validating session tokens.
 *
 * @version 1.0.0, 12-02-2021
 */
public interface SessionService extends SessionJwtService {

    /**
     * Loads a session token for the given user alias and password.
     *
     * @param sessionRequest the session request containing user credentials
     * @return a ResponseEntity containing the session response with the generated token
     */
    default ResponseEntity<SessionResponse> loadSession(SessionRequest sessionRequest) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }

    /**
     * Loads a session token for the given user email and password.
     *
     * @param sessionEmailRequest the session email request containing user credentials
     * @return a ResponseEntity containing the session response with the generated token
     */
    default ResponseEntity<SessionResponse> loadSession(SessionEmailRequest sessionEmailRequest) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }

    /**
     * Renews a session token by validating the current token and generating a new one.
     *
     * @param currentToken the current session token to be renewed
     * @return a ResponseEntity containing the session response with the new token
     */
    default ResponseEntity<SessionResponse> renewToken(String currentToken) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }

    /**
     * Revokes the given session token by adding its JTI to the deny-list.
     *
     * @param token the session token to revoke
     * @return 204 No Content on success; 400 Bad Request on invalid token
     */
    default ResponseEntity<Void> revokeToken(String token) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }

    /// Exchanges a valid or recently-expired refresh token for a new access token
    /// and a new refresh token.
    /// <p>
    /// The refresh token must carry {@code type=refresh-token} (or {@code type=session-token}
    /// for backward compatibility). A grace period of 7 days beyond expiry is honoured.
    /// </p>
    ///
    /// @param refreshToken the refresh token string
    /// @return a {@link ResponseEntity} with a new {@link SessionResponse}; 401 if the token
    ///         is invalid, revoked, or beyond the grace period; 404 if the user is not found
    default ResponseEntity<SessionResponse> refreshSession(String refreshToken) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }
}

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

package com.prx.backoffice.v1.session.services;

import com.prx.backoffice.v1.session.to.SessionEmailRequest;
import com.prx.backoffice.v1.session.to.SessionRequest;
import com.prx.backoffice.v1.session.to.SessionResponse;
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
}

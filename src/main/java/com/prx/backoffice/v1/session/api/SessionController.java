/*
 *  @(#)SessionController.java
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
package com.prx.backoffice.v1.session.api;

import com.prx.backoffice.v1.session.services.SessionService;
import com.prx.backoffice.v1.session.to.SessionRequest;
import com.prx.backoffice.v1.session.to.SessionResponse;
import io.jsonwebtoken.ExpiredJwtException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.prx.security.constant.ConstantApp.SESSION_TOKEN_KEY;

/**
 * REST controller for managing session-related operations.
 * Provides endpoints for generating and validating session tokens.
 *
 * @version 1.0.0, 12-02-2021
 */
@RestController
@RequestMapping("/v1/session")
public class SessionController {

    private final SessionService sessionService;

    /**
     * Constructor for SessionController.
     *
     * @param sessionService the session service to be used by this controller
     */
    public SessionController(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    /**
     * Endpoint to generate a session token.
     *
     * @param sessionRequest the session request containing user credentials
     * @return a ResponseEntity containing the session response with the generated token
     */
    @PostMapping
    public ResponseEntity<SessionResponse> generateSessionToken(@RequestBody SessionRequest sessionRequest) {
        return sessionService.loadSession(sessionRequest);
    }

    /**
     * Endpoint to validate a session token.
     *
     * @param sessionToken the session token to be validated
     * @return a ResponseEntity containing a boolean indicating whether the token is valid
     */
    @GetMapping("/validate")
    public ResponseEntity<Boolean> validateSessionToken(
            @RequestHeader(SESSION_TOKEN_KEY) String sessionToken) {
        boolean isValid = false;
        try {
            var value = sessionService.getTokenClaims(sessionToken).get("type");
            isValid = SESSION_TOKEN_KEY.equals(value) && !sessionService.isTokenExpired(sessionToken);

        } catch (ExpiredJwtException e) {
            return ResponseEntity.ok(false);
        }
        return ResponseEntity.ok(isValid);
    }

}

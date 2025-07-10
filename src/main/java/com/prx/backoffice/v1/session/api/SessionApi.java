/*
 *  @(#)SessionApi.java
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

import com.prx.backoffice.util.MessageUtil;
import com.prx.backoffice.v1.session.services.SessionService;
import com.prx.backoffice.v1.session.to.SessionEmailRequest;
import com.prx.backoffice.v1.session.to.SessionRequest;
import com.prx.backoffice.v1.session.to.SessionResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.Map;

import static com.prx.backoffice.v1.session.services.SessionJwtService.SESSION_TOKEN_KEY;

@Tag(name = "session", description = "The Session API")
public interface SessionApi {

    default SessionService getSessionService() {
        return new SessionService() {

            @Override
            public String generateSessionToken(String username, Map<String, String> parameters) {
                return HttpStatus.NOT_IMPLEMENTED.name();
            }

            @Override
            public String getUsernameFromToken(String token) {
                return HttpStatus.NOT_IMPLEMENTED.name();
            }
        };
    }

    /// Endpoint to generate a session token.
    ///
    /// @param sessionRequest the session request containing user credentials
    /// @return a ResponseEntity containing the session response with the generated token
    @Operation(summary = "Generate session token", description = "Generates a session token based on user credentials")
    @ApiResponses(value = {
            @ApiResponse(responseCode = MessageUtil.OK, description = "Session token generated successfully"),
            @ApiResponse(responseCode = MessageUtil.NOT_FOUND, description = "Invalid request payload"),
            @ApiResponse(responseCode = MessageUtil.UNAUTHORIZED, description = "Invalid credentials")
    })
    @PostMapping
    default ResponseEntity<SessionResponse> generateSessionToken(@RequestBody SessionRequest sessionRequest) {
        return this.getSessionService().loadSession(sessionRequest);
    }

    /// Endpoint to generate a session token.
    ///
    /// @param sessionEmailRequest the session email request containing user credentials
    /// @return a ResponseEntity containing the session response with the generated token
    @Operation(summary = "Generate session token with email", description = "Generates a session token based on user email credentials")
    @ApiResponses(value = {
            @ApiResponse(responseCode = MessageUtil.OK, description = "Session token generated successfully"),
            @ApiResponse(responseCode = MessageUtil.NOT_FOUND, description = "Invalid request payload"),
            @ApiResponse(responseCode = MessageUtil.UNAUTHORIZED, description = "Invalid credentials")
    })
    @PostMapping("/token")
    default ResponseEntity<SessionResponse> generateSessionToken(@RequestBody SessionEmailRequest sessionEmailRequest) {
        return this.getSessionService().loadSession(sessionEmailRequest);
    }

    /// Endpoint to validate a session token.
    ///
    /// @param sessionToken the session token to be validated
    /// @return a ResponseEntity containing a boolean indicating whether the token is valid
    @Operation(summary = "Validate session token", description = "Validates the provided session token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = MessageUtil.OK, description = "Session token is valid"),
            @ApiResponse(responseCode = MessageUtil.UNAUTHORIZED, description = "Invalid session token")
    })
    @GetMapping("/validate")
    default ResponseEntity<Boolean> validateSessionToken(@RequestHeader(SESSION_TOKEN_KEY) String sessionToken) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(Boolean.FALSE);
    }

    /// Endpoint to renew a session token.
    ///
    /// @param sessionToken the current session token to be renewed
    /// @return a ResponseEntity containing the session response with the new token
    @Operation(summary = "Renew session token", description = "Renews the provided session token by validating it and generating a new one")
    @ApiResponses(value = {
            @ApiResponse(responseCode = MessageUtil.OK, description = "Session token renewed successfully"),
            @ApiResponse(responseCode = MessageUtil.BAD_REQUEST, description = "Invalid request - token is required"),
            @ApiResponse(responseCode = MessageUtil.UNAUTHORIZED, description = "Invalid or expired session token"),
            @ApiResponse(responseCode = MessageUtil.NOT_FOUND, description = "User not found"),
            @ApiResponse(responseCode = MessageUtil.INTERNAL_SERVER_ERROR, description = "Token generation failed")
    })
    @GetMapping(value = "/renew", produces = {MediaType.APPLICATION_JSON_VALUE})
    default ResponseEntity<SessionResponse> renewSessionToken(@RequestHeader(SESSION_TOKEN_KEY) String sessionToken) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(new SessionResponse());
    }

}

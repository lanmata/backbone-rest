/*
 *  @(#)TokenIntrospectApi.java
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
package com.umdc.backoffice.v1.iam.tokens.api;

import com.umdc.backoffice.v1.iam.tokens.api.to.TokenIntrospectRequest;
import com.umdc.backoffice.v1.iam.tokens.api.to.TokenIntrospectResponse;
import com.umdc.backoffice.v1.iam.tokens.service.TokenIntrospectService;
import com.umdc.commons.util.HttpStatusUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import jakarta.validation.Valid;

/// REST API interface for IAM token introspection.
/// <p>
/// Requires a valid session-token JWT — this endpoint is <em>not</em> in {@code umdc.api.excludes}.
/// </p>
@Tag(name = "iam-tokens", description = "IAM Token Introspection API")
@RequestMapping("/api/v1/iam/tokens")
public interface TokenIntrospectApi {

    /// Returns the {@link TokenIntrospectService} used by this API.
    ///
    /// @return the service instance
    default TokenIntrospectService getService() {
        return request -> ResponseEntity.status(HttpStatusUtil.NOT_IMPLEMENTED).build();
    }

    /// Introspects the provided JWT token and returns its decoded metadata.
    ///
    /// @param request the introspection request containing the raw JWT token
    /// @return a {@link ResponseEntity} wrapping {@link TokenIntrospectResponse}
    @Operation(summary = "Introspect token", description = "Decodes and validates an application session-token JWT, returning its metadata")
    @ApiResponses(value = {
            @ApiResponse(responseCode = HttpStatusUtil.OK_STR, description = "Token introspection result returned (active=false for invalid tokens)"),
            @ApiResponse(responseCode = HttpStatusUtil.BAD_REQUEST_STR, description = "Token is blank or missing")
    })
    @PostMapping(value = "/introspect", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    default ResponseEntity<TokenIntrospectResponse> introspect(@Valid @RequestBody TokenIntrospectRequest request) {
        return getService().introspect(request);
    }
}


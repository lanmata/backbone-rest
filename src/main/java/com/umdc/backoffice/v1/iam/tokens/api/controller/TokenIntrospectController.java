/*
 *  @(#)TokenIntrospectController.java
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
package com.umdc.backoffice.v1.iam.tokens.api.controller;

import com.umdc.backoffice.v1.iam.tokens.api.TokenIntrospectApi;
import com.umdc.backoffice.v1.iam.tokens.api.to.TokenIntrospectRequest;
import com.umdc.backoffice.v1.iam.tokens.api.to.TokenIntrospectResponse;
import com.umdc.backoffice.v1.iam.tokens.service.TokenIntrospectService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;

/// Thin REST controller for IAM token-introspection operations.
/// Delegates all business logic to {@link TokenIntrospectService}.
@RestController
@CrossOrigin(origins = "*")
public class TokenIntrospectController implements TokenIntrospectApi {

    private final TokenIntrospectService tokenIntrospectService;

    /// Constructs a new {@code TokenIntrospectController}.
    ///
    /// @param tokenIntrospectService the service that handles token introspection
    public TokenIntrospectController(TokenIntrospectService tokenIntrospectService) {
        this.tokenIntrospectService = tokenIntrospectService;
    }

    @Override
    public TokenIntrospectService getService() {
        return this.tokenIntrospectService;
    }

    @Override
    public ResponseEntity<TokenIntrospectResponse> introspect(TokenIntrospectRequest request) {
        return tokenIntrospectService.introspect(request);
    }
}


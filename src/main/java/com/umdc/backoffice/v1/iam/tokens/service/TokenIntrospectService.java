/*
 *  @(#)TokenIntrospectService.java
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

import com.umdc.backoffice.v1.iam.tokens.api.to.TokenIntrospectRequest;
import com.umdc.backoffice.v1.iam.tokens.api.to.TokenIntrospectResponse;
import org.springframework.http.ResponseEntity;

/// Service contract for JWT token introspection.
public interface TokenIntrospectService {

    /// Introspects the provided JWT token and returns its decoded metadata.
    ///
    /// @param request the introspection request containing the raw token
    /// @return a {@link ResponseEntity} wrapping a {@link TokenIntrospectResponse}
    ResponseEntity<TokenIntrospectResponse> introspect(TokenIntrospectRequest request);
}


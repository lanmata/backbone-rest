/*
 *  @(#)TokenIntrospectControllerTest.java
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

import com.umdc.backoffice.v1.iam.tokens.api.to.TokenIntrospectRequest;
import com.umdc.backoffice.v1.iam.tokens.api.to.TokenIntrospectResponse;
import com.umdc.backoffice.v1.iam.tokens.service.TokenIntrospectService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

/**
 * @author Luis Mata
 */
class TokenIntrospectControllerTest {

    @Mock
    private TokenIntrospectService tokenIntrospectService;

    private TokenIntrospectController tokenIntrospectController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        tokenIntrospectController = new TokenIntrospectController(tokenIntrospectService);
    }

    @Test
    @DisplayName("getService returns the injected TokenIntrospectService")
    void getServiceReturnsInjectedService() {
        assertEquals(tokenIntrospectService, tokenIntrospectController.getService());
    }

    @Test
    @DisplayName("introspect delegates to TokenIntrospectService.introspect")
    void introspectDelegatesToService() {
        TokenIntrospectRequest request = new TokenIntrospectRequest("a.jwt.token");
        ResponseEntity<TokenIntrospectResponse> expected = ResponseEntity.ok(
                new TokenIntrospectResponse(true, "user-1", "backbone-rest", "backbone-rest-client",
                        1_000_000L, 900_000L, "session-token", List.of("ROLE_ADMIN")));
        when(tokenIntrospectService.introspect(request)).thenReturn(expected);

        var response = tokenIntrospectController.introspect(request);

        assertEquals(expected, response);
    }
}

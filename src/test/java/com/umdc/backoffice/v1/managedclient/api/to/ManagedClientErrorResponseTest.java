/*
 *  @(#)ManagedClientErrorResponseTest.java
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

package com.umdc.backoffice.v1.managedclient.api.to;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ManagedClientErrorResponseTest {

    @Test
    @DisplayName("default constructor and setters populate every field")
    void defaultConstructorAndSetters() {
        var response = new ManagedClientErrorResponse();
        response.setError("invalid_client");
        response.setErrorDescription("Client not found or inactive.");
        response.setClientId("11111111-1111-1111-1111-111111111111");

        Assertions.assertAll("Test Getters and Setters",
                () -> Assertions.assertEquals("invalid_client", response.getError()),
                () -> Assertions.assertEquals("Client not found or inactive.", response.getErrorDescription()),
                () -> Assertions.assertEquals("11111111-1111-1111-1111-111111111111", response.getClientId())
        );
    }

    @Test
    @DisplayName("two-arg constructor sets error and description, leaves clientId null")
    void twoArgConstructor() {
        var response = new ManagedClientErrorResponse("invalid_scope", "Requested scopes exceed registered scopes.");

        Assertions.assertAll("Test two-arg constructor",
                () -> Assertions.assertEquals("invalid_scope", response.getError()),
                () -> Assertions.assertEquals("Requested scopes exceed registered scopes.", response.getErrorDescription()),
                () -> Assertions.assertNull(response.getClientId())
        );
    }

    @Test
    @DisplayName("three-arg constructor sets all three fields")
    void threeArgConstructor() {
        var response = new ManagedClientErrorResponse("rate_limit_exceeded",
                "Token issuance rate limit exceeded.", "22222222-2222-2222-2222-222222222222");

        Assertions.assertAll("Test three-arg constructor",
                () -> Assertions.assertEquals("rate_limit_exceeded", response.getError()),
                () -> Assertions.assertEquals("Token issuance rate limit exceeded.", response.getErrorDescription()),
                () -> Assertions.assertEquals("22222222-2222-2222-2222-222222222222", response.getClientId())
        );
    }
}

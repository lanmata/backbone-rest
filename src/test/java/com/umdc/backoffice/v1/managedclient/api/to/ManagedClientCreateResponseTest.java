/*
 *  @(#)ManagedClientCreateResponseTest.java
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

class ManagedClientCreateResponseTest {

    @Test
    @DisplayName("Test getters and setters of ManagedClientCreateResponse")
    void gettersAndSetters() {
        var response = new ManagedClientCreateResponse();
        var clientId = UUID.randomUUID();
        var applicationId = UUID.randomUUID();
        var createdAt = LocalDateTime.now();

        response.setClientId(clientId);
        response.setClientSecret("raw-secret-value");
        response.setName("test-client");
        response.setApplicationId(applicationId);
        response.setScopes(List.of("read:data", "write:data"));
        response.setActive(true);
        response.setCreatedAt(createdAt);

        Assertions.assertAll("Test Getters and Setters",
                () -> Assertions.assertEquals(clientId, response.getClientId()),
                () -> Assertions.assertEquals("raw-secret-value", response.getClientSecret()),
                () -> Assertions.assertEquals("test-client", response.getName()),
                () -> Assertions.assertEquals(applicationId, response.getApplicationId()),
                () -> Assertions.assertEquals(List.of("read:data", "write:data"), response.getScopes()),
                () -> Assertions.assertTrue(response.isActive()),
                () -> Assertions.assertEquals(createdAt, response.getCreatedAt())
        );
    }

    @Test
    @DisplayName("default constructor leaves active false and reference fields null")
    void defaultConstructor() {
        var response = new ManagedClientCreateResponse();

        Assertions.assertAll("Test default state",
                () -> Assertions.assertNull(response.getClientId()),
                () -> Assertions.assertNull(response.getClientSecret()),
                () -> Assertions.assertNull(response.getName()),
                () -> Assertions.assertNull(response.getApplicationId()),
                () -> Assertions.assertNull(response.getScopes()),
                () -> Assertions.assertFalse(response.isActive()),
                () -> Assertions.assertNull(response.getCreatedAt())
        );
    }
}

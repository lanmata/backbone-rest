/*
 * @(#)$file.className.java.
 *
 * Copyright (c) Luis Antonio Mata Mata. All rights reserved.
 *
 * All rights to this product are owned by Luis Antonio Mata Mata and may only
 * be used under the terms of its associated license document. You may NOT
 * copy, modify, sublicense, or distribute this source file or portions of
 * it unless previously authorized in writing by Luis Antonio Mata Mata.
 * In any event, this notice and the above copyright must always be included
 * verbatim with this file.
 */
package com.prx.backoffice.v1.user.api.to;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.*;

/**
 * UserCreateRequestTest.
 *
 * @author Luis Antonio Mata
 * @version 1.0.0, 20-10-2020
 */
class UserCreateRequestTest {

    @Test
    void gettersAndSetters() {
        final var userCreateRequest = new UserCreateRequest();

        userCreateRequest.setUser(new UserTO());
        userCreateRequest.setAppName("APC22536");
        userCreateRequest.setAppToken("ABC22598");
        userCreateRequest.setDateTime(LocalDateTime.now(ZoneId.systemDefault()));

        assertAll("Test Getters and Setters",
            () -> assertNotNull(userCreateRequest.getUser()),
            () -> assertNotNull(userCreateRequest.getAppName()),
            () -> assertNotNull(userCreateRequest.getAppToken()),
            () -> assertNotNull(userCreateRequest.getDateTime()),
            () -> assertNotNull(userCreateRequest.toString()),
            () -> assertNotEquals(1, userCreateRequest.hashCode()),
            () -> assertNotEquals(new UserCreateRequest(), userCreateRequest)
                 );

    }

}

/*
 *
 *  * @(#)UserAccessResponseTest.java.
 *  *
 *  * Copyright (c) Luis Antonio Mata Mata. All rights reserved.
 *  *
 *  * All rights to this product are owned by Luis Antonio Mata Mata and may only
 *  * be used under the terms of its associated license document. You may NOT
 *  * copy, modify, sublicense, or distribute this source file or portions of
 *  * it unless previously authorized in writing by Luis Antonio Mata Mata.
 *  * In any event, this notice and the above copyright must always be included
 *  * verbatim with this file.
 *
 */
package com.prx.backoffice.to.user;

import java.time.LocalDateTime;
import java.time.ZoneId;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;

/**
 * UserAccessResponseTest.
 *
 * @author Luis Antonio Mata
 * @version 1.0.0, 20-10-2020
 */
class UserAccessResponseTest {

    @Test
    void gettersAndSetters() {
        final var userAccessResponse = new UserAccessResponse();

        userAccessResponse.setToken("ABC22598");
        userAccessResponse.setCode(200);
        userAccessResponse.setDateTime(LocalDateTime.now(ZoneId.systemDefault()));
        userAccessResponse.setMessage("Mensaje de respuesta");

        assertAll("Test Getters and Setters",
            () -> assertNotNull(userAccessResponse.getToken()),
            () -> assertNotNull(userAccessResponse.getCode()),
            () -> assertNotNull(userAccessResponse.getDateTime()),
            () -> assertNotNull(userAccessResponse.getMessage()),
            () -> assertNotNull(userAccessResponse.toString()),
            () -> assertNotEquals(1, userAccessResponse.hashCode()),
            () -> assertNotEquals(new UserAccessResponse(), userAccessResponse)
                 );

    }
}
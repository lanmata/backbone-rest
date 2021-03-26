/*
 *
 *  * @(#)UserResponseTest.java.
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

import com.prx.commons.pojo.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * UserResponseTest.
 *
 * @author Luis Antonio Mata
 * @version 1.0.0, 20-10-2020
 */
class UserResponseTest {

    @Test
    void gettersAndSetters(){
        final var userResponse = new UserResponse();
        userResponse.setUser(new User());
        userResponse.setCode(100);
        userResponse.setDateTime(LocalDateTime.now(ZoneId.systemDefault()));
        userResponse.setMessage("Mensaje de respuestas");
        Assertions.assertAll("Test Getters and Setters",
            () -> Assertions.assertNotNull(userResponse.getUser()),
            () -> Assertions.assertNotNull(userResponse.getCode()),
            () -> Assertions.assertNotNull(userResponse.getDateTime()),
            () -> Assertions.assertNotNull(userResponse.getMessage()),
            () -> Assertions.assertNotNull(userResponse.toString()),
            () -> Assertions.assertNotEquals(1, userResponse.hashCode()),
            () -> Assertions.assertNotEquals(new UserResponse(), userResponse)
                 );
    }

}
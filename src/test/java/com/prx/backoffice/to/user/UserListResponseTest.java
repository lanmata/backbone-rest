/*
 *
 *  * @(#)UserListResponseTest.java.
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
import java.util.ArrayList;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;

/**
 * UserListResponseTest.
 *
 * @author Luis Antonio Mata
 * @version 1.0.0, 20-10-2020
 */
class UserListResponseTest {

    @Test
    void gettersAndSetters(){
        final var userListResponse =  new UserListResponse();

        userListResponse.setDatetimeResponse(LocalDateTime.now(ZoneId.systemDefault()));
        userListResponse.setList(new ArrayList<>());
        userListResponse.setCode(200);
        userListResponse.setDateTime(LocalDateTime.now(ZoneId.systemDefault()));
        userListResponse.setMessage("Mensaje de respuesta");

        assertAll("Test Getters and Setters",
            () -> assertNotNull(userListResponse.getDatetimeResponse()),
            () -> assertNotNull(userListResponse.getList()),
            () -> assertNotNull(userListResponse.getCode()),
            () -> assertNotNull(userListResponse.getDateTime()),
            () -> assertNotNull(userListResponse.getMessage()),
            () -> assertNotNull(userListResponse.toString()),
            () -> assertNotEquals(1, userListResponse.hashCode()),
            () -> assertNotEquals(new UserListResponse(), userListResponse)
                 );

    }

}
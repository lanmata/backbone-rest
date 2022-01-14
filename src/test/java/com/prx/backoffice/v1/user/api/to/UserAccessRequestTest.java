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
package com.prx.backoffice.to.user;

import java.time.LocalDateTime;
import java.time.ZoneId;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.prx.backoffice.v1.user.api.to.UserAccessRequest;
import org.junit.jupiter.api.Test;

/**
 * UserAccessRequestTest.
 *
 * @author Luis Antonio Mata
 * @version 1.0.0, 20-10-2020
 */
class UserAccessRequestTest {

    @Test
    void gettersAndSetters() {
        final var userAccessRequest = new UserAccessRequest();

        userAccessRequest.setAlias("lmata");
        userAccessRequest.setPassword("123456789");
        userAccessRequest.setAppName("AP001");
        userAccessRequest.setAppToken("3456d789gfsduyt");
        userAccessRequest.setDateTime(LocalDateTime.now(ZoneId.systemDefault()));

        assertAll("Test Getters and Setters",
            () -> assertNotNull(userAccessRequest.getAlias()),
            () -> assertNotNull(userAccessRequest.getPassword()),
            () -> assertNotNull(userAccessRequest.getDateTime()),
            () -> assertNotNull(userAccessRequest.getAppName()),
            () -> assertNotNull(userAccessRequest.getAppToken()),
            () -> assertNotNull(userAccessRequest.toString()),
            () -> assertNotEquals(1, userAccessRequest.hashCode()),
            () -> assertNotEquals(new UserAccessRequest(), userAccessRequest)
                 );
    }

}
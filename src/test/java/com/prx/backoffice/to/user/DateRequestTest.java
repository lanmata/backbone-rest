/*
 *
 *  * @(#)DateRequestTest.java.
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
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

/**
 * DateRequestTest.
 *
 * @author Luis Antonio Mata
 * @version 1.0.0, 20-10-2020
 */
class DateRequestTest {

    @Test
    void gettersAndSetters() {
        final var dateRequest = new DateRequest();

        dateRequest.setAppName("AP001");
        dateRequest.setAppToken("3456d789gfsduyt");
        dateRequest.setDateTime(LocalDateTime.now(ZoneId.systemDefault()));

        assertAll("Test Getters and Setters",
            () -> assertNotNull(dateRequest.getDateTime()),
            () -> assertNotNull(dateRequest.getAppName()),
            () -> assertNotNull(dateRequest.getAppToken()),
            () -> assertNotNull(dateRequest.toString()),
            () -> assertNotEquals(1, dateRequest.hashCode()),
            () -> assertNotEquals(new DateRequest(), dateRequest)
                 );

    }

}
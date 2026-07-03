/*
 *  @(#)SessionRequestTest.java
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
package com.umdc.backoffice.v1.users.api.to;

import com.umdc.backoffice.v1.session.to.SessionRequest;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Unit tests for the {@link SessionRequest} record.
 *
 * @author Luis Antonio Mata
 * @version 2.0.0, 03-07-2026
 */
class SessionRequestTest {

    @Test
    void accessorsReturnConstructorValues() {
        UUID appId = UUID.randomUUID();
        SessionRequest request = new SessionRequest("lmata", "123456789", appId);

        assertAll("SessionRequest accessors",
                () -> assertEquals("lmata", request.alias()),
                () -> assertEquals("123456789", request.password()),
                () -> assertEquals(appId, request.applicationId()),
                () -> assertNotNull(request.toString())
        );
    }

    @Test
    void equalRecordsAreEqual() {
        UUID appId = UUID.randomUUID();
        SessionRequest r1 = new SessionRequest("lmata", "secret", appId);
        SessionRequest r2 = new SessionRequest("lmata", "secret", appId);

        assertEquals(r1, r2);
        assertEquals(r1.hashCode(), r2.hashCode());
    }

    @Test
    void differentAliasProducesInequality() {
        UUID appId = UUID.randomUUID();
        SessionRequest r1 = new SessionRequest("alice", "secret", appId);
        SessionRequest r2 = new SessionRequest("bob", "secret", appId);

        assertNotEquals(r1, r2);
    }
}

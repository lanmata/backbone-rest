/*
 *  @(#)ContactTypeRequestTest.java
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

package com.umdc.backoffice.v1.contacttypes.api.to;

import com.umdc.commons.general.pojo.ContactType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

class ContactTypeRequestTest {

    @Test
    @DisplayName("Test getters, setters and toString of ContactTypeRequest")
    void gettersAndSetters() {
        final var contactTypeRequest = new ContactTypeRequest();
        var contactType = new ContactType();
        contactType.setId(UUID.randomUUID());
        contactType.setName("Email");

        contactTypeRequest.setContactType(contactType);

        Assertions.assertAll("Test Getters and Setters",
                () -> Assertions.assertNotNull(contactTypeRequest.getContactType()),
                () -> Assertions.assertEquals(contactType, contactTypeRequest.getContactType()),
                () -> Assertions.assertTrue(contactTypeRequest.toString().contains("ContactTypeRequest"))
        );
    }
}

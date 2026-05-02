/*
 *  @(#)ContactRequestTest.java
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

package com.umdc.backoffice.v1.contacts.to;

import com.umdc.backoffice.v1.contacts.api.to.ContactRequest;
import com.umdc.commons.general.pojo.Contact;
import com.umdc.commons.general.pojo.ContactType;
import com.umdc.commons.general.pojo.Person;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;


class ContactRequestTest {

    @Test
    @DisplayName("Test getters and setters for ContactRequest")
    void testGetterAndSetter() {
        final var uuid = UUID.fromString("e8e8a8cc-9493-4677-8c7e-2a09ef51f6ea");
        final var contactRequest = new ContactRequest();
        final var contact = new Contact();
        final var person = new Person();
        final var contactType = new ContactType();
        person.setBirthdate(LocalDate.of(1979, 4, 14));
        person.setFirstName("Pepe");
        person.setGender("M");
        person.setId(UUID.randomUUID());
        person.setLastName("Perez");
        person.setMiddleName("Peter");
        contactType.setId(UUID.randomUUID());
        contactType.setActive(true);
        contactType.setName("Contact TST 001");
        contactType.setDescription("Contact description TST 001");
        contact.setId(uuid);
        contact.setContent("Contact TST0 01");
        contact.setActive(true);
        contact.setContactType(contactType);
        contact.setContactType(contactType);
        contactRequest.setContact(contact);
        contactRequest.setAppToken("ABC001");
        contactRequest.setAppName("TST-001");
        contactRequest.setDateTime(LocalDateTime.now());

        assertAll(() -> assertNotNull(contactRequest),
                () -> assertNotNull(contactRequest.getContact()),
                () -> assertEquals(uuid, contactRequest.getContact().getId()),
                () -> assertNotNull(contactRequest.getContact().getContactType()),
                () -> assertNotNull(contactRequest.getContact().getActive()),
                () -> assertNotNull(contactRequest.getContact().getContent()),
                () -> assertNotNull(contactRequest.getContact().toString()),
                () -> assertNotNull(contactRequest.toString())
        );
    }
}

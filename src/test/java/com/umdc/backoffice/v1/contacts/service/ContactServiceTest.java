/*
 *  @(#)ContactServiceTest.java
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

package com.umdc.backoffice.v1.contacts.service;

import com.umdc.commons.general.pojo.Contact;
import org.apache.commons.lang3.NotImplementedException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;

class ContactServiceTest {

    private final ContactService contactService = new ContactService() {

    };


    @Test
    @DisplayName("Test saveAll method")
    void saveAll() {
        List<Contact> contactList = List.of(new Contact());
        assertThrows(NotImplementedException.class, () -> contactService.saveAll(contactList));
    }

    @Test
    @DisplayName("Test create method")
    void create() {
        UUID id = UUID.randomUUID();
        Contact contact = new Contact();
        assertThrows(NotImplementedException.class, () -> contactService.create(id, contact));
    }

    @Test
    @DisplayName("Test update method")
    void update() {
        UUID id = UUID.randomUUID();
        Contact contact = new Contact();
        assertThrows(NotImplementedException.class, () -> contactService.update(id, contact));
    }

    @Test
    @DisplayName("Test find method")
    void find() {
        UUID id = UUID.randomUUID();
        assertThrows(NotImplementedException.class, () -> contactService.find(id));
    }

    @Test
    @DisplayName("Test listByPersonId method")
    void listByPersonId() {
        UUID id = UUID.randomUUID();
        assertThrows(NotImplementedException.class, () -> contactService.listByPersonId(id));
    }

    @Test
    @DisplayName("Test list method")
    void list() {
        var uuidList = List.of(UUID.randomUUID());
        assertThrows(NotImplementedException.class, () -> contactService.list(uuidList));
    }

    @Test
    @DisplayName("Test deleteById method")
    void deleteById() {
        UUID id = UUID.randomUUID();
        assertThrows(NotImplementedException.class, () -> contactService.deleteById(id));
    }
}

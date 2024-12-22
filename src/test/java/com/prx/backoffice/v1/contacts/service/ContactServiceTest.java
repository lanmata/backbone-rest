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

package com.prx.backoffice.v1.contacts.service;

import com.prx.commons.pojo.Contact;
import org.apache.commons.lang.NotImplementedException;
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
        assertThrows(NotImplementedException.class, () -> contactService.saveAll(List.of(new Contact())));
    }

    @Test
    @DisplayName("Test create method")
    void create() {
        assertThrows(NotImplementedException.class, () -> contactService.create(new Contact()));
    }

    @Test
    @DisplayName("Test update method")
    void update() {
        assertThrows(NotImplementedException.class, () -> contactService.update(new Contact(), UUID.randomUUID().toString()));
    }

    @Test
    @DisplayName("Test find method")
    void find() {
        assertThrows(NotImplementedException.class, () -> contactService.find(UUID.randomUUID().toString()));
    }

    @Test
    @DisplayName("Test listByPersonId method")
    void listByPersonId() {
        assertThrows(NotImplementedException.class, () -> contactService.listByPersonId(UUID.randomUUID().toString()));
    }

    @Test
    @DisplayName("Test list method")
    void list() {
        assertThrows(NotImplementedException.class, () -> contactService.list(List.of(UUID.randomUUID().toString())));
    }

    @Test
    @DisplayName("Test deleteById method")
    void deleteById() {
        assertThrows(NotImplementedException.class, () -> contactService.deleteById(UUID.randomUUID().toString()));
    }
}

/*
 *  @(#)ContactTypeServiceTest.java
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

package com.prx.backoffice.v1.contacttypes.service;

import com.prx.backoffice.v1.contacttypes.to.ContactTypeRequest;
import com.prx.commons.pojo.ContactType;
import org.apache.commons.lang.NotImplementedException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertThrows;

class ContactTypeServiceTest {

    private final ContactTypeService contactTypeService = new ContactTypeService() {
    };

    /**
     * Method under test: {@link ContactTypeService#listById(List)}
     */
    @Test
    @DisplayName("Test listing contact types by ID - Empty List")
    void testListById() {
        assertThrows(NotImplementedException.class, () -> contactTypeService.listById(new ArrayList<>()));
    }

    /**
     * Method under test: {@link ContactTypeService#listById(List)}
     */
    @Test
    @DisplayName("Test listing contact types by ID - Single ID")
    void testListById2() {
        ArrayList<String> contactTypeIds = new ArrayList<>();
        contactTypeIds.add("aa7ca292-eede-4319-b20b-5db61a56efa8");
        assertThrows(NotImplementedException.class, () -> contactTypeService.listById(contactTypeIds));
    }

    /**
     * Method under test: {@link ContactTypeService#listById(List)}
     */
    @Test
    @DisplayName("Test listing contact types by ID - Multiple IDs")
    void testListById3() {
        ArrayList<String> contactTypeIds = new ArrayList<>();
        contactTypeIds.add("d65f84d0-4845-46fb-a541-e46ef3dd30e0");
        contactTypeIds.add("c26f681f-f25c-4dde-b57c-22bd0b296877");
        assertThrows(NotImplementedException.class, () -> contactTypeService.listById(contactTypeIds));
    }

    /**
     * Method under test: {@link ContactTypeService#update(String, ContactType)}
     */
    @Test
    @DisplayName("Test updating a contact type")
    void testUpdate() {
        assertThrows(NotImplementedException.class, () -> contactTypeService.update("d425105c-9539-4790-b281-32ff22407888", new ContactType()));
    }

    /**
     * Method under test: {@link ContactTypeService#create(ContactTypeRequest)}
     */
    @Test
    @DisplayName("Test creating a contact type")
    void testCreate() {
        assertThrows(NotImplementedException.class, () -> contactTypeService.create(new ContactTypeRequest()));
    }

    /**
     * Method under test: {@link ContactTypeService#list()}
     */
    @Test
    @DisplayName("Test listing all contact types")
    void testList() {
        assertThrows(NotImplementedException.class, contactTypeService::list);
    }

    /**
     * Method under test: {@link ContactTypeService#list()}
     */
    @Test
    @DisplayName("Test deleting a contact type")
    void testDelete() {
        assertThrows(NotImplementedException.class, () -> contactTypeService.delete("c241fa1f-1770-4e6a-ab50-99e86562fd4a"));
    }

    /**
     * Method under test: {@link ContactTypeService#list()}
     */
    @Test
    @DisplayName("Test finding a contact type by ID")
    void testFindById() {
        assertThrows(NotImplementedException.class, () -> contactTypeService.findById("c241fa1f-1770-4e6a-ab50-99e86562fd4a"));
    }
}

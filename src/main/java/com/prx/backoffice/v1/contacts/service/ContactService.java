/*
 *  @(#)ContactService.java
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

import com.prx.commons.general.pojo.Contact;
import com.prx.commons.services.CrudService;
import org.apache.commons.lang.NotImplementedException;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.UUID;

/**
 * ContactService.
 *
 * @author <a href="mailto:luis.antonio.mata@gmail.com">Luis Antonio Mata</a>
 * @version 1.0.1.20200904-01, 02-11-2020
 */
public interface ContactService extends CrudService<UUID, Contact> {

    /**
     * Records incoming contacts associated with individuals.
     *
     * @param contacts {@link List} object type.
     * @return {@link List} object type with a {@link Contact} object type.
     */
    default List<Contact> saveAll(List<Contact> contacts) {
        throw new NotImplementedException();
    }

    /**
     * Save a contact.
     *
     * @param contact {@link Contact} object type.
     * @return {@link ResponseEntity} object type with a {@link Contact} object type.
     */
    default ResponseEntity<Contact> create(UUID personId, Contact contact) {
        throw new NotImplementedException();
    }

    /**
     * Update a contact exist.
     *
     * @param contact {@link Contact} object type.
     * @return {@link ResponseEntity} object type with a {@link Contact} object type.
     */
    default ResponseEntity<Contact> update(UUID contactId, Contact contact){
        throw new NotImplementedException();
    }

    /**
     * Find contact by contact id.
     *
     * @param id {@link UUID} object type.
     * @return {@link ResponseEntity} object type with a {@link Contact} object type.
     */
    default ResponseEntity<Contact> find(UUID id) {
        throw new NotImplementedException();
    }

    /**
     * List contacts by person id.
     *
     * @param personId {@link UUID} object type.
     * @return {@link ResponseEntity} object type with a {@link Contact} object type.
     */
    default ResponseEntity<List<Contact>> listByPersonId(UUID personId) {
        throw new NotImplementedException();
    }

    /**
     * List contact by contact id.
     *
     * @param contactIds {@link UUID} object type.
     * @return {@link ResponseEntity} object type with a {@link Contact} object type.
     */
    default ResponseEntity<List<Contact>> list(List<UUID> contactIds) {
        throw new NotImplementedException();
    }

    /**
     * Delete contact by contactId.
     *
     * @param id {@link UUID}
     * @return {@link ResponseEntity} object type.
     */
    default ResponseEntity<String> deleteById(UUID id) {
        throw new NotImplementedException();
    }
}

/*
 *  @(#)ContactController.java
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

package com.umdc.backoffice.v1.contacts.api;

import com.umdc.backoffice.v1.contacts.service.ContactService;
import com.umdc.commons.general.pojo.Contact;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * ContactApiController.
 *
 * @author <a href="mailto:luis.antonio.mata@gmail.com">Luis Antonio Mata</a>
 * @version 1.0.0, 07-04-2022
 * @since 11
 */
@RestController
@RequestMapping("/api/v1/contacts")
public class ContactController implements ContactApi {

    private final ContactService contactService;

    public ContactController(ContactService contactService) {
        this.contactService = contactService;
    }

    @Override
    public ResponseEntity<Contact> create(final Contact contact) {
        return contactService.create(contact);
    }

    @Override
    public ResponseEntity<Contact> update(final UUID contactId, Contact contact) {
        return contactService.update(contactId, contact);
    }

    @Override
    public ResponseEntity<Contact> find(final UUID contactId) {
        return contactService.find(contactId);
    }

    @Override
    public ResponseEntity<List<Contact>> list(List<UUID> contactIds) {
        return contactService.list(contactIds);
    }

    @Override
    public ResponseEntity<List<Contact>> list(UUID personId) {
        return contactService.listByPersonId(personId);
    }

    @Override
    public ResponseEntity<List<Contact>> list() {
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    @Override
    public ResponseEntity<String> delete(final UUID contactId) {
        return contactService.deleteById(contactId);
    }

}

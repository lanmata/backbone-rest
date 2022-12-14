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

package com.prx.backoffice.v1.contact.service;

import com.prx.backoffice.v1.contact.to.ContactRequest;
import com.prx.commons.pojo.Contact;
import com.prx.commons.pojo.MessageActivity;
import org.springframework.http.ResponseEntity;

import java.math.BigInteger;
import java.util.List;

/**
 * ContactService.
 *
 * @author <a href="mailto:luis.antonio.mata@gmail.com">Luis Antonio Mata</a>
 * @version 1.0.1.20200904-01, 02-11-2020
 */
public interface ContactService {

    /**
     * Registra los contactos recibidos asociados a las personas
     *
     * @param contacts Objeto de tipo {@link List}
     *
     * @return Objeto de tipo {@link MessageActivity}
     */
    List<Contact> saveAll(List<Contact> contacts);

    ResponseEntity<Contact> create(ContactRequest contactRequest);

    /**
     * Update a contact exist.
     * @param contact {@link Contact} object type.
     * @return {@link ResponseEntity} object type with a {@link Contact} object type.
     */
    ResponseEntity<Contact> update(Contact contact, BigInteger contactId);

    /**
     * Find contact by contact id.
     * @param contactId {@link BigInteger} object type.
     * @return {@link ResponseEntity} object type with a {@link Contact} object type.
     */
    ResponseEntity<Contact> find(BigInteger contactId);
}

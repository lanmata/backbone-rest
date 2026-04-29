/*
 *  @(#)ContactServiceImpl.java
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

import com.umdc.backoffice.v1.contacts.mapper.ContactMapper;
import com.umdc.backoffice.v1.contacttypes.mapper.ContactTypeMapper;
import com.prx.commons.general.pojo.Contact;
import com.prx.persistence.general.domains.ContactEntity;
import com.prx.persistence.general.repositories.ContactRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.umdc.backoffice.util.MessageUtil.MESSAGE_HEADER_STR;

/**
 * ContactService.
 *
 * @author <a href="mailto:luis.antonio.mata@gmail.com">Luis Antonio Mata</a>
 * @version 1.0.0, 20-10-2020
 */
@Service
public class ContactServiceImpl implements ContactService {
    private final ContactRepository contactRepository;
    private final ContactMapper contactMapper;
    private final ContactTypeMapper contactTypeMapper;
    @Value("${app.environments.contact.limit}")
    private int contactLimit;

    public ContactServiceImpl(ContactRepository contactRepository, ContactMapper contactMapper, ContactTypeMapper contactTypeMapper) {
        this.contactRepository = contactRepository;
        this.contactMapper = contactMapper;
        this.contactTypeMapper = contactTypeMapper;
    }

    public List<Contact> saveAll(List<Contact> contacts) {
        final List<ContactEntity> results = new ArrayList<>();
        contacts.forEach(contact -> results.add(contactRepository.save(contactMapper.toSource(contact))));

        if (!results.isEmpty()) {
            return results.stream().map(contactMapper::toTarget).collect(Collectors.toList());
        }

        return new ArrayList<>();
    }

    @Override
    public ResponseEntity<Contact> create(UUID personId, Contact contact) {
        if (null == contact) {
            return ResponseEntity.notFound().build();
        }
        var contactList = listByPersonId(personId);
        if (contactList.getStatusCode().equals(HttpStatus.OK) && contactList.hasBody() && Objects.nonNull(contactList.getBody())) {
            if (contactList.getBody().size() < contactLimit) {
                var contactEntity = contactMapper.toSource(contact);
                var response = contactRepository.save(contactEntity);
                return ResponseEntity.status(HttpStatus.CREATED).header(MESSAGE_HEADER_STR, "Contact created").body(contactMapper.toTarget(response));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).header(MESSAGE_HEADER_STR, "Contact NOT created. Contact limit has been reached.").build();
            }
        }
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<Contact> update(UUID contactId, Contact contact) {
        if (null == contactId || null == contact) {
            return ResponseEntity.notFound().build();
        }
        var contactOptionResult = contactRepository.findById(contactId);
        if (contactOptionResult.isPresent()) {
            var contactEntity = contactOptionResult.get();
            contactEntity.setContent(contact.getContent());
            contactEntity.setActive(contact.getActive());
            contactEntity.setContactType(contactTypeMapper.toSource(contact.getContactType()));
            return ResponseEntity.ok(contactMapper.toTarget(contactRepository.save(contactEntity)));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @Override
    public ResponseEntity<Contact> find(UUID contactId) {
        if (null == contactId) {
            return ResponseEntity.badRequest().build();
        }
        var contactEntityResult = contactRepository.findById(contactId);
        if (contactEntityResult.isPresent()) {
            var contact = contactMapper.toTarget(contactEntityResult.get());
            return ResponseEntity.ok(contact);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @Override
    public ResponseEntity<List<Contact>> listByPersonId(UUID personId) {
        var optionalContactList = contactRepository.listByPersonId(personId);
        if (optionalContactList.isPresent()) {
            var contactList = new ArrayList<Contact>();
            optionalContactList.get().stream().toList().forEach(contactEntity -> contactList.add(contactMapper.toTarget(contactEntity)));
            return ResponseEntity.ok(contactList);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @Override
    public ResponseEntity<String> deleteById(UUID contactId) {
        var contactItem = contactRepository.findById(contactId);
        if (contactItem.isPresent()) {
            contactRepository.deleteById(contactId);
            return ResponseEntity.accepted().header(MESSAGE_HEADER_STR, "The Contact has been removed.").build();
        }
        return ResponseEntity.notFound().header(MESSAGE_HEADER_STR, "The Contact is NOT present.").build();
    }
}

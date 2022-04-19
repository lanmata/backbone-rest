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

import com.prx.backoffice.v1.contact.mapper.ContactMapper;
import com.prx.commons.pojo.Contact;
import com.prx.persistence.general.domains.ContactEntity;
import com.prx.persistence.general.repositories.ContactRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * ContactService.
 *
 * @author <a href="mailto:luis.antonio.mata@gmail.com">Luis Antonio Mata</a>
 * @version 1.0.0, 20-10-2020
 */
@Service
@RequiredArgsConstructor
public class ContactServiceImpl implements ContactService{
    private final ContactRepository contactRepository;
    private final ContactMapper contactMapper;

    public List<Contact> saveAll(List<Contact> contacts) {
        final List<ContactEntity> results = new ArrayList<>();
        contacts.forEach(contact -> results.add(contactRepository.save(contactMapper.toSource(contact))));

        if(!results.isEmpty()) {
            return results.stream().map(contactMapper::toTarget).collect(Collectors.toList());
        }

        return new ArrayList<>();
    }
}

/*
 *
 *  * @(#)ContactServiceImpl.java.
 *  *
 *  * Copyright (c) Luis Antonio Mata Mata. All rights reserved.
 *  *
 *  * All rights to this product are owned by Luis Antonio Mata Mata and may only
 *  * be used under the terms of its associated license document. You may NOT
 *  * copy, modify, sublicense, or distribute this source file or portions of
 *  * it unless previously authorized in writing by Luis Antonio Mata Mata.
 *  * In any event, this notice and the above copyright must always be included
 *  * verbatim with this file.
 *  
 */

package com.prx.backoffice.service.impl;

import com.prx.backoffice.mapper.ContactMapper;
import com.prx.commons.pojo.Contact;
import com.prx.persistence.general.repositories.ContactRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * ContactService.
 *
 * @author Luis Antonio Mata
 * @version 1.0.0, 20-10-2020
 */
@Service
@RequiredArgsConstructor
public class ContactServiceImpl {
    private final ContactRepository contactRepository;
    private final ContactMapper contactMapper;

    public void saveAll(List<Contact> contacts) {
        contacts.forEach(contact ->
            contactRepository.save(contactMapper.toSource(contact))
        );
    }
}

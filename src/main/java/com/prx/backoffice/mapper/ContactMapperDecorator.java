/*
 *
 *  * @(#)ContactMapperDecorator.java.
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
package com.prx.backoffice.mapper;

import com.prx.commons.enums.types.ContactType;
import com.prx.commons.pojo.Contact;
import com.prx.persistence.general.domain.ContactEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * ContactMapperDecorator.
 *
 * @author Luis Antonio Mata
 * @version 1.0.0, 21-10-2020
 */
@Service
@RequiredArgsConstructor
public abstract class ContactMapperDecorator implements ContactMapper {

    private final ContactMapper contactMapper;

    @Override
    public Contact toTarget(final ContactEntity contactEntity) {
        var contact = contactMapper.toTarget(contactEntity);
        contact.setContactTypeId(contactEntity.getContactType().ordinal());
        return contact;
    }

    @Override
    public ContactEntity toSource(final Contact contact) {
        var contactEntity = contactMapper.toSource(contact);
        contactEntity.setContactType(ContactType.getConctactType(contact.getContactTypeId()));
        return contactEntity;
    }

}

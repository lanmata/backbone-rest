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

import com.prx.backoffice.MockLoaderBase;
import com.prx.backoffice.util.ConstantUtilTest;
import com.prx.backoffice.v1.contact.mapper.ContactMapper;
import com.prx.backoffice.v1.contact.to.ContactRequest;
import com.prx.backoffice.v1.contacttype.mapper.ContactTypeMapper;
import com.prx.backoffice.v1.person.mapper.PersonMapper;
import com.prx.commons.pojo.Contact;
import com.prx.commons.pojo.ContactType;
import com.prx.commons.pojo.Person;
import com.prx.persistence.general.domains.ContactEntity;
import com.prx.persistence.general.domains.ContactTypeEntity;
import com.prx.persistence.general.domains.PersonEntity;
import com.prx.persistence.general.repositories.ContactRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.math.BigInteger;
import java.time.LocalDate;

/**
 * ContactServiceImplTest.
 *
 * @author &lt;a href='mailto:luis.antonio.mata@gmail.com'&gt;Luis Antonio Mata&lt;/a&gt;
 * @version 1.0.0, 08-12-2021
 * @since 11
 */
class ContactServiceImplTest extends MockLoaderBase {

    @InjectMocks
    ContactServiceImpl contactService;

    @Mock
    ContactMapper contactMapper;
    @Mock
    ContactTypeMapper contactTypeMapper;
    @Mock
    PersonMapper personMapper;
    @Mock
    ContactRepository contactRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void saveAll() {

    }

    @Test
    void create() {
        var contactEntity = getContactEntity();
        var contactTypeEntity = contactEntity.getContactType();
        var contactRequest = getContactRequest();
        Mockito.doReturn(contactRequest.getContact().getContactType()).when(contactTypeMapper)
                .toTarget(ArgumentMatchers.any(ContactTypeEntity.class));
        Mockito.doReturn(contactRequest.getContact().getPerson()).when(personMapper)
                .toTarget(ArgumentMatchers.any(PersonEntity.class));
        Mockito.doReturn(contactEntity).when(contactMapper).toSource(ArgumentMatchers.any(Contact.class));
        final var responseEntity = contactService.create(contactRequest);
        Assertions.assertNotNull(responseEntity);
    }

    @Test
    void update() {
        var contactEntity = getContactEntity();
        var contactTypeEntity = contactEntity.getContactType();
        var contact = getContact();
        Mockito.doReturn(contact.getContactType()).when(contactTypeMapper)
                .toTarget(ArgumentMatchers.any(ContactTypeEntity.class));
        Mockito.doReturn(contact.getPerson()).when(personMapper)
                .toTarget(ArgumentMatchers.any(PersonEntity.class));
        Mockito.doReturn(contactEntity).when(contactMapper).toSource(ArgumentMatchers.any(Contact.class));
        final var responseEntity = contactService.update(getContact(), BigInteger.valueOf(1));
        Assertions.assertNotNull(responseEntity);
    }

    @Test
    void find() {
        var contactEntity = getContactEntity();
        var contactTypeEntity = contactEntity.getContactType();
        var contact = getContact();
        Mockito.doReturn(contact.getContactType()).when(contactTypeMapper)
                .toTarget(ArgumentMatchers.any(ContactTypeEntity.class));
        Mockito.doReturn(contact.getPerson()).when(personMapper)
                .toTarget(ArgumentMatchers.any(PersonEntity.class));
        Mockito.doReturn(contactEntity).when(contactMapper).toSource(ArgumentMatchers.any(Contact.class));
        final var responseEntity = contactService.find(BigInteger.valueOf(1));
        Assertions.assertNotNull(responseEntity);
    }

    private static ContactEntity getContactEntity() {
        var contactEntity = new ContactEntity();
        var contactTypeEntity = new ContactTypeEntity();
        var personEntity = new PersonEntity();
        personEntity.setId(1L);
        personEntity.setName("Fausto");
        personEntity.setMiddleName("Joaquin");
        personEntity.setLastName("Perez");
        personEntity.setGender("M");
        personEntity.setBirthdate(LocalDate.of(1983, 12,23));
        contactTypeEntity.setId(BigInteger.valueOf(1));
        contactTypeEntity.setName("Messenger");
        contactTypeEntity.setActive(true);
        contactTypeEntity.setDescription("Instant message app");
        contactEntity.setActive(true);
        contactEntity.setContactType(contactTypeEntity);
        contactEntity.setId(null);
        contactEntity.setContent("Contact description");
        contactEntity.setPerson(personEntity);
        return contactEntity;
    }

    private static ContactRequest getContactRequest() {
        var contactRequest = new ContactRequest();
        contactRequest.setContact(getContact());
        contactRequest.setAppName(ConstantUtilTest.APP_NAME_VALUE);
        contactRequest.setAppToken(ConstantUtilTest.APP_TOKEN_VALUE);
        return contactRequest;
    }

    private static Contact getContact() {
        var person = new Person();
        person.setId(1L);
        person.setFirstName("Fausto");
        person.setMiddleName("Joaquin");
        person.setLastName("Perez");
        person.setGender("M");
        person.setBirthdate(LocalDate.of(1983, 12,23));
        var contactType = new ContactType();
        contactType.setId(1L);
        contactType.setName("Messenger");
        contactType.setActive(true);
        contactType.setDescription("Instant message app");
        var contact = new Contact();
        contact.setActive(true);
        contact.setContactType(contactType);
        contact.setId(null);
        contact.setContent("Contact description");
        contact.setPerson(person);
        return contact;
    }
}

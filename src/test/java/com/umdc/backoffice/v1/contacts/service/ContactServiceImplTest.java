/*
 *  @(#)ContactServiceImplTest.java
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
import com.umdc.commons.general.pojo.Contact;
import com.umdc.commons.general.pojo.ContactType;
import com.umdc.persistence.general.domains.ContactEntity;
import com.umdc.persistence.general.domains.ContactTypeEntity;
import com.umdc.persistence.general.domains.PersonEntity;
import com.umdc.persistence.general.repositories.ContactRepository;
import com.umdc.backoffice.v1.contacttypes.mapper.ContactTypeMapperImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ContactServiceImplTest {
    @InjectMocks
    private ContactServiceImpl contactServiceImpl;

    @Mock
    private ContactMapper contactMapper;

    @Mock
    private ContactRepository contactRepository;

    @Mock
    private ContactTypeMapper contactTypeMapper;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(contactServiceImpl, "contactLimit", 10);
    }

    /**
     * Method under test: {@link ContactServiceImpl#saveAll(List)}
     */
    @Test
    @DisplayName("Test saving all contacts with empty list")
    void testSaveAll() {
        assertTrue(contactServiceImpl.saveAll(new ArrayList<>()).isEmpty());
    }

    /**
     * Method under test: {@link ContactServiceImpl#saveAll(List)}
     */
    @Test
    @DisplayName("Test saving all contacts with valid data")
    void testSaveAll2() {
        var uuid = UUID.randomUUID();
        ContactTypeEntity contactType = new ContactTypeEntity();
        contactType.setActive(true);
        contactType.setDescription("The characteristics of someone or something");
        contactType.setId(uuid);
        contactType.setName("Name");

        PersonEntity person = new PersonEntity();
        person.setBirthdate(LocalDate.of(1970, 1, 1));
        person.setGender("Gender");
        person.setId(uuid);
        person.setLastName("Doe");
        person.setMiddleName("Middle Name");
        person.setName("Name");

        ContactEntity contactEntity = new ContactEntity();
        contactEntity.setActive(true);
        contactEntity.setContactType(contactType);
        contactEntity.setContent("Not all who wander are lost");
        contactEntity.setId(UUID.randomUUID());
        contactEntity.setPerson(person);
        when(contactRepository.save(Mockito.<ContactEntity>any())).thenReturn(contactEntity);

        ContactType contactType2 = new ContactType();
        contactType2.setActive(true);
        contactType2.setDescription("The characteristics of someone or something");
        contactType2.setId(uuid);
        contactType2.setName("Name");

        Contact contact = new Contact();
        contact.setActive(true);
        contact.setContactType(contactType2);
        contact.setContent("Not all who wander are lost");
        contact.setId(uuid);

        ContactTypeEntity contactType3 = new ContactTypeEntity();
        contactType3.setActive(true);
        contactType3.setDescription("The characteristics of someone or something");
        contactType3.setId(UUID.randomUUID());
        contactType3.setName("Name");

        PersonEntity person3 = new PersonEntity();
        person3.setBirthdate(LocalDate.of(1970, 1, 1));
        person3.setGender("Gender");
        person3.setId(UUID.randomUUID());
        person3.setLastName("Doe");
        person3.setMiddleName("Middle Name");
        person3.setName("Name");

        ContactEntity contactEntity2 = new ContactEntity();
        contactEntity2.setActive(true);
        contactEntity2.setContactType(contactType3);
        contactEntity2.setContent("Not all who wander are lost");
        contactEntity2.setId(UUID.randomUUID());
        contactEntity2.setPerson(person3);
        when(contactMapper.toTarget(Mockito.<ContactEntity>any())).thenReturn(contact);
        when(contactMapper.toSource(Mockito.<Contact>any())).thenReturn(contactEntity2);

        ContactType contactType4 = new ContactType();
        contactType4.setActive(true);
        contactType4.setDescription("The characteristics of someone or something");
        contactType4.setId(uuid);
        contactType4.setName("Name");

        Contact contact2 = new Contact();
        contact2.setActive(true);
        contact2.setContactType(contactType4);
        contact2.setContent("Not all who wander are lost");
        contact2.setId(uuid);

        ArrayList<Contact> contacts = new ArrayList<>();
        contacts.add(contact2);
        assertEquals(1, contactServiceImpl.saveAll(contacts).size());
        verify(contactRepository).save(Mockito.<ContactEntity>any());
        verify(contactMapper).toTarget(Mockito.<ContactEntity>any());
        verify(contactMapper).toSource(Mockito.<Contact>any());
    }

    /**
     * Method under test: {@link ContactServiceImpl#saveAll(List)}
     */
    @Test
    @DisplayName("Test saving all contacts with mixed valid and invalid data")
    void testSaveAll3() {
        var uuid = UUID.randomUUID();
        ContactTypeEntity contactType = new ContactTypeEntity();
        contactType.setActive(true);
        contactType.setDescription("The characteristics of someone or something");
        contactType.setId(UUID.randomUUID());
        contactType.setName("Name");

        PersonEntity person = new PersonEntity();
        person.setBirthdate(LocalDate.of(1970, 1, 1));
        person.setGender("Gender");
        person.setId(UUID.randomUUID());
        person.setLastName("Doe");
        person.setMiddleName("Middle Name");
        person.setName("Name");

        ContactEntity contactEntity = new ContactEntity();
        contactEntity.setActive(true);
        contactEntity.setContactType(contactType);
        contactEntity.setContent("Not all who wander are lost");
        contactEntity.setId(UUID.randomUUID());
        contactEntity.setPerson(person);
        when(contactRepository.save(Mockito.<ContactEntity>any())).thenReturn(contactEntity);

        ContactType contactType2 = new ContactType();
        contactType2.setActive(true);
        contactType2.setDescription("The characteristics of someone or something");
        contactType2.setId(uuid);
        contactType2.setName("Name");

        Contact contact = new Contact();
        contact.setActive(true);
        contact.setContactType(contactType2);
        contact.setContent("Not all who wander are lost");
        contact.setId(uuid);

        ContactTypeEntity contactType3 = new ContactTypeEntity();
        contactType3.setActive(true);
        contactType3.setDescription("The characteristics of someone or something");
        contactType3.setId(UUID.randomUUID());
        contactType3.setName("Name");

        PersonEntity person3 = new PersonEntity();
        person3.setBirthdate(LocalDate.of(1970, 1, 1));
        person3.setGender("Gender");
        person3.setId(UUID.randomUUID());
        person3.setLastName("Doe");
        person3.setMiddleName("Middle Name");
        person3.setName("Name");

        ContactEntity contactEntity2 = new ContactEntity();
        contactEntity2.setActive(true);
        contactEntity2.setContactType(contactType3);
        contactEntity2.setContent("Not all who wander are lost");
        contactEntity2.setId(UUID.randomUUID());
        contactEntity2.setPerson(person3);
        when(contactMapper.toTarget(Mockito.<ContactEntity>any())).thenReturn(contact);
        when(contactMapper.toSource(Mockito.<Contact>any())).thenReturn(contactEntity2);

        ContactType contactType4 = new ContactType();
        contactType4.setActive(true);
        contactType4.setDescription("The characteristics of someone or something");
        contactType4.setId(uuid);
        contactType4.setName("Name");

        Contact contact2 = new Contact();
        contact2.setActive(true);
        contact2.setContactType(contactType4);
        contact2.setContent("Not all who wander are lost");
        contact2.setId(uuid);

        ContactType contactType5 = new ContactType();
        contactType5.setActive(false);
        contactType5.setDescription("Description");
        contactType5.setId(uuid);
        contactType5.setName("Name");

        Contact contact3 = new Contact();
        contact3.setActive(false);
        contact3.setContactType(contactType5);
        contact3.setContent("Content");
        contact3.setId(uuid);

        ArrayList<Contact> contacts = new ArrayList<>();
        contacts.add(contact3);
        contacts.add(contact2);
        assertEquals(2, contactServiceImpl.saveAll(contacts).size());
        verify(contactRepository, atLeast(1)).save(Mockito.<ContactEntity>any());
        verify(contactMapper, atLeast(1)).toTarget(Mockito.<ContactEntity>any());
        verify(contactMapper, atLeast(1)).toSource(Mockito.<Contact>any());
    }

    /**
     * Method under test: {@link ContactServiceImpl#create(UUID, Contact)}
     */
    @Test
    @DisplayName("Test creating a contact with valid data")
    void testCreate() {
        final var contactTypeUUID1 = UUID.randomUUID();
        final var contactTypeUUID2 = UUID.randomUUID();
        final var contactTypeUUID3 = UUID.randomUUID();
        final var contactUUID1 = UUID.randomUUID();
        final var contactUUID2 = UUID.randomUUID();
        final var personUUID1 = UUID.randomUUID();
        final var personUUID2 = UUID.randomUUID();
        ContactTypeEntity contactTypeEntity1 = new ContactTypeEntity();
        contactTypeEntity1.setActive(true);
        contactTypeEntity1.setDescription("The characteristics of someone or something");
        contactTypeEntity1.setId(contactTypeUUID1);
        contactTypeEntity1.setName("Name");

        PersonEntity personEntity1 = new PersonEntity();
        personEntity1.setBirthdate(LocalDate.of(1970, 1, 1));
        personEntity1.setGender("Gender");
        personEntity1.setId(personUUID1);
        personEntity1.setLastName("Doe");
        personEntity1.setMiddleName("Middle Name");
        personEntity1.setName("Name");

        ContactEntity contactEntity1 = new ContactEntity();
        contactEntity1.setActive(true);
        contactEntity1.setContactType(contactTypeEntity1);
        contactEntity1.setContent("Not all who wander are lost");
        contactEntity1.setId(contactUUID1);
        contactEntity1.setPerson(personEntity1);

        ContactType contactType1 = new ContactType();
        contactType1.setActive(true);
        contactType1.setDescription("The characteristics of someone or something");
        contactType1.setId(contactTypeUUID1);
        contactType1.setName("Name");

        Contact contact = new Contact();
        contact.setActive(true);
        contact.setContactType(contactType1);
        contact.setContent("Not all who wander are lost");
        contact.setId(contactUUID1);

        ContactTypeEntity contactTypeEntity2 = new ContactTypeEntity();
        contactTypeEntity2.setActive(true);
        contactTypeEntity2.setDescription("The characteristics of someone or something");
        contactTypeEntity2.setId(contactTypeUUID3);
        contactTypeEntity2.setName("Name");

        PersonEntity personEntity2 = new PersonEntity();
        personEntity2.setBirthdate(LocalDate.of(1970, 1, 1));
        personEntity2.setGender("Gender");
        personEntity2.setId(personUUID2);
        personEntity2.setLastName("Doe");
        personEntity2.setMiddleName("Middle Name");
        personEntity2.setName("Name");

        ContactEntity contactEntity2 = new ContactEntity();
        contactEntity2.setActive(true);
        contactEntity2.setContactType(contactTypeEntity1);
        contactEntity2.setContent("Not all who wander are lost");
        contactEntity2.setId(contactUUID2);
        contactEntity2.setPerson(personEntity2);
        var responseContact = ResponseEntity.ok();
        when(contactRepository.listByPersonId(Mockito.<UUID>any())).thenReturn(Optional.of(List.of(contactEntity1)));
        when(contactMapper.toTarget(Mockito.<ContactEntity>any())).thenReturn(contact);
        when(contactMapper.toSource(Mockito.<Contact>any())).thenReturn(contactEntity2);
        when(contactRepository.save(Mockito.<ContactEntity>any())).thenReturn(contactEntity1);

        ContactType contactType2 = new ContactType();
        contactType2.setActive(true);
        contactType2.setDescription("The characteristics of someone or something");
        contactType2.setId(contactTypeUUID2);
        contactType2.setName("Name");

        Contact contact2 = new Contact();
        contact2.setActive(true);
        contact2.setContactType(contactType2);
        contact2.setContent("Not all who wander are lost");
        contact2.setId(contactUUID2);

        when(contactRepository.listByPersonId(any(UUID.class))).thenReturn(Optional.of(List.of(contactEntity1, contactEntity2)));
        when(contactMapper.toSource(any(Contact.class))).thenReturn(contactEntity1);
        when(contactRepository.save(any(ContactEntity.class))).thenReturn(contactEntity2);
        when(contactMapper.toTarget(any(ContactEntity.class))).thenReturn(contact2);

        ResponseEntity<Contact> actualCreateResult = contactServiceImpl.create(contact2);

        assertFalse(actualCreateResult.hasBody());
        assertTrue(actualCreateResult.getHeaders().isEmpty());
        assertEquals(HttpStatus.NOT_IMPLEMENTED, actualCreateResult.getStatusCode());
        assertNotNull(responseContact);
    }

    /**
     * Method under test: {@link ContactServiceImpl#create(UUID, Contact)}
     */
    @Test
    @DisplayName("Test creating a contact with null data")
    void testCreate_bad_request() {
        ResponseEntity<Contact> actualCreateResult = contactServiceImpl.create(null);
        assertTrue(actualCreateResult.getHeaders().isEmpty());
        assertFalse(actualCreateResult.hasBody());
        assertEquals(HttpStatus.NOT_IMPLEMENTED, actualCreateResult.getStatusCode());
    }

    /**
     * Method under test: {@link ContactServiceImpl#update(UUID, Contact)}
     */
    @Test
    @DisplayName("Test updating a contact with null data")
    void testUpdate() {
        ResponseEntity<Contact> actualUpdateResult = (new ContactServiceImpl(contactRepository, contactMapper,
                new ContactTypeMapperImpl())).update((UUID) null, null);
        assertNull(actualUpdateResult.getBody());
        assertEquals(HttpStatus.NOT_FOUND, actualUpdateResult.getStatusCode());
        assertTrue(actualUpdateResult.getHeaders().isEmpty());
    }

    /**
     * Method under test: {@link ContactServiceImpl#update(UUID, Contact)}
     */
    @Test
    @DisplayName("Test updating a contact with null contact and valid contactId")
    void testUpdate2() {
        ResponseEntity<Contact> actualUpdateResult = (new ContactServiceImpl(contactRepository, contactMapper,
                new ContactTypeMapperImpl())).update(UUID.randomUUID(), null);
        assertNull(actualUpdateResult.getBody());
        assertEquals(404, actualUpdateResult.getStatusCode().value());
        assertTrue(actualUpdateResult.getHeaders().isEmpty());
    }

    /**
     * Method under test: {@link ContactServiceImpl#find(UUID)}
     */
    @Test
    @DisplayName("Test finding a contact with null contactId")
    void testFind() {
        ResponseEntity<Contact> actualFindResult = (new ContactServiceImpl(contactRepository, contactMapper,
                new ContactTypeMapperImpl())).find(null);
        assertNull(actualFindResult.getBody());
        assertEquals(400, actualFindResult.getStatusCode().value());
        assertTrue(actualFindResult.getHeaders().isEmpty());
    }

    /**
     * Method under test: {@link ContactServiceImpl#listByPersonId(UUID)}
     */
    @Test
    @DisplayName("Test listing contacts by personId with valid data")
    void testListByPersonId() {
        UUID personId = UUID.randomUUID();
        UUID contactId = UUID.randomUUID();
        var contactEntity = new ContactEntity();
        var contactTypeEntity = new ContactTypeEntity();
        var contactType = new ContactType();
        var personEntity = new PersonEntity();
        var contact = new Contact();
        personEntity.setId(personId);
        personEntity.setBirthdate(LocalDate.now());
        personEntity.setGender("M");
        personEntity.setName("Pepe");
        personEntity.setMiddleName("Chavo");
        personEntity.setLastName("Guardia");
        contactTypeEntity.setActive(true);
        contactTypeEntity.setDescription("Contact type description");
        contactTypeEntity.setName("Contact type");
        contactTypeEntity.setId(UUID.randomUUID());

        contactEntity.setActive(true);
        contactEntity.setContent("Content description");
        contactEntity.setPerson(personEntity);
        contactEntity.setId(contactId);
        contactEntity.setContactType(contactTypeEntity);

        contact.setActive(true);
        contact.setContactType(contactType);
        contact.setContent("Content description");
        contact.setId(contactId);

        contactType.setActive(true);
        contactType.setDescription("Contact type description");
        contactType.setName("Contact type");
        contactType.setId(contactTypeEntity.getId());
        Optional<List<ContactEntity>> optionalContactEntityList = Optional.of(List.of(contactEntity));
        when(contactRepository.listByPersonId(Mockito.any(UUID.class))).thenReturn(optionalContactEntityList);
        when(contactTypeMapper.toTarget(Mockito.any(ContactTypeEntity.class))).thenReturn(contactType);
        when(contactMapper.toTarget(Mockito.any(ContactEntity.class))).thenReturn(contact);

        ResponseEntity<List<Contact>> actualFindResult = contactServiceImpl.listByPersonId(personId);
        assertTrue(actualFindResult.hasBody());
        assertEquals(HttpStatus.OK, actualFindResult.getStatusCode());
        verify(contactRepository).listByPersonId(Mockito.<UUID>any());
    }

    /**
     * Method under test: {@link ContactServiceImpl#listByPersonId(UUID)}
     */
    @Test
    @DisplayName("Test listing contacts by personId with no data found")
    void testListByPersonId_NotFound() {
        UUID personId = UUID.randomUUID();
        Optional<List<ContactEntity>> optionalContactEntityList = Optional.empty();
        when(contactRepository.listByPersonId(Mockito.any(UUID.class))).thenReturn(optionalContactEntityList);
        ResponseEntity<List<Contact>> actualFindResult = contactServiceImpl.listByPersonId(personId);
        assertFalse(actualFindResult.hasBody());
        assertEquals(HttpStatus.NOT_FOUND, actualFindResult.getStatusCode());
    }

    /**
     * Method under test: {@link ContactServiceImpl#deleteById(UUID)}
     */
    @Test
    @DisplayName("Test deleting a contact by ID with valid data")
    void testDeleteById() {
        UUID personId = UUID.randomUUID();
        UUID contactId = UUID.randomUUID();
        var contactEntity = new ContactEntity();
        var contactTypeEntity = new ContactTypeEntity();
        var personEntity = new PersonEntity();
        personEntity.setId(personId);
        personEntity.setBirthdate(LocalDate.now());
        personEntity.setGender("M");
        personEntity.setName("Pepe");
        personEntity.setMiddleName("Chavo");
        personEntity.setLastName("Guardia");
        contactTypeEntity.setActive(true);
        contactTypeEntity.setDescription("Contact type description");
        contactTypeEntity.setName("Contact type");
        contactTypeEntity.setId(UUID.randomUUID());

        contactEntity.setActive(true);
        contactEntity.setContent("Content description");
        contactEntity.setPerson(personEntity);
        contactEntity.setId(contactId);
        contactEntity.setContactType(contactTypeEntity);
        when(contactRepository.findById(Mockito.any(UUID.class))).thenReturn(Optional.of(contactEntity));
        ResponseEntity<String> actualFindResult = contactServiceImpl.deleteById(personId);
        assertEquals(HttpStatus.ACCEPTED, actualFindResult.getStatusCode());
    }

    /**
     * Method under test: {@link ContactServiceImpl#deleteById(UUID)}
     */
    @Test
    @DisplayName("Test deleting a contact by ID with no data found")
    void testDeleteById_not_found() {
        var personId = UUID.randomUUID();
        when(contactRepository.findById(Mockito.any(UUID.class))).thenReturn(Optional.empty());
        ResponseEntity<String> actualFindResult = contactServiceImpl.deleteById(personId);
        assertEquals(HttpStatus.NOT_FOUND, actualFindResult.getStatusCode());
    }

    @Test
    @DisplayName("Update contact with valid data")
    void updateContactWithValidData() {
        Contact contact = new Contact();
        contact.setContent("New Content");
        contact.setActive(true);
        contact.setContactType(new ContactType());

        ContactEntity contactEntity = new ContactEntity();
        contactEntity.setId(UUID.randomUUID());
        contactEntity.setContent("Old Content");
        contactEntity.setActive(false);

        when(contactRepository.findById(any(UUID.class))).thenReturn(Optional.of(contactEntity));
        when(contactTypeMapper.toSource(any(ContactType.class))).thenReturn(new ContactTypeEntity());
        when(contactRepository.save(any(ContactEntity.class))).thenReturn(contactEntity);
        when(contactMapper.toTarget(any(ContactEntity.class))).thenReturn(contact);

        ResponseEntity<Contact> response = contactServiceImpl.update(contactEntity.getId(), contact);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("New Content", response.getBody().getContent());
    }

    @Test
    @DisplayName("Update contact with null contact")
    void updateContactWithNullContact() {
        ResponseEntity<Contact> response = contactServiceImpl.update(UUID.randomUUID(), null);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    @DisplayName("Update contact with null contactId")
    void updateContactWithNullContactId() {
        Contact contact = new Contact();
        ResponseEntity<Contact> response = contactServiceImpl.update(null, contact);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    @DisplayName("Update contact with non-existent contactId")
    void updateContactWithNonExistentContactId() {
        Contact contact = new Contact();
        when(contactRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        ResponseEntity<Contact> response = contactServiceImpl.update(UUID.randomUUID(), contact);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    @DisplayName("Find contact with valid contactId")
    void findContactWithValidContactId() {
        ContactEntity contactEntity = new ContactEntity();
        contactEntity.setId(UUID.randomUUID());
        when(contactRepository.findById(any(UUID.class))).thenReturn(Optional.of(contactEntity));
        when(contactMapper.toTarget(any(ContactEntity.class))).thenReturn(new Contact());

        ResponseEntity<Contact> response = contactServiceImpl.find(contactEntity.getId());

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    @DisplayName("Find contact with null contactId")
    void findContactWithNullContactId() {
        ResponseEntity<Contact> response = contactServiceImpl.find(null);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @DisplayName("Find contact with non-existent contactId")
    void findContactWithNonExistentContactId() {
        when(contactRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        ResponseEntity<Contact> response = contactServiceImpl.find(UUID.randomUUID());

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }
}

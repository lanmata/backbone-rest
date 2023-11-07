package com.prx.backoffice.v1.contacts.service;

import com.prx.backoffice.v1.contacts.mapper.ContactMapper;
import com.prx.backoffice.v1.contacts.mapper.ContactMapperImpl;
import com.prx.backoffice.v1.contacttypes.mapper.ContactTypeMapper;
import com.prx.backoffice.v1.contacttypes.mapper.ContactTypeMapperImpl;
import com.prx.commons.pojo.Contact;
import com.prx.commons.pojo.ContactType;
import com.prx.commons.pojo.Person;
import com.prx.persistence.general.domains.ContactEntity;
import com.prx.persistence.general.domains.ContactTypeEntity;
import com.prx.persistence.general.domains.PersonEntity;
import com.prx.persistence.general.repositories.ContactRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ContextConfiguration(classes = {ContactServiceImpl.class})
@ExtendWith(SpringExtension.class)
@TestPropertySource(properties = {"app.environments.contact.limit=5"})
class ContactServiceImplTest {
    @MockBean
    private ContactMapper contactMapper;

    @MockBean
    private ContactRepository contactRepository;

    @Autowired
    private ContactServiceImpl contactServiceImpl;

    @MockBean
    private ContactTypeMapper contactTypeMapper;

    /**
     * Method under test: {@link ContactServiceImpl#saveAll(List)}
     */
    @Test
    void testSaveAll() {
        assertTrue(contactServiceImpl.saveAll(new ArrayList<>()).isEmpty());
    }

    /**
     * Method under test: {@link ContactServiceImpl#saveAll(List)}
     */
    @Test
    void testSaveAll2() {
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
        contactType2.setId("42");
        contactType2.setName("Name");

        Person person2 = new Person();
        person2.setBirthdate(LocalDate.of(1970, 1, 1));
        person2.setFirstName("Jane");
        person2.setGender("Gender");
        person2.setId("42");
        person2.setLastName("Doe");
        person2.setMiddleName("Middle Name");

        Contact contact = new Contact();
        contact.setActive(true);
        contact.setContactType(contactType2);
        contact.setContent("Not all who wander are lost");
        contact.setId("42");
        contact.setPerson(person2);

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
        contactType4.setId("42");
        contactType4.setName("Name");

        Person person4 = new Person();
        person4.setBirthdate(LocalDate.of(1970, 1, 1));
        person4.setFirstName("Jane");
        person4.setGender("Gender");
        person4.setId("42");
        person4.setLastName("Doe");
        person4.setMiddleName("Middle Name");

        Contact contact2 = new Contact();
        contact2.setActive(true);
        contact2.setContactType(contactType4);
        contact2.setContent("Not all who wander are lost");
        contact2.setId("42");
        contact2.setPerson(person4);

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
    void testSaveAll3() {
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
        contactType2.setId("42");
        contactType2.setName("Name");

        Person person2 = new Person();
        person2.setBirthdate(LocalDate.of(1970, 1, 1));
        person2.setFirstName("Jane");
        person2.setGender("Gender");
        person2.setId("42");
        person2.setLastName("Doe");
        person2.setMiddleName("Middle Name");

        Contact contact = new Contact();
        contact.setActive(true);
        contact.setContactType(contactType2);
        contact.setContent("Not all who wander are lost");
        contact.setId("42");
        contact.setPerson(person2);

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
        contactType4.setId("42");
        contactType4.setName("Name");

        Person person4 = new Person();
        person4.setBirthdate(LocalDate.of(1970, 1, 1));
        person4.setFirstName("Jane");
        person4.setGender("Gender");
        person4.setId("42");
        person4.setLastName("Doe");
        person4.setMiddleName("Middle Name");

        Contact contact2 = new Contact();
        contact2.setActive(true);
        contact2.setContactType(contactType4);
        contact2.setContent("Not all who wander are lost");
        contact2.setId("42");
        contact2.setPerson(person4);

        ContactType contactType5 = new ContactType();
        contactType5.setActive(false);
        contactType5.setDescription("Description");
        contactType5.setId("Id");
        contactType5.setName("42");

        Person person5 = new Person();
        person5.setBirthdate(LocalDate.of(1970, 1, 1));
        person5.setFirstName("John");
        person5.setGender("42");
        person5.setId("Id");
        person5.setLastName("Smith");
        person5.setMiddleName("42");

        Contact contact3 = new Contact();
        contact3.setActive(false);
        contact3.setContactType(contactType5);
        contact3.setContent("Content");
        contact3.setId("Id");
        contact3.setPerson(person5);

        ArrayList<Contact> contacts = new ArrayList<>();
        contacts.add(contact3);
        contacts.add(contact2);
        assertEquals(2, contactServiceImpl.saveAll(contacts).size());
        verify(contactRepository, atLeast(1)).save(Mockito.<ContactEntity>any());
        verify(contactMapper, atLeast(1)).toTarget(Mockito.<ContactEntity>any());
        verify(contactMapper, atLeast(1)).toSource(Mockito.<Contact>any());
    }

    /**
     * Method under test: {@link ContactServiceImpl#create(Contact)}
     */
    @Test
    void testCreate() {
        final var contactTypeUUID1 = UUID.randomUUID();
        final var contactTypeUUID2 = UUID.randomUUID();
        final var contactTypeUUID3 = UUID.randomUUID();
        final var contactUUID1 = UUID.randomUUID();
        final var contactUUID2 = UUID.randomUUID();
        final var personUUID1 = UUID.randomUUID();
        final var personUUID2 = UUID.randomUUID();
        final var personUUID3 = UUID.randomUUID();
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
        contactType1.setId(contactTypeUUID1.toString());
        contactType1.setName("Name");

        Person person1 = new Person();
        person1.setBirthdate(LocalDate.of(1970, 1, 1));
        person1.setFirstName("Jane");
        person1.setGender("Gender");
        person1.setId(personUUID1.toString());
        person1.setLastName("Doe");
        person1.setMiddleName("Middle Name");

        Contact contact = new Contact();
        contact.setActive(true);
        contact.setContactType(contactType1);
        contact.setContent("Not all who wander are lost");
        contact.setId(contactUUID1.toString());
        contact.setPerson(person1);

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
        contactType2.setId(contactTypeUUID2.toString());
        contactType2.setName("Name");

        Person person2 = new Person();
        person2.setBirthdate(LocalDate.of(1970, 1, 1));
        person2.setFirstName("Jane");
        person2.setGender("Gender");
        person2.setId(personUUID2.toString());
        person2.setLastName("Doe");
        person2.setMiddleName("Middle Name");

        Contact contact2 = new Contact();
        contact2.setActive(true);
        contact2.setContactType(contactType2);
        contact2.setContent("Not all who wander are lost");
        contact2.setId(contactUUID2.toString());
        contact2.setPerson(person2);
        ResponseEntity<Contact> actualCreateResult = contactServiceImpl.create(contact2);
        assertTrue(actualCreateResult.hasBody());
        assertFalse(actualCreateResult.getHeaders().isEmpty());
        assertEquals(HttpStatus.CREATED, actualCreateResult.getStatusCode());
        verify(contactRepository).listByPersonId(Mockito.<UUID>any());
        verify(contactMapper, times(2)).toTarget(Mockito.<ContactEntity>any());
        verify(contactRepository).save(Mockito.<ContactEntity>any());
        verify(contactMapper).toSource(Mockito.<Contact>any());
    }

    /**
     * Method under test: {@link ContactServiceImpl#create(Contact)}
     */
    @Test
    void testCreate_bad_request() {
        ResponseEntity<Contact> actualCreateResult = contactServiceImpl.create(null);
        assertTrue(actualCreateResult.getHeaders().isEmpty());
        assertFalse(actualCreateResult.hasBody());
        assertEquals(HttpStatus.NOT_FOUND, actualCreateResult.getStatusCode());
    }

    /**
     * Method under test: {@link ContactServiceImpl#update(Contact, String)}
     */
    @Test
    void testUpdate() {
        //   Diffblue Cover was unable to write a Spring test,
        //   so wrote a non-Spring test instead.
        //   Reason: R013 No inputs found that don't throw a trivial exception.
        //   Diffblue Cover tried to run the arrange/act section, but the method under
        //   test threw
        //   java.lang.IllegalArgumentException: Invalid UUID string: 42
        //       at java.util.UUID.fromString1(UUID.java:280)
        //       at java.util.UUID.fromString(UUID.java:258)
        //       at com.prx.backoffice.v1.contacts.service.ContactServiceImpl.update(ContactServiceImpl.java:72)
        //   See https://diff.blue/R013 to resolve this issue.

        ContactRepository contactRepository = mock(ContactRepository.class);
        ContactMapperImpl contactMapper = new ContactMapperImpl();
        ResponseEntity<Contact> actualUpdateResult = (new ContactServiceImpl(contactRepository, contactMapper,
                new ContactTypeMapperImpl())).update(null, null);
        assertNull(actualUpdateResult.getBody());
        assertEquals(404, actualUpdateResult.getStatusCodeValue());
        assertTrue(actualUpdateResult.getHeaders().isEmpty());
    }

    /**
     * Method under test: {@link ContactServiceImpl#update(Contact, String)}
     */
    @Test
    void testUpdate2() {
        ContactRepository contactRepository = mock(ContactRepository.class);
        ContactMapperImpl contactMapper = new ContactMapperImpl();
        ResponseEntity<Contact> actualUpdateResult = (new ContactServiceImpl(contactRepository, contactMapper,
                new ContactTypeMapperImpl())).update(null, "foo");
        assertNull(actualUpdateResult.getBody());
        assertEquals(404, actualUpdateResult.getStatusCode().value());
        assertTrue(actualUpdateResult.getHeaders().isEmpty());
    }

    /**
     * Method under test: {@link ContactServiceImpl#find(String)}
     */
    @Test
    void testFind() {
        ContactMapperImpl contactMapper = new ContactMapperImpl();
        ResponseEntity<Contact> actualFindResult = (new ContactServiceImpl(contactRepository, contactMapper,
                new ContactTypeMapperImpl())).find(null);
        assertNull(actualFindResult.getBody());
        assertEquals(400, actualFindResult.getStatusCode().value());
        assertTrue(actualFindResult.getHeaders().isEmpty());
    }

    /**
     * Method under test: {@link ContactServiceImpl#listByPersonId(String)}
     */
    @Test
    void testListByPersonId() {
        UUID personId = UUID.randomUUID();
        UUID contactId = UUID.randomUUID();
        var contactEntity = new ContactEntity();
        var contactTypeEntity = new ContactTypeEntity();
        var contactType = new ContactType();
        var personEntity = new PersonEntity();
        var person = new Person();
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

        person.setId(personId.toString());
        person.setBirthdate(LocalDate.now());
        person.setGender("M");
        person.setFirstName("Pepe");
        person.setMiddleName("Chavo");
        person.setLastName("Guardia");

        contact.setActive(true);
        contact.setContactType(contactType);
        contact.setContent("Content description");
        contact.setPerson(person);
        contact.setId(contactId.toString());

        contactType.setActive(true);
        contactType.setDescription("Contact type description");
        contactType.setName("Contact type");
        contactType.setId(contactTypeEntity.getId().toString());
        Optional<List<ContactEntity>> optionalContactEntityList = Optional.of(List.of(contactEntity));
        when(contactRepository.listByPersonId(Mockito.any(UUID.class))).thenReturn(optionalContactEntityList);
        when(contactTypeMapper.toTarget(Mockito.any(ContactTypeEntity.class))).thenReturn(contactType);
        when(contactMapper.toTarget(Mockito.any(ContactEntity.class))).thenReturn(contact);

        ContactMapperImpl contactMapper = new ContactMapperImpl();
        ResponseEntity<List<Contact>> actualFindResult = contactServiceImpl.listByPersonId(personId.toString());
        assertTrue(actualFindResult.hasBody());
        assertEquals(HttpStatus.OK, actualFindResult.getStatusCode());
        verify(contactRepository).listByPersonId(Mockito.<UUID>any());
    }

    /**
     * Method under test: {@link ContactServiceImpl#listByPersonId(String)}
     */
    @Test
    void testListByPersonId_NotFound() {
        UUID personId = UUID.randomUUID();
        Optional<List<ContactEntity>> optionalContactEntityList = Optional.empty();
        when(contactRepository.listByPersonId(Mockito.any(UUID.class))).thenReturn(optionalContactEntityList);
        ContactMapperImpl contactMapper = new ContactMapperImpl();
        ResponseEntity<List<Contact>> actualFindResult = contactServiceImpl.listByPersonId(personId.toString());
        assertFalse(actualFindResult.hasBody());
        assertEquals(HttpStatus.NOT_FOUND, actualFindResult.getStatusCode());
    }

    /**
     * Method under test: {@link ContactServiceImpl#deleteById(String)}
     */
    @Test
    void testDeleteById() {
        UUID personId = UUID.randomUUID();
        UUID contactId = UUID.randomUUID();
        var contactEntity = new ContactEntity();
        var contactTypeEntity = new ContactTypeEntity();
        var contactType = new ContactType();
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
        ResponseEntity<String> actualFindResult = contactServiceImpl.deleteById(personId.toString());
        assertEquals(HttpStatus.ACCEPTED, actualFindResult.getStatusCode());
    }

    /**
     * Method under test: {@link ContactServiceImpl#deleteById(String)}
     */
    @Test
    void testDeleteById_not_found() {
        var personId = UUID.randomUUID();
        when(contactRepository.findById(Mockito.any(UUID.class))).thenReturn(Optional.empty());
        ResponseEntity<String> actualFindResult = contactServiceImpl.deleteById(personId.toString());
        assertEquals(HttpStatus.NOT_FOUND, actualFindResult.getStatusCode());
    }
}


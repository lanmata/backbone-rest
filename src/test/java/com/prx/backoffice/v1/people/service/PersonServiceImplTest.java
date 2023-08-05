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
package com.prx.backoffice.v1.people.service;

import com.prx.backoffice.MockLoaderBase;
import com.prx.backoffice.v1.people.mapper.PersonMapper;
import com.prx.commons.exception.StandardException;
import com.prx.commons.pojo.Person;
import com.prx.persistence.general.domains.PersonEntity;
import com.prx.persistence.general.repositories.PersonRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * PersonServiceImplTest.
 *
 * @author Luis Antonio Mata
 * @version 1.0.1.20200904-01, 06-11-2020
 */
@ContextConfiguration(classes = {PersonServiceImpl.class})
@ExtendWith(SpringExtension.class)
class PersonServiceImplTest extends MockLoaderBase {

    @Autowired
    private PersonServiceImpl personServiceImpl;

    @Mock
    PersonRepository personRepository;
    @Mock
    PersonMapper personMapper;

    @InjectMocks
    PersonServiceImpl personService;

    @BeforeEach
    void setUp() {
        try (var c = MockitoAnnotations.openMocks(this)) {
            c.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        ;
    }

    @Test
    @DisplayName("Create a person")
    void create() {
        var personEntity = new PersonEntity();
        personEntity.setId(UUID.fromString("1L"));
        personEntity.setName("Fausto");
        personEntity.setMiddleName("Joaquin");
        personEntity.setLastName("Perez");
        personEntity.setGender("M");
        personEntity.setBirthdate(LocalDate.of(1983, 12, 23));
        var person = new Person();
        person.setId("1L");
        person.setFirstName("Fausto");
        person.setMiddleName("Joaquin");
        person.setLastName("Perez");
        person.setGender("M");
        person.setBirthdate(LocalDate.of(1983, 12, 23));
        var responseEntityPerson = ResponseEntity.ok(person);
        var responseEntity = ResponseEntity.ok(personEntity);

        Mockito.doReturn(personEntity).when(personMapper).toSource(ArgumentMatchers.any(Person.class));
        Mockito.when(personRepository.save(Mockito.any(PersonEntity.class))).thenReturn(personEntity);
        Mockito.doReturn(person).when(personMapper).toTarget(ArgumentMatchers.any(PersonEntity.class));
        var response = personService.create(person);
        Assertions.assertNotNull(responseEntityPerson);
        Assertions.assertNotNull(responseEntity);
        Assertions.assertNotNull(response);
    }

    /**
     * Method under test: {@link PersonServiceImpl#create(Person)}
     */
    @Test
    void testCreate2() {
        PersonEntity personEntity = new PersonEntity();
        personEntity.setBirthdate(LocalDate.ofEpochDay(1L));
        personEntity.setGender("Gender");
        personEntity.setId(UUID.fromString("123L"));
        personEntity.setLastName("Doe");
        personEntity.setMiddleName("Middle Name");
        personEntity.setName("Name");
        when(personRepository.save((PersonEntity) any())).thenReturn(personEntity);

        Person person = new Person();
        person.setBirthdate(LocalDate.ofEpochDay(1L));
        person.setFirstName("Jane");
        person.setGender("Gender");
        person.setId("123L");
        person.setLastName("Doe");
        person.setMiddleName("Middle Name");

        PersonEntity personEntity1 = new PersonEntity();
        personEntity1.setBirthdate(LocalDate.ofEpochDay(1L));
        personEntity1.setGender("Gender");
        personEntity1.setId(UUID.fromString("123L"));
        personEntity1.setLastName("Doe");
        personEntity1.setMiddleName("Middle Name");
        personEntity1.setName("Name");
        when(personMapper.toTarget((PersonEntity) any())).thenReturn(person);
        when(personMapper.toSource((Person) any())).thenReturn(personEntity1);

        Person person1 = new Person();
        person1.setBirthdate(LocalDate.ofEpochDay(1L));
        person1.setFirstName("Jane");
        person1.setGender("Gender");
        person1.setId("123L");
        person1.setLastName("Doe");
        person1.setMiddleName("Middle Name");
        ResponseEntity<Person> actualCreateResult = personServiceImpl.create(person1);
        assertTrue(actualCreateResult.hasBody());
        assertTrue(actualCreateResult.getHeaders().isEmpty());
        assertEquals(HttpStatus.CREATED, actualCreateResult.getStatusCode());
        verify(personRepository).save((PersonEntity) any());
        verify(personMapper).toTarget((PersonEntity) any());
        verify(personMapper).toSource((Person) any());
    }

    /**
     * Method under test: {@link PersonServiceImpl#create(Person)}
     */
    @Test
    void testCreate3() {
        PersonEntity personEntity = new PersonEntity();
        personEntity.setBirthdate(LocalDate.ofEpochDay(1L));
        personEntity.setGender("Gender");
        personEntity.setId(UUID.fromString("123L"));
        personEntity.setLastName("Doe");
        personEntity.setMiddleName("Middle Name");
        personEntity.setName("Name");
        when(personRepository.save((PersonEntity) any())).thenReturn(personEntity);
        when(personMapper.toTarget((PersonEntity) any())).thenThrow(new StandardException(null));
        when(personMapper.toSource((Person) any())).thenThrow(new StandardException(null));

        Person person = new Person();
        person.setBirthdate(LocalDate.ofEpochDay(1L));
        person.setFirstName("Jane");
        person.setGender("Gender");
        person.setId("123L");
        person.setLastName("Doe");
        person.setMiddleName("Middle Name");
        assertThrows(StandardException.class, () -> personServiceImpl.create(person));
        verify(personMapper).toSource((Person) any());
    }

    @Test
    void save() {
        var personEntity = new PersonEntity();
        personEntity.setId(UUID.fromString("1L"));
        personEntity.setName("Fausto");
        personEntity.setMiddleName("Joaquin");
        personEntity.setLastName("Perez");
        personEntity.setGender("M");
        personEntity.setBirthdate(LocalDate.of(1983, 12, 23));
        Mockito.when(personRepository.save(Mockito.any(PersonEntity.class))).thenReturn(personEntity);
        var response = personRepository.save(personEntity);
        Assertions.assertNotNull(response);
    }

    /**
     * Method under test: {@link PersonServiceImpl#save(Person)}
     */
    @Test
    void testSave2() {
        PersonEntity personEntity = new PersonEntity();
        personEntity.setBirthdate(LocalDate.ofEpochDay(1L));
        personEntity.setGender("Gender");
        personEntity.setId(UUID.fromString("123L"));
        personEntity.setLastName("Doe");
        personEntity.setMiddleName("Middle Name");
        personEntity.setName("Name");
        when(personRepository.save((PersonEntity) any())).thenReturn(personEntity);

        PersonEntity personEntity1 = new PersonEntity();
        personEntity1.setBirthdate(LocalDate.ofEpochDay(1L));
        personEntity1.setGender("Gender");
        personEntity1.setId(UUID.fromString("123L"));
        personEntity1.setLastName("Doe");
        personEntity1.setMiddleName("Middle Name");
        personEntity1.setName("Name");
        when(personMapper.toSource((Person) any())).thenReturn(personEntity1);

        Person person = new Person();
        person.setBirthdate(LocalDate.ofEpochDay(1L));
        person.setFirstName("Jane");
        person.setGender("Gender");
        person.setId("123L");
        person.setLastName("Doe");
        person.setMiddleName("Middle Name");
        ResponseEntity<PersonEntity> actualSaveResult = personServiceImpl.save(person);
        assertTrue(actualSaveResult.hasBody());
        assertTrue(actualSaveResult.getHeaders().isEmpty());
        assertEquals(HttpStatus.CREATED, actualSaveResult.getStatusCode());
        verify(personRepository).save((PersonEntity) any());
        verify(personMapper).toSource((Person) any());
    }

    /**
     * Method under test: {@link PersonServiceImpl#save(Person)}
     */
    @Test
    void testSave3() {
        PersonEntity personEntity = new PersonEntity();
        personEntity.setBirthdate(LocalDate.ofEpochDay(1L));
        personEntity.setGender("Gender");
        personEntity.setId(UUID.fromString("123L"));
        personEntity.setLastName("Doe");
        personEntity.setMiddleName("Middle Name");
        personEntity.setName("Name");
        when(personRepository.save((PersonEntity) any())).thenReturn(personEntity);
        when(personMapper.toSource((Person) any())).thenThrow(new StandardException(null));

        Person person = new Person();
        person.setBirthdate(LocalDate.ofEpochDay(1L));
        person.setFirstName("Jane");
        person.setGender("Gender");
        person.setId("123L");
        person.setLastName("Doe");
        person.setMiddleName("Middle Name");
        assertThrows(StandardException.class, () -> personServiceImpl.save(person));
        verify(personMapper).toSource((Person) any());
    }

    @Test
    void find() {
    }

    @Test
    void testCreate() {
    }

    /**
     * Method under test: {@link PersonServiceImpl#update(String, Person)}
     */
    @Test
    void testUpdate() {
        PersonEntity personEntity = new PersonEntity();
        personEntity.setBirthdate(LocalDate.ofEpochDay(1L));
        personEntity.setGender("Gender");
        personEntity.setId(UUID.fromString("123L"));
        personEntity.setLastName("Doe");
        personEntity.setMiddleName("Middle Name");
        personEntity.setName("Name");
        Optional<PersonEntity> ofResult = Optional.of(personEntity);

        PersonEntity personEntity1 = new PersonEntity();
        personEntity1.setBirthdate(LocalDate.ofEpochDay(1L));
        personEntity1.setGender("Gender");
        personEntity1.setId(UUID.fromString("123L"));
        personEntity1.setLastName("Doe");
        personEntity1.setMiddleName("Middle Name");
        personEntity1.setName("Name");
        when(personRepository.save((PersonEntity) any())).thenReturn(personEntity1);
        when(personRepository.findById((UUID) any())).thenReturn(ofResult);

        PersonEntity personEntity2 = new PersonEntity();
        personEntity2.setBirthdate(LocalDate.ofEpochDay(1L));
        personEntity2.setGender("Gender");
        personEntity2.setId(UUID.fromString("123L"));
        personEntity2.setLastName("Doe");
        personEntity2.setMiddleName("Middle Name");
        personEntity2.setName("Name");

        Person person = new Person();
        person.setBirthdate(LocalDate.ofEpochDay(1L));
        person.setFirstName("Jane");
        person.setGender("Gender");
        person.setId("123L");
        person.setLastName("Doe");
        person.setMiddleName("Middle Name");
        when(personMapper.toTarget((PersonEntity) any())).thenReturn(person);
        when(personMapper.toSource((Person) any())).thenReturn(personEntity2);

        Person person1 = new Person();
        person1.setBirthdate(LocalDate.ofEpochDay(1L));
        person1.setFirstName("Jane");
        person1.setGender("Gender");
        person1.setId("123L");
        person1.setLastName("Doe");
        person1.setMiddleName("Middle Name");
        ResponseEntity<Person> actualUpdateResult = personServiceImpl.update("123L", person1);
        assertTrue(actualUpdateResult.hasBody());
        assertTrue(actualUpdateResult.getHeaders().isEmpty());
        assertEquals(HttpStatus.OK, actualUpdateResult.getStatusCode());
        verify(personRepository).save((PersonEntity) any());
        verify(personRepository).findById((UUID) any());
        verify(personMapper).toTarget((PersonEntity) any());
        verify(personMapper).toSource((Person) any());
    }

    /**
     * Method under test: {@link PersonServiceImpl#update(String, Person)}
     */
    @Test
    void testUpdate2() {
        PersonEntity personEntity = new PersonEntity();
        personEntity.setBirthdate(LocalDate.ofEpochDay(1L));
        personEntity.setGender("Gender");
        personEntity.setId(UUID.fromString("123L"));
        personEntity.setLastName("Doe");
        personEntity.setMiddleName("Middle Name");
        personEntity.setName("Name");
        Optional<PersonEntity> ofResult = Optional.of(personEntity);

        PersonEntity personEntity1 = new PersonEntity();
        personEntity1.setBirthdate(LocalDate.ofEpochDay(1L));
        personEntity1.setGender("Gender");
        personEntity1.setId(UUID.fromString("123L"));
        personEntity1.setLastName("Doe");
        personEntity1.setMiddleName("Middle Name");
        personEntity1.setName("Name");
        when(personRepository.save((PersonEntity) any())).thenReturn(personEntity1);
        when(personRepository.findById((UUID) any())).thenReturn(ofResult);

        PersonEntity personEntity2 = new PersonEntity();
        personEntity2.setBirthdate(LocalDate.ofEpochDay(1L));
        personEntity2.setGender("Gender");
        personEntity2.setId(UUID.fromString("123L"));
        personEntity2.setLastName("Doe");
        personEntity2.setMiddleName("Middle Name");
        personEntity2.setName("Name");
        when(personMapper.toTarget((PersonEntity) any())).thenThrow(new StandardException(null));
        when(personMapper.toSource((Person) any())).thenReturn(personEntity2);

        Person person = new Person();
        person.setBirthdate(LocalDate.ofEpochDay(1L));
        person.setFirstName("Jane");
        person.setGender("Gender");
        person.setId("123L");
        person.setLastName("Doe");
        person.setMiddleName("Middle Name");
        assertThrows(StandardException.class, () -> personServiceImpl.update("123L", person));
        verify(personRepository).save((PersonEntity) any());
        verify(personRepository).findById((UUID) any());
        verify(personMapper).toTarget((PersonEntity) any());
        verify(personMapper).toSource((Person) any());
    }

    /**
     * Method under test: {@link PersonServiceImpl#update(String, Person)}
     */
    @Test
    void testUpdate3() {
        PersonEntity personEntity = new PersonEntity();
        personEntity.setBirthdate(LocalDate.ofEpochDay(1L));
        personEntity.setGender("Gender");
        personEntity.setId(UUID.fromString("123L"));
        personEntity.setLastName("Doe");
        personEntity.setMiddleName("Middle Name");
        personEntity.setName("Name");
        when(personRepository.save((PersonEntity) any())).thenReturn(personEntity);
        when(personRepository.findById((UUID) any())).thenReturn(Optional.empty());

        PersonEntity personEntity1 = new PersonEntity();
        personEntity1.setBirthdate(LocalDate.ofEpochDay(1L));
        personEntity1.setGender("Gender");
        personEntity1.setId(UUID.fromString("123L"));
        personEntity1.setLastName("Doe");
        personEntity1.setMiddleName("Middle Name");
        personEntity1.setName("Name");

        Person person = new Person();
        person.setBirthdate(LocalDate.ofEpochDay(1L));
        person.setFirstName("Jane");
        person.setGender("Gender");
        person.setId("123L");
        person.setLastName("Doe");
        person.setMiddleName("Middle Name");
        when(personMapper.toTarget((PersonEntity) any())).thenReturn(person);
        when(personMapper.toSource((Person) any())).thenReturn(personEntity1);

        Person person1 = new Person();
        person1.setBirthdate(LocalDate.ofEpochDay(1L));
        person1.setFirstName("Jane");
        person1.setGender("Gender");
        person1.setId("123L");
        person1.setLastName("Doe");
        person1.setMiddleName("Middle Name");
        ResponseEntity<Person> actualUpdateResult = personServiceImpl.update("123L", person1);
        assertNull(actualUpdateResult.getBody());
        assertEquals(HttpStatus.BAD_REQUEST, actualUpdateResult.getStatusCode());
        assertEquals(1, actualUpdateResult.getHeaders().size());
        verify(personRepository).findById((UUID) any());
    }

    @Test
    void update() {
        var personEntity = new PersonEntity();
        personEntity.setId(UUID.fromString("1L"));
        personEntity.setName("Fausto");
        personEntity.setMiddleName("Joaquin");
        personEntity.setLastName("Perez");
        personEntity.setGender("M");
        personEntity.setBirthdate(LocalDate.of(1983, 12, 23));
        var person = new Person();
        person.setId("1L");
        person.setFirstName("Fausto");
        person.setMiddleName("Joaquin");
        person.setLastName("Perez");
        person.setGender("M");
        person.setBirthdate(LocalDate.of(1983, 12, 23));
        var responseEntityPerson = ResponseEntity.ok(person);
        var responseEntity = ResponseEntity.ok(personEntity);

        Mockito.doReturn(personEntity).when(personMapper).toSource(ArgumentMatchers.any(Person.class));
        Mockito.when(personRepository.save(Mockito.any(PersonEntity.class))).thenReturn(personEntity);
        Mockito.doReturn(person).when(personMapper).toTarget(ArgumentMatchers.any(PersonEntity.class));
        var response = personService.update("1L", person);
        Assertions.assertNotNull(response);
    }

    /**
     * Method under test: {@link PersonServiceImpl#delete(String, Person)}
     */
    @Test
    void testDelete() {
        Person person = new Person();
        person.setBirthdate(LocalDate.ofEpochDay(1L));
        person.setFirstName("Jane");
        person.setGender("Gender");
        person.setId("123L");
        person.setLastName("Doe");
        person.setMiddleName("Middle Name");
        assertNull(personServiceImpl.delete("123L", person));
    }

    /**
     * Method under test: {@link PersonServiceImpl#delete(String, Person)}
     */
    @Test
    void testDelete2() {
        Person person = new Person();
        doNothing().when(person).setBirthdate((LocalDate) any());
        doNothing().when(person).setFirstName((String) any());
        doNothing().when(person).setGender((String) any());
        doNothing().when(person).setId(anyString());
        doNothing().when(person).setLastName((String) any());
        doNothing().when(person).setMiddleName((String) any());
        person.setBirthdate(LocalDate.ofEpochDay(1L));
        person.setFirstName("Jane");
        person.setGender("Gender");
        person.setId("123L");
        person.setLastName("Doe");
        person.setMiddleName("Middle Name");
        assertNull(personServiceImpl.delete("123L", person));
        verify(person).setBirthdate((LocalDate) any());
        verify(person).setFirstName((String) any());
        verify(person).setGender((String) any());
        verify(person).setId(anyString());
        verify(person).setLastName((String) any());
        verify(person).setMiddleName((String) any());
    }

    /**
     * Method under test: {@link PersonServiceImpl#find(Person)}
     */
    @Test
    void testFind2() {
        PersonEntity personEntity = new PersonEntity();
        personEntity.setBirthdate(LocalDate.ofEpochDay(1L));
        personEntity.setGender("Gender");
        personEntity.setId(UUID.fromString("123L"));
        personEntity.setLastName("Doe");
        personEntity.setMiddleName("Middle Name");
        personEntity.setName("Name");
        when(personRepository.findByFirstNameMiddleNameLastName((String) any(), (String) any(), (String) any()))
                .thenReturn(personEntity);

        Person person = new Person();
        person.setBirthdate(LocalDate.ofEpochDay(1L));
        person.setFirstName("Jane");
        person.setGender("Gender");
        person.setId("123L");
        person.setLastName("Doe");
        person.setMiddleName("Middle Name");
        when(personMapper.toTarget((PersonEntity) any())).thenReturn(person);

        Person person1 = new Person();
        person1.setBirthdate(LocalDate.ofEpochDay(1L));
        person1.setFirstName("Jane");
        person1.setGender("Gender");
        person1.setId("123L");
        person1.setLastName("Doe");
        person1.setMiddleName("Middle Name");
        ResponseEntity<Person> actualFindResult = personServiceImpl.find(person1);
        assertTrue(actualFindResult.hasBody());
        assertTrue(actualFindResult.getHeaders().isEmpty());
        assertEquals(HttpStatus.FOUND, actualFindResult.getStatusCode());
        verify(personRepository).findByFirstNameMiddleNameLastName((String) any(), (String) any(), (String) any());
        verify(personMapper).toTarget((PersonEntity) any());
    }

    /**
     * Method under test: {@link PersonServiceImpl#find(Person)}
     */
    @Test
    void testFind3() {
        PersonEntity personEntity = new PersonEntity();
        personEntity.setBirthdate(LocalDate.ofEpochDay(1L));
        personEntity.setGender("Gender");
        personEntity.setId(UUID.fromString("123L"));
        personEntity.setLastName("Doe");
        personEntity.setMiddleName("Middle Name");
        personEntity.setName("Name");
        when(personRepository.findByFirstNameMiddleNameLastName((String) any(), (String) any(), (String) any()))
                .thenReturn(personEntity);
        when(personMapper.toTarget((PersonEntity) any())).thenThrow(new StandardException(null));

        Person person = new Person();
        person.setBirthdate(LocalDate.ofEpochDay(1L));
        person.setFirstName("Jane");
        person.setGender("Gender");
        person.setId("123L");
        person.setLastName("Doe");
        person.setMiddleName("Middle Name");
        assertThrows(StandardException.class, () -> personServiceImpl.find(person));
        verify(personRepository).findByFirstNameMiddleNameLastName((String) any(), (String) any(), (String) any());
        verify(personMapper).toTarget((PersonEntity) any());
    }

    /**
     * Method under test: {@link PersonServiceImpl#find(String)}
     */
    @Test
    void testFind4() {
        PersonEntity personEntity = new PersonEntity();
        personEntity.setBirthdate(LocalDate.ofEpochDay(1L));
        personEntity.setGender("Gender");
        personEntity.setId(UUID.fromString("123L"));
        personEntity.setLastName("Doe");
        personEntity.setMiddleName("Middle Name");
        personEntity.setName("Name");
        Optional<PersonEntity> ofResult = Optional.of(personEntity);
        when(personRepository.findById((UUID) any())).thenReturn(ofResult);

        Person person = new Person();
        person.setBirthdate(LocalDate.ofEpochDay(1L));
        person.setFirstName("Jane");
        person.setGender("Gender");
        person.setId("123L");
        person.setLastName("Doe");
        person.setMiddleName("Middle Name");
        when(personMapper.toTarget((PersonEntity) any())).thenReturn(person);
        ResponseEntity<Person> actualFindResult = personServiceImpl.find("12354L");
        assertTrue(actualFindResult.hasBody());
        assertTrue(actualFindResult.getHeaders().isEmpty());
        assertEquals(HttpStatus.OK, actualFindResult.getStatusCode());
        verify(personRepository).findById((UUID) any());
        verify(personMapper).toTarget((PersonEntity) any());
    }

    /**
     * Method under test: {@link PersonServiceImpl#find(String)}
     */
    @Test
    void testFind5() {
        PersonEntity personEntity = new PersonEntity();
        personEntity.setBirthdate(LocalDate.ofEpochDay(1L));
        personEntity.setGender("Gender");
        personEntity.setId(UUID.fromString("123L"));
        personEntity.setLastName("Doe");
        personEntity.setMiddleName("Middle Name");
        personEntity.setName("Name");
        Optional<PersonEntity> ofResult = Optional.of(personEntity);
        when(personRepository.findById((UUID) any())).thenReturn(ofResult);
        when(personMapper.toTarget((PersonEntity) any())).thenThrow(new StandardException(null));
        assertThrows(StandardException.class, () -> personServiceImpl.find("123L"));
        verify(personRepository).findById((UUID) any());
        verify(personMapper).toTarget((PersonEntity) any());
    }

    /**
     * Method under test: {@link PersonServiceImpl#find(String)}
     */
    @Test
    void testFind6() {
        when(personRepository.findById((UUID) any())).thenReturn(Optional.empty());

        Person person = new Person();
        person.setBirthdate(LocalDate.ofEpochDay(1L));
        person.setFirstName("Jane");
        person.setGender("Gender");
        person.setId("123L");
        person.setLastName("Doe");
        person.setMiddleName("Middle Name");
        when(personMapper.toTarget((PersonEntity) any())).thenReturn(person);
        ResponseEntity<Person> actualFindResult = personServiceImpl.find("123L");
        assertNull(actualFindResult.getBody());
        assertEquals(HttpStatus.NOT_FOUND, actualFindResult.getStatusCode());
        assertTrue(actualFindResult.getHeaders().isEmpty());
        verify(personRepository).findById((UUID) any());
    }

    /**
     * Method under test: {@link PersonServiceImpl#list(String[])}
     */
    @Test
    void testList() {
        List<PersonEntity> iterable = new ArrayList<>();
        doNothing().when(iterable).forEach((Consumer<PersonEntity>) any());
        when(personRepository.findAllById((Iterable<UUID>) any())).thenReturn(iterable);
        ResponseEntity<List<Person>> actualListResult = personServiceImpl.list("1L");
        assertNull(actualListResult.getBody());
        assertEquals(HttpStatus.NOT_FOUND, actualListResult.getStatusCode());
        assertTrue(actualListResult.getHeaders().isEmpty());
        verify(personRepository).findAllById((Iterable<UUID>) any());
        verify(iterable).forEach((Consumer<PersonEntity>) any());
    }

    /**
     * Method under test: {@link PersonServiceImpl#list(String[])}
     */
    @Test
    @Disabled("TODO: Complete this test")
    void testList2() {
        // TODO: Complete this test.
        //   Reason: R013 No inputs found that don't throw a trivial exception.
        //   Diffblue Cover tried to run the arrange/act section, but the method under
        //   test threw
        //   com.prx.commons.exception.StandardException
        //       at com.prx.backoffice.v1.person.service.PersonServiceImpl.list(PersonServiceImpl.java:96)
        //   In order to prevent list(Long[])
        //   from throwing StandardException, add constructors or factory
        //   methods that make it easier to construct fully initialized objects used in
        //   list(Long[]).
        //   See https://diff.blue/R013 to resolve this issue.

        when(personRepository.findAllById(anyList()))
                .thenReturn(null);
        new StandardException(null);
        personServiceImpl.list("1L");
    }

    /**
     * Method under test: {@link PersonServiceImpl#list(String[])}
     */
    @Test
    void testList3() {
        List<PersonEntity> iterable = new ArrayList<>();
        doNothing().when(iterable).forEach((Consumer<PersonEntity>) any());
        when(personRepository.findAll()).thenReturn(iterable);
        when(personRepository.findAllById((Iterable<UUID>) any()))
                .thenReturn(iterable);
        ResponseEntity<List<Person>> actualListResult = personServiceImpl.list();
        assertNull(actualListResult.getBody());
        assertEquals(HttpStatus.NOT_FOUND, actualListResult.getStatusCode());
        assertTrue(actualListResult.getHeaders().isEmpty());
        verify(personRepository).findAll();
        verify(iterable).forEach((Consumer<PersonEntity>) any());
    }

    /**
     * Method under test: {@link PersonServiceImpl#list(String[])}
     */
    @Test
    void testList4() {
        List<PersonEntity> personEntities = new ArrayList<>();
        List<UUID> personEntitiesId = new ArrayList<>();
        when(personRepository.findAll()).thenThrow(new StandardException(null));
        when(personRepository.findAllById(personEntitiesId)).thenReturn(personEntities);
        assertThrows(StandardException.class, () -> personServiceImpl.list());
        verify(personRepository).findAll();
    }

    @Test
    void delete() {
    }

    @Test
    void testFind() {
    }

    @Test
    void list() {
        var response = new ArrayList<PersonEntity>();
        var personEntity = new PersonEntity();
        personEntity.setId(UUID.fromString("1L"));
        personEntity.setName("Fausto");
        personEntity.setMiddleName("Joaquin");
        personEntity.setLastName("Perez");
        personEntity.setGender("M");
        personEntity.setBirthdate(LocalDate.of(1983, 12, 23));
        response.add(personEntity);
        Mockito.when(personRepository.findAll()).thenReturn(response);
        var responseResult = personRepository.findAll();
        Assertions.assertNotNull(responseResult);
    }

    @Test
    void testSave() {
    }

    @Test
    void testFind1() {
    }
}

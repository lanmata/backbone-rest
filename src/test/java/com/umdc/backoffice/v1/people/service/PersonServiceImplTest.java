/*
 *  @(#)PersonServiceImplTest.java
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

package com.umdc.backoffice.v1.people.service;

import com.umdc.backoffice.v1.people.mapper.PersonMapper;
import com.prx.commons.exception.StandardException;
import com.prx.commons.general.pojo.Person;
import com.prx.persistence.general.domains.PersonEntity;
import com.prx.persistence.general.repositories.PersonRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
class PersonServiceImplTest {

    @InjectMocks
    private PersonServiceImpl personServiceImpl;

    @Mock
    private PersonMapper personMapper;

    @Mock
    private PersonRepository personRepository;

    /**
     * Method under test: {@link PersonServiceImpl#update(UUID, Person)}
     */
    @Test
    @DisplayName("Test update method with valid person and existing person entity")
    void testUpdate() {
        final var personId = UUID.randomUUID();
        Person person = mock(Person.class);
        doNothing().when(person).setBirthdate(Mockito.<LocalDate>any());
        doNothing().when(person).setFirstName(Mockito.<String>any());
        doNothing().when(person).setGender(Mockito.<String>any());
        doNothing().when(person).setId(Mockito.<UUID>any());
        doNothing().when(person).setLastName(Mockito.<String>any());
        doNothing().when(person).setMiddleName(Mockito.<String>any());
        person.setBirthdate(LocalDate.of(1970, 1, 1));
        person.setFirstName("Jane");
        person.setGender("M");
        person.setLastName("Doe");
        person.setMiddleName("Middle Name");

        PersonEntity personEntity = new PersonEntity();
        personEntity.setBirthdate(LocalDate.of(1970, 1, 1));
        personEntity.setGender("Gender");
        personEntity.setId(personId);
        personEntity.setLastName("Doe");
        personEntity.setMiddleName("Middle Name");
        personEntity.setName("Name");
        when(personRepository.findById(Mockito.<UUID>any())).thenReturn(Optional.of(personEntity));
        when(personMapper.toSource(Mockito.<Person>any())).thenReturn(personEntity);
        when(personMapper.toTarget(Mockito.<PersonEntity>any())).thenReturn(person);
        when(personRepository.save(Mockito.<PersonEntity>any())).thenReturn(personEntity);

        final var response = personServiceImpl.update(personId, person);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(personRepository).findById(Mockito.<UUID>any());
        verify(personMapper).toSource(Mockito.<Person>any());
        verify(personMapper).toTarget(Mockito.<PersonEntity>any());
        verify(personRepository).save(Mockito.<PersonEntity>any());
    }

    /**
     * Method under test: {@link PersonServiceImpl#update(UUID, Person)}
     */
    @Test
    void testUpdate2() {
        final var personId = UUID.randomUUID();
        Person person = mock(Person.class);
        doNothing().when(person).setBirthdate(Mockito.<LocalDate>any());
        doNothing().when(person).setFirstName(Mockito.<String>any());
        doNothing().when(person).setGender(Mockito.<String>any());
        doNothing().when(person).setId(Mockito.<UUID>any());
        doNothing().when(person).setLastName(Mockito.<String>any());
        doNothing().when(person).setMiddleName(Mockito.<String>any());
        person.setBirthdate(LocalDate.of(1970, 1, 1));
        person.setFirstName("Jane");
        person.setGender("M");
        person.setLastName("Doe");
        person.setMiddleName("Middle Name");

        when(personRepository.findById(Mockito.<UUID>any())).thenReturn(Optional.empty());

        final var response = personServiceImpl.update(personId, person);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(personRepository).findById(Mockito.<UUID>any());
    }

    /**
     * Method under test: {@link PersonServiceImpl#update(UUID, Person)}
     */
    @Test
    void testUpdate3() {
        Person person = mock(Person.class);
        doNothing().when(person).setBirthdate(Mockito.<LocalDate>any());
        doNothing().when(person).setFirstName(Mockito.<String>any());
        doNothing().when(person).setGender(Mockito.<String>any());
        doNothing().when(person).setId(Mockito.<UUID>any());
        doNothing().when(person).setLastName(Mockito.<String>any());
        doNothing().when(person).setMiddleName(Mockito.<String>any());
        person.setBirthdate(LocalDate.of(1970, 1, 1));
        person.setFirstName("Jane");
        person.setGender("M");
        person.setLastName("Doe");
        person.setMiddleName("Middle Name");

        when(personRepository.findById(Mockito.<UUID>any())).thenReturn(Optional.empty());

        final var response = personServiceImpl.update(null, person);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    /**
     * Method under test: {@link PersonServiceImpl#update(UUID, Person)}
     */
    @Test
    void testUpdate4() {
        final var personId = UUID.randomUUID();
        when(personRepository.findById(Mockito.<UUID>any())).thenReturn(Optional.empty());
        final var response = personServiceImpl.update(personId, null);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    /**
     * Method under test: {@link PersonServiceImpl#find(UUID)}
     */
    @Test
    void testFind() {
        final var personId = UUID.randomUUID();
        PersonEntity personEntity = new PersonEntity();
        personEntity.setBirthdate(LocalDate.of(1970, 1, 1));
        personEntity.setGender("Gender");
        personEntity.setId(personId);
        personEntity.setLastName("Doe");
        personEntity.setMiddleName("Middle Name");
        personEntity.setName("Name");
        Person person = new Person();
        person.setGender("Gender");
        person.setId(personId);
        person.setLastName("Doe");
        person.setMiddleName("Middle Name");
        person.setFirstName("Name");
        person.setBirthdate(LocalDate.of(1970, 1, 1));
        when(personRepository.findById(Mockito.<UUID>any())).thenReturn(Optional.of(personEntity));
        when(personMapper.toTarget(Mockito.<PersonEntity>any())).thenReturn(person);
        final var result = personServiceImpl.find(personId);
        assertNotNull(result);
        assertNotNull(result.getBody());
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals("Gender", personEntity.getGender());
        assertEquals("Doe", personEntity.getLastName());
        assertEquals("Middle Name", personEntity.getMiddleName());
        assertEquals("Name", personEntity.getName());
        verify(personRepository).findById(Mockito.<UUID>any());
        verify(personMapper).toTarget(Mockito.<PersonEntity>any());
    }

    /**
     * Method under test: {@link PersonServiceImpl#find(UUID)}
     */
    @Test
    void testFind_not_found() {
        final var personId = UUID.randomUUID();
        PersonEntity personEntity = new PersonEntity();
        personEntity.setBirthdate(LocalDate.of(1970, 1, 1));
        personEntity.setGender("Gender");
        personEntity.setId(personId);
        personEntity.setLastName("Doe");
        personEntity.setMiddleName("Middle Name");
        personEntity.setName("Name");
        when(personRepository.findById(Mockito.<UUID>any())).thenReturn(Optional.empty());
        final var result = personServiceImpl.find(personId);
        assertNotNull(result);
        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
        verify(personRepository).findById(Mockito.<UUID>any());
    }

    /**
     * Method under test: {@link PersonServiceImpl#find(UUID)}
     */
    @Test
    void testFind_null_parameter() {
        final var result = personServiceImpl.find((UUID) null);
        assertNotNull(result);
        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, result.getStatusCode());
    }

    /**
     * Method under test: {@link PersonServiceImpl#create(Person)}
     */
    @Test
    void testCreate() {
        PersonEntity personEntity = new PersonEntity();
        personEntity.setBirthdate(LocalDate.of(1970, 1, 1));
        personEntity.setGender("Gender");
        personEntity.setId(UUID.randomUUID());
        personEntity.setLastName("Doe");
        personEntity.setMiddleName("Middle Name");
        personEntity.setName("Name");
        when(personRepository.save(Mockito.<PersonEntity>any())).thenReturn(personEntity);

        PersonEntity personEntity2 = new PersonEntity();
        personEntity2.setBirthdate(LocalDate.of(1970, 1, 1));
        personEntity2.setGender("Gender");
        personEntity2.setId(UUID.randomUUID());
        personEntity2.setLastName("Doe");
        personEntity2.setMiddleName("Middle Name");
        personEntity2.setName("Name");
        when(personMapper.toSource(Mockito.<Person>any())).thenReturn(personEntity2);

        Person person = new Person();
        person.setBirthdate(LocalDate.of(1970, 1, 1));
        person.setFirstName("Jane");
        person.setGender("Gender");
        person.setLastName("Doe");
        person.setMiddleName("Middle Name");
        when(personMapper.toTarget(Mockito.<PersonEntity>any())).thenReturn(person);
        ResponseEntity<Person> actualSaveResult = personServiceImpl.create(person);
        assertTrue(actualSaveResult.hasBody());
        assertTrue(actualSaveResult.getHeaders().isEmpty());
        assertEquals(HttpStatus.CREATED, actualSaveResult.getStatusCode());
        verify(personRepository).save(Mockito.<PersonEntity>any());
        verify(personMapper).toSource(Mockito.<Person>any());
        verify(personMapper).toTarget(Mockito.<PersonEntity>any());
    }

    /**
     * Method under test: {@link PersonServiceImpl#create(Person)}
     */
    @Test
    void testCreate2() {
        when(personMapper.toSource(Mockito.<Person>any())).thenThrow(new StandardException(null));

        Person person = new Person();
        person.setBirthdate(LocalDate.of(1970, 1, 1));
        person.setFirstName("Jane");
        person.setGender("Gender");
        person.setLastName("Doe");
        person.setMiddleName("Middle Name");
        assertThrows(StandardException.class, () -> personServiceImpl.create(person));
        verify(personMapper).toSource(Mockito.<Person>any());
    }

    @Test
    void testCreate3() {
        ResponseEntity<Person> actualSaveResult = personServiceImpl.create(null);
        assertFalse(actualSaveResult.hasBody());
        assertTrue(actualSaveResult.getHeaders().isEmpty());
        assertEquals(HttpStatus.NOT_FOUND, actualSaveResult.getStatusCode());
    }

    @Test
    @DisplayName("List all persons when no IDs are provided")
    void listAllPersonsWhenNoIdsProvided() {
        var personEntity1 = getPersonEntity();
        var personEntity2 = getPersonEntity();
        var person = getPerson();
        personEntity2.setId(UUID.randomUUID());
        person.setId(UUID.randomUUID());

        List<PersonEntity> personEntities = List.of(personEntity1, personEntity2 );
        when(personRepository.findAll()).thenReturn(personEntities);
        when(personMapper.toTarget(any(PersonEntity.class))).thenReturn(person);

        ResponseEntity<List<Person>> response = personServiceImpl.list();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
    }

    @Test
    @DisplayName("List persons by IDs")
    void listPersonsByIds() {
        List<PersonEntity> personEntities = List.of(getPersonEntity());
        when(personRepository.findAllById(anyList())).thenReturn(personEntities);
        when(personMapper.toTarget(any(PersonEntity.class))).thenReturn(getPerson());

        ResponseEntity<List<Person>> response = personServiceImpl.list(UUID.randomUUID(), UUID.randomUUID());

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
    }

    @Test
    @DisplayName("List persons with empty result")
    void listPersonsWithEmptyResult() {
        when(personRepository.findAll()).thenReturn(List.of(getPersonEntity()));

        ResponseEntity<List<Person>> response = personServiceImpl.list(UUID.randomUUID());

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    private Person getPerson() {
        Person person = new Person();
        person.setBirthdate(LocalDate.of(1970, 1, 1));
        person.setFirstName("Jane");
        person.setGender("Gender");
        person.setLastName("Doe");
        person.setMiddleName("Middle Name");

        return person;
    }

    private PersonEntity getPersonEntity() {
        PersonEntity personEntity = new PersonEntity();
        personEntity.setBirthdate(LocalDate.of(1970, 1, 1));
        personEntity.setGender("Gender");
        personEntity.setId(UUID.randomUUID());
        personEntity.setLastName("Doe");
        personEntity.setMiddleName("Middle Name");
        personEntity.setName("Name");

        return personEntity;
    }
}


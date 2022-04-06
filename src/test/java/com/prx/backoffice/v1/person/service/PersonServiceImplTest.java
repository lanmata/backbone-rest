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
package com.prx.backoffice.v1.person.service;

import com.prx.backoffice.MockLoaderBase;
import com.prx.backoffice.v1.person.mapper.PersonMapper;
import com.prx.commons.pojo.Person;
import com.prx.persistence.general.domains.PersonEntity;
import com.prx.persistence.general.repositories.PersonRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.ArrayList;

/**
 * PersonServiceImplTest.
 *
 * @author Luis Antonio Mata
 * @version 1.0.1.20200904-01, 06-11-2020
 */
class PersonServiceImplTest extends MockLoaderBase {

    @Mock
    PersonRepository personRepository;
    @Mock
    PersonMapper personMapper;

    @InjectMocks
    PersonServiceImpl personService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void create() {
        var personEntity = new PersonEntity();
        personEntity.setId(1L);
        personEntity.setName("Fausto");
        personEntity.setMiddleName("Joaquin");
        personEntity.setLastName("Perez");
        personEntity.setGender("M");
        personEntity.setBirthdate(LocalDate.of(1983, 12,23));
        var person = new Person();
        person.setId(1L);
        person.setFirstName("Fausto");
        person.setMiddleName("Joaquin");
        person.setLastName("Perez");
        person.setGender("M");
        person.setBirthdate(LocalDate.of(1983, 12,23));
        var responseEntityPerson = ResponseEntity.ok(person);
        var responseEntity = ResponseEntity.ok(personEntity);

        Mockito.doReturn(personEntity).when(personMapper).toSource(ArgumentMatchers.any(Person.class));
        Mockito.when(personRepository.save(Mockito.any(PersonEntity.class))).thenReturn(personEntity);
        Mockito.doReturn(person).when(personMapper).toTarget(ArgumentMatchers.any(PersonEntity.class));
        var response = personService.create(person);
        Assertions.assertNotNull(response);
    }

    @Test
    void save() {
        var personEntity = new PersonEntity();
        personEntity.setId(1L);
        personEntity.setName("Fausto");
        personEntity.setMiddleName("Joaquin");
        personEntity.setLastName("Perez");
        personEntity.setGender("M");
        personEntity.setBirthdate(LocalDate.of(1983, 12,23));
        Mockito.when(personRepository.save(Mockito.any(PersonEntity.class))).thenReturn(personEntity);
        var response = personRepository.save(personEntity);
        Assertions.assertNotNull(response);
    }

    @Test
    void find() {
    }

    @Test
    void testCreate() {
    }

    @Test
    void update() {
        var personEntity = new PersonEntity();
        personEntity.setId(1L);
        personEntity.setName("Fausto");
        personEntity.setMiddleName("Joaquin");
        personEntity.setLastName("Perez");
        personEntity.setGender("M");
        personEntity.setBirthdate(LocalDate.of(1983, 12,23));
        var person = new Person();
        person.setId(1L);
        person.setFirstName("Fausto");
        person.setMiddleName("Joaquin");
        person.setLastName("Perez");
        person.setGender("M");
        person.setBirthdate(LocalDate.of(1983, 12,23));
        var responseEntityPerson = ResponseEntity.ok(person);
        var responseEntity = ResponseEntity.ok(personEntity);

        Mockito.doReturn(personEntity).when(personMapper).toSource(ArgumentMatchers.any(Person.class));
        Mockito.when(personRepository.save(Mockito.any(PersonEntity.class))).thenReturn(personEntity);
        Mockito.doReturn(person).when(personMapper).toTarget(ArgumentMatchers.any(PersonEntity.class));
        var response = personService.update(1L, person);
        Assertions.assertNotNull(response);
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
        personEntity.setId(1L);
        personEntity.setName("Fausto");
        personEntity.setMiddleName("Joaquin");
        personEntity.setLastName("Perez");
        personEntity.setGender("M");
        personEntity.setBirthdate(LocalDate.of(1983, 12,23));
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

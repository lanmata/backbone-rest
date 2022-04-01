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
import com.prx.persistence.general.domains.PersonEntity;
import com.prx.persistence.general.repositories.PersonRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;

/**
 * PersonServiceImplTest.
 *
 * @author Luis Antonio Mata
 * @version 1.0.1.20200904-01, 06-11-2020
 */
class PersonServiceImplTest extends MockLoaderBase {

    @Mock
    PersonRepository personRepository;

    @MockBean
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

        Mockito.when(personRepository.save(Mockito.any(PersonEntity.class))).thenReturn(personEntity);
        var response = personRepository.save(personEntity);
        Assertions.assertNotNull(response);
    }

    @Test
    void save() {
    }

    @Test
    void find() {
    }

    @Test
    void testCreate() {
    }

    @Test
    void update() {
    }

    @Test
    void delete() {
    }

    @Test
    void testFind() {
    }

    @Test
    void list() {
    }

    @Test
    void testSave() {
    }

    @Test
    void testFind1() {
    }
}

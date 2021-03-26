/*
 *
 *  * @(#)PersonControllerTest.java.
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

package com.prx.backoffice.controller;

import com.prx.backoffice.MockLoaderBase;
import com.prx.backoffice.enums.keys.PersonMessageKey;
import com.prx.backoffice.service.PersonService;
import com.prx.backoffice.to.person.PersonCreateRequest;
import com.prx.commons.pojo.MessageActivity;
import com.prx.commons.pojo.Person;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.time.LocalDate;

/**
 * PersonControllerTest.
 *
 * @author Luis Antonio Mata
 * @version 1.0.1.20200904-01, 06-11-2020
 */
public class PersonControllerTest extends MockLoaderBase {
    /** personService */
    @Mock
    private PersonService personService;
    /** personController */
    @InjectMocks
    private PersonController personController;

    /** setup */
    @BeforeEach
    public void setup() throws Exception {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testCreate() {
//        final var personCreateRequest = new PersonCreateRequest();
//        final var person = new Person();
//        person.setBirthdate(LocalDate.of(1979,4,14));
//        person.setFirstName("Pepe");
//        person.setGender("M");
//        person.setId(1);
//        person.setLastName("Perez");
//        person.setMiddleName("Peter");
//        personCreateRequest.setPerson(person);
//        final var response = new MessageActivity<Person>();
//        response.setObjectResponse(person);
//        response.setCode(PersonMessageKey.PERSON_CREATED.getCode());
//        response.setMessage(PersonMessageKey.PERSON_CREATED.getStatus());
//
//        Mockito.when(this.personService.create(ArgumentMatchers.any())).thenReturn(response);
//        Assertions.assertNotNull(this.personController.create(personCreateRequest));
    }

}
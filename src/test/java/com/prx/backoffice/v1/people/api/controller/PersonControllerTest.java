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

package com.prx.backoffice.v1.people.api.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.prx.backoffice.MockLoaderBase;
import com.prx.backoffice.v1.people.api.to.PersonRequest;
import com.prx.backoffice.v1.people.mapper.PersonMapperImpl;
import com.prx.backoffice.v1.people.service.PersonService;
import com.prx.backoffice.v1.people.service.PersonServiceImpl;
import com.prx.commons.pojo.Person;
import com.prx.persistence.general.repositories.PersonRepository;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDate;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * PersonControllerTest.
 *
 * @author Luis Antonio Mata
 * @version 1.0.1.20200904-01, 06-11-2020
 */
@SpringBootTest
@ActiveProfiles("local")
class PersonControllerTest extends MockLoaderBase {

    @Autowired
    WebApplicationContext applicationContext;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    PersonService personService;

    @MockBean
    PersonRepository personRepository;

    private static final String PATH;

    static {
        PATH = "/v1/people/";
    }

    @BeforeEach
    public void setup() {
        RestAssuredMockMvc.webAppContextSetup(applicationContext);
    }


    @Test
    void testCreate() throws JsonProcessingException {
        final var personRequest = new PersonRequest();
        personRequest.setPerson(getPerson());

        final var response = ResponseEntity.status(HttpStatus.CREATED).body(personRequest.getPerson());
        //when:
        when(personService.create(Mockito.<Person>any())).thenReturn(response);
        //then:
        given().contentType(MediaType.APPLICATION_JSON_VALUE).body(objectMapper.writeValueAsString(personRequest))
                .accept(MediaType.APPLICATION_JSON_VALUE).when().post(PATH).then().assertThat()
                .statusCode(HttpStatus.CREATED.value()).expect(MvcResult::getResponse);
    }

    @Test
    void testCreate_not_found() throws JsonProcessingException {
        final var personRequest = new PersonRequest();
        personRequest.setPerson(null);

        //when:
        when(personService.create(Mockito.<Person>any())).thenReturn(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
        //then:
        given().contentType(MediaType.APPLICATION_JSON_VALUE).body(objectMapper.writeValueAsString(personRequest))
                .accept(MediaType.APPLICATION_JSON_VALUE).when().post(PATH).then().assertThat()
                .statusCode(HttpStatus.NOT_FOUND.value()).expect(MvcResult::getResponse);
    }

    /**
     * Method under test: {@link PersonController#update(String, PersonRequest)}
     */
    @Test
    void testUpdate() throws JsonProcessingException {
        PersonController personController = new PersonController(
                new PersonServiceImpl(personRepository, new PersonMapperImpl()));

        Person person = new Person();
        person.setBirthdate(LocalDate.of(1970, 1, 1));
        person.setFirstName("Jane");
        person.setGender("Gender");
        person.setLastName("Doe");
        person.setMiddleName("Middle Name");

        final var personRequest = new PersonRequest();
        personRequest.setAppName("App Name");
        personRequest.setAppToken("ABC123");
        personRequest.setDateTime(LocalDate.of(1970, 1, 1).atStartOfDay());
        personRequest.setPerson(person);
        personRequest.setPerson(null);

        //when:
        when(personService.update(anyString(), Mockito.<Person>any())).thenReturn(ResponseEntity.status(HttpStatus.OK).build());
        //then:
        given().contentType(MediaType.APPLICATION_JSON_VALUE).body(objectMapper.writeValueAsString(personRequest))
                .accept(MediaType.APPLICATION_JSON_VALUE).when().put(PATH.concat("610a376a-aa19-4e0d-ad0b-4536457522f2")).then().assertThat()
                .statusCode(HttpStatus.OK.value()).expect(MvcResult::getResponse);
    }

    /**
     * Method under test: {@link PersonController#update(String, PersonRequest)}
     */
    @Test
    void testUpdate2() throws JsonProcessingException {
        Person person = new Person();
        person.setBirthdate(LocalDate.of(1970, 1, 1));
        person.setFirstName("Jane");
        person.setGender("Gender");
        person.setLastName("Doe");
        person.setMiddleName("Middle Name");

        final var personRequest = new PersonRequest();
        personRequest.setAppName("App Name");
        personRequest.setAppToken("ABC123");
        personRequest.setDateTime(LocalDate.of(1970, 1, 1).atStartOfDay());
        personRequest.setPerson(person);
        personRequest.setPerson(null);

        //when:
        when(personService.update(anyString(), Mockito.<Person>any())).thenReturn(ResponseEntity.status(HttpStatus.BAD_REQUEST).build());
        //then:
        given().contentType(MediaType.APPLICATION_JSON_VALUE).body(objectMapper.writeValueAsString(personRequest))
                .accept(MediaType.APPLICATION_JSON_VALUE).when().put(PATH.concat("610a376a-aa19-4e0d-ad0b-4536457522f2")).then().assertThat()
                .statusCode(HttpStatus.BAD_REQUEST.value()).expect(MvcResult::getResponse);
    }

    private static Person getPerson() {
        final var person = new Person();
        person.setBirthdate(LocalDate.of(1984, 5, 27));
        person.setGender("F");
        person.setFirstName("Jenna");
        person.setMiddleName("Rylee");
        person.setLastName("Batty");

        return person;
    }

}

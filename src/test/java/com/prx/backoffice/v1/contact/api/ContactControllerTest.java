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

package com.prx.backoffice.v1.contact.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.prx.backoffice.MockLoaderBase;
import com.prx.backoffice.util.ConstantUtilTest;
import com.prx.backoffice.v1.contact.service.ContactServiceImpl;
import com.prx.backoffice.v1.contact.to.ContactRequest;
import com.prx.commons.pojo.Contact;
import com.prx.commons.pojo.ContactType;
import com.prx.commons.pojo.Person;
import io.restassured.module.mockmvc.specification.MockMvcRequestSpecification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigInteger;
import java.time.LocalDate;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.apache.http.HttpHeaders.CONTENT_TYPE;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * ContactControllerTest.
 *
 * @author Luis Antonio Mata
 * @version 1.0.0, 23-04-2022
 * @since 11
 */
class ContactControllerTest extends MockLoaderBase {

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    ContactServiceImpl contactService;

    private static final String PATH_CREATE;

    private MockMvcRequestSpecification mockMvcRequestSpecification;

    static {
        PATH_CREATE = "/v1/contact/";
    }

    @BeforeEach
    void setUp() {
        mockMvcRequestSpecification = given().header(CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
    }

    @Test
    @DisplayName("Create a new contact.")
    void create() throws JsonProcessingException {
        final var contactRequest = getContactRequest();
        final var response = ResponseEntity.status(HttpStatus.CREATED).body(contactRequest.getContact());
        //When:
        Mockito.when(contactService.create(Mockito.any(ContactRequest.class))).thenReturn(response);
        //Then:
        given().contentType(MediaType.APPLICATION_JSON_VALUE).body(objectMapper.writeValueAsString(contactRequest))
                .accept(MediaType.APPLICATION_JSON_VALUE).when().post(PATH_CREATE).then().assertThat()
                .statusCode(HttpStatus.CREATED.value()).expect(MvcResult::getResponse);
    }

    @Test
    @DisplayName("Fail to create a new contact.")
    void create_fail() throws JsonProcessingException {
        final var contactRequest = getContactRequest();
        final var response = ResponseEntity.status(HttpStatus.NOT_FOUND).body(contactRequest.getContact());
        //When:
        Mockito.when(contactService.create(Mockito.any(ContactRequest.class))).thenReturn(response);
        //Then:
        given().contentType(MediaType.APPLICATION_JSON_VALUE).body(objectMapper.writeValueAsString(contactRequest))
                .accept(MediaType.APPLICATION_JSON_VALUE).when().post(PATH_CREATE).then().assertThat()
                .statusCode(HttpStatus.NOT_FOUND.value()).expect(MvcResult::getResponse);
    }

    @Test
    @DisplayName("update a contact.")
    void update() throws JsonProcessingException {
        final var contact = getContact();
        final var response = ResponseEntity.status(HttpStatus.OK).body(contact);
        //When:
        Mockito.when(contactService.create(Mockito.any(ContactRequest.class))).thenReturn(response);
        //Then:
        given().contentType(MediaType.APPLICATION_JSON_VALUE).body(objectMapper.writeValueAsString(contact))
                .accept(MediaType.APPLICATION_JSON_VALUE).when().put(PATH_CREATE.concat("/" + BigInteger.valueOf(1)))
                .then().assertThat().statusCode(HttpStatus.OK.value()).expect(MvcResult::getResponse);
    }

    @Test
    @DisplayName("Find a contact.")
    void find() {
        //when:
        var response = mockMvcRequestSpecification.get(PATH_CREATE.concat("5"));
        // then:
        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
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

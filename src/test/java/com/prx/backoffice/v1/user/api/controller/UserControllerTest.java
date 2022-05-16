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

package com.prx.backoffice.v1.user.api.controller;

import com.prx.backoffice.MockLoaderBase;
import com.prx.backoffice.v1.person.service.PersonService;
import com.prx.backoffice.v1.user.service.UserService;
import com.prx.backoffice.v1.util.UserTemplateTest;
import io.restassured.module.mockmvc.specification.MockMvcRequestSpecification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.apache.http.HttpHeaders.CONTENT_TYPE;

/**
 * UserControllerTest.
 *
 * @author <a href='mailto:luis.antonio.mata@gmail.com'>Luis Antonio Mata</a>
 * @version 1.0.0, 26-03-2021
 */
class UserControllerTest extends MockLoaderBase {

    private static final String PATH;
    private static final String FIND_ALL_OP;
    private static final String LOGIN_OP;
    private static final String FIND_BY_ALIAS_OP;

    @Mock
    UserService userService;

    @Mock
    PersonService personService;

    @InjectMocks
    UserController userController;

    private MockMvcRequestSpecification mockMvcRequestSpecification;


    static {
        PATH = "/v1/user/";
        FIND_ALL_OP = "findAll";
        FIND_BY_ALIAS_OP = "findByAlias/";
        LOGIN_OP = "login";
    }

    @BeforeEach
    void setUp() {
        mockMvcRequestSpecification = given().header(CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void find() {
        given().contentType(MediaType.APPLICATION_JSON_VALUE).accept(MediaType.APPLICATION_JSON_VALUE)
                .when().get(PATH.concat("ABS125/12")).then().assertThat().statusCode(HttpStatus.OK.value())
                .expect(MvcResult::getResponse);
    }

    @Test
    void findAll() {
        given().contentType(MediaType.APPLICATION_JSON_VALUE).accept(MediaType.APPLICATION_JSON_VALUE).when()
                .get(PATH.concat(FIND_ALL_OP)).then().assertThat().statusCode(HttpStatus.OK.value()).expect(MvcResult::getResponse);
    }

    @Test
    void login() {
        given().contentType(MediaType.APPLICATION_JSON_VALUE).body(UserTemplateTest.USER.getUserAccessRequest())
                .accept(MediaType.APPLICATION_JSON_VALUE).when().post(PATH.concat(LOGIN_OP)).then().assertThat()
                .statusCode(HttpStatus.ACCEPTED.value()).expect(MvcResult::getResponse);
    }

    @Test
    void create() {
//        final var userTo = UserTemplateTest.USER.getModel();
//        final var response = ResponseEntity.status(HttpStatus.CREATED).body(userTo);
//        MockHttpServletRequest request = new MockHttpServletRequest();
//        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
//        then:
//        given().contentType(MediaType.APPLICATION_JSON_VALUE).body(UserTemplateTest.USER.getModel())
//                .accept(MediaType.APPLICATION_JSON_VALUE).when().post(PATH_CREATE).then().assertThat()
//                .statusCode(HttpStatus.CREATED.value()).expect(MvcResult::getResponse);
    }

    @Test
    void findByAlias() {
        given().contentType(MediaType.APPLICATION_JSON_VALUE).accept(MediaType.APPLICATION_JSON_VALUE).when()
                .get(PATH.concat(FIND_BY_ALIAS_OP).concat("pperez")).then().assertThat()
                .statusCode(HttpStatus.OK.value()).expect(MvcResult::getResponse);
    }

}

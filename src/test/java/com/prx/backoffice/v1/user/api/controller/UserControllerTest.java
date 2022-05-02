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
import com.prx.backoffice.v1.user.api.to.UserAccessRequest;
import com.prx.backoffice.v1.user.api.to.UserCreateRequest;
import com.prx.backoffice.v1.user.service.UserService;
import com.prx.commons.pojo.Feature;
import com.prx.commons.pojo.Person;
import com.prx.commons.pojo.Role;
import com.prx.commons.pojo.User;
import io.restassured.module.mockmvc.specification.MockMvcRequestSpecification;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MvcResult;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;

import static com.prx.backoffice.util.ConstantUtilTest.APP_NAME_VALUE;
import static com.prx.backoffice.util.ConstantUtilTest.APP_TOKEN_VALUE;
import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.apache.http.HttpHeaders.CONTENT_TYPE;

/**
 * UserControllerTest.
 *
 * @author <a href='mailto:luis.antonio.mata@gmail.com'>Luis Antonio Mata</a>
 * @version 1.0.0, 26-03-2021
 */
class UserControllerTest extends MockLoaderBase {

    private static final String PATH_CREATE;

    @Mock
    UserService userService;

    @InjectMocks
    UserController userController;

    private MockMvcRequestSpecification mockMvcRequestSpecification;

    static {
        PATH_CREATE = "/v1/user/";
    }

    @BeforeEach
    void setUp() {
        mockMvcRequestSpecification = given().header(CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
    }

    @Test
    void find() {
        Mockito.when(this.userService.findUserById(ArgumentMatchers.anyLong()))
                .thenReturn(ResponseEntity.status(HttpStatus.OK).build());
        Assertions.assertNotNull(this.userController.find( 12L));
    }

    @Test
    void findAll() {
        var users = new ArrayList<User>();
        users.add(getUser());
        Mockito.when(this.userService.findAll()).thenReturn(ResponseEntity.ok(users));
        Assertions.assertNotNull(this.userController.findAll());
    }

    @Test
    void login() {
        final var userAccessRequest = new UserAccessRequest();

        ResponseEntity.ok().build();
        userAccessRequest.setAlias("pepe");
        userAccessRequest.setPassword("123456789");
        userAccessRequest.setDateTime(LocalDateTime.now(ZoneId.systemDefault()));
        userAccessRequest.setAppName("TEST-APP");
        userAccessRequest.setAppToken("TEST-APP/00252336");
        Mockito.when(this.userService.access(ArgumentMatchers.anyString(),ArgumentMatchers.anyString()))
                .thenReturn(ResponseEntity.ok().build());
        Assertions.assertNotNull(this.userController.login(userAccessRequest));
    }

    @Test
    void create() {
        final var response = ResponseEntity.status(HttpStatus.CREATED).body(getUser());
        //when:
        Mockito.when(this.userService.create(ArgumentMatchers.any(User.class))).thenReturn(ResponseEntity.ok(getUser()));
        //then:
        given().contentType(MediaType.APPLICATION_JSON_VALUE).body(getUserCreateRequest())
                .accept(MediaType.APPLICATION_JSON_VALUE).when().post(PATH_CREATE).then().assertThat()
                .statusCode(HttpStatus.CREATED.value()).expect(MvcResult::getResponse);
    }

    @Test
    void findByAlias() {
        Mockito.when(this.userService.findUserByAlias(ArgumentMatchers.anyString())).thenReturn(ResponseEntity.ok(getUser()));
        Assertions.assertNotNull(this.userController.findByAlias("pperez"));
    }

    private User getUser() {
        final var user = new User();
        final var role = new Role();
        final var person = new Person();
        final var feature = new Feature();
        feature.setId(1L);
        feature.setActive(true);
        feature.setName("Feature name");
        feature.setDescription("Feature description");
        role.setId(11L);
        role.setActive(true);
        role.setName("Role name");
        role.setDescription("Role description");
        role.setFeatures(new ArrayList<>());
        role.getFeatures().add(feature);
        person.setMiddleName("Pepito");
        person.setFirstName("Pepe");
        person.setGender("M");
        person.setLastName("Perez");
        person.setId(1L);
        person.setBirthdate(LocalDate.of(1985, 5, 25));
        user.setActive(true);
        user.setAlias("pepe");
        user.setId(1L);
        user.setPassword("234567890");
        user.setPerson(person);
        user.setRoles(new ArrayList<>());
        user.getRoles().add(role);
        return user;
    }

    private @NotNull UserCreateRequest getUserCreateRequest() {
        var userCreateRequest = new UserCreateRequest();
        userCreateRequest.setAppName(APP_NAME_VALUE);
        userCreateRequest.setAppToken(APP_TOKEN_VALUE);
        userCreateRequest.setDateTime(LocalDateTime.now(ZoneId.systemDefault()));
        userCreateRequest.setUser(getUser());
        return userCreateRequest;
    }

}

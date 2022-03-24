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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;

/**
 * UserControllerTest.
 *
 * @author <a href='mailto:luis.antonio.mata@gmail.com'>Luis Antonio Mata</a>
 * @version 1.0.0, 26-03-2021
 */
class UserControllerTest extends MockLoaderBase {

    @Mock
    UserService userService;
    @InjectMocks
    UserController userController;

    Role role;
    User user;
    Person person;
    Feature feature;

    @BeforeEach
    void setUp() {
        role = new Role();
        user = new User();
        person = new Person();
        feature = new Feature();
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
    }

    @Test
    void find() {
        Mockito.when(this.userService.findUserById(ArgumentMatchers.anyLong()))
                .thenReturn(ResponseEntity.status(HttpStatus.FOUND).build());
        Assertions.assertNotNull(this.userController.find("ABS125", 12L));
    }

    @Test
    void findAll() {
        var users = new ArrayList<User>();
        users.add(user);
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
        final var userCreateRequest = new UserCreateRequest();
        userCreateRequest.setUser(user);
        userCreateRequest.setDateTime(LocalDateTime.now(ZoneId.systemDefault()));
        userCreateRequest.setAppName("TEST-APP");
        userCreateRequest.setAppToken("TEST-APP/00252336");
        Mockito.when(this.userService.create(ArgumentMatchers.any(User.class))).thenReturn(ResponseEntity.ok(user));
//        Assertions.assertNotNull(this.userController.create(userCreateRequest));
    }

    @Test
    void findByAlias() {
        Mockito.when(this.userService.findUserByAlias(ArgumentMatchers.anyString())).thenReturn(ResponseEntity.ok(user));
        Assertions.assertNotNull(this.userController.findByAlias("pperez"));
    }

}

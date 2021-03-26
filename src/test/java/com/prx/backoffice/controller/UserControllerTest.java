/*
 * @(#)UserControllerTest.java.
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

package com.prx.backoffice.controller;

import com.prx.backoffice.MockLoaderBase;
import com.prx.backoffice.enums.keys.UserMessageKey;
import com.prx.backoffice.service.UserService;
import com.prx.backoffice.to.user.UserAccessRequest;
import com.prx.backoffice.to.user.UserCreateRequest;
import com.prx.commons.pojo.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

/**
 * UserControllerTest.
 *
 * @author <a href='mailto:luis.antonio.mata@gmail.com'>Luis Antonio Mata</a>
 * @version 1.0.0, 26-03-2021
 */

public class UserControllerTest extends MockLoaderBase {

    @Mock
    UserService userService;
    @InjectMocks
    UserController userController;

    Rol rol;
    User user;
    Person person;
    Feature feature;
    MessageActivity<User> userMessageActivity;

    @BeforeEach
    public void setUp() {
        rol = new Rol();
        user = new User();
        person = new Person();
        feature = new Feature();
        userMessageActivity = new MessageActivity<>();
        feature.setId(1L);
        feature.setActive(true);
        feature.setName("Nombre de feature");
        feature.setDescription("Descripcin de feature");
        rol.setId(11);
        rol.setActive(true);
        rol.setName("Nombre de rol");
        rol.setDescription("Descripcion de rol");
        rol.setFeatures(new ArrayList<>());
        rol.getFeatures().add(feature);
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
        user.getRoles().add(rol);
        userMessageActivity.setObjectResponse(user);
    }

    @Test
    public void find() {
        userMessageActivity.setMessage(UserMessageKey.USER_FOUND.getStatus());
        userMessageActivity.setCode(UserMessageKey.USER_FOUND.getCode());
        userMessageActivity.setDateTime(LocalDateTime.now(ZoneId.systemDefault()));
        Mockito.when(this.userService.findUserById(ArgumentMatchers.anyLong())).thenReturn(userMessageActivity);
        Assertions.assertNotNull(this.userController.find("ABS125", 12L));
    }

    @Test
    public void findAll() {
        final var userMessageActivityList = new MessageActivity<List<User>>();
        userMessageActivityList.setDateTime(LocalDateTime.now(ZoneId.systemDefault()));
        userMessageActivityList.setMessage(UserMessageKey.USER_OK.getStatus());
        userMessageActivityList.setCode(UserMessageKey.USER_OK.getCode());
        userMessageActivityList.setObjectResponse(new ArrayList<>());
        userMessageActivityList.getObjectResponse().add(user);
        Mockito.when(this.userService.findAll()).thenReturn(userMessageActivityList);
        Assertions.assertNotNull(this.userController.findAll("ABS125", 12L));
    }

    @Test
    public void login() {
        final var userAccessRequest = new UserAccessRequest();
        final var messageActivityString = new MessageActivity<String>();
        messageActivityString.setObjectResponse(UserMessageKey.USER_OK.getStatus());
        messageActivityString.setDateTime(LocalDateTime.now(ZoneId.systemDefault()));
        messageActivityString.setMessage(UserMessageKey.USER_OK.getStatus());
        messageActivityString.setCode(UserMessageKey.USER_OK.getCode());
        userAccessRequest.setAlias("pepe");
        userAccessRequest.setPassword("123456789");
        userAccessRequest.setDateTime(LocalDateTime.now(ZoneId.systemDefault()));
        userAccessRequest.setAppName("TEST-APP");
        userAccessRequest.setAppToken("TEST-APP/00252336");
        Mockito.when(this.userService.access(ArgumentMatchers.anyString(),ArgumentMatchers.anyString())).thenReturn(messageActivityString);
        Assertions.assertNotNull(this.userController.login(userAccessRequest));
    }

    @Test
    public void create() {
        final var userCreateRequest = new UserCreateRequest();
        userCreateRequest.setUser(user);
        userCreateRequest.setDateTime(LocalDateTime.now(ZoneId.systemDefault()));
        userCreateRequest.setAppName("TEST-APP");
        userCreateRequest.setAppToken("TEST-APP/00252336");
        Mockito.when(this.userService.create(ArgumentMatchers.any(User.class))).thenReturn(userMessageActivity);
        Assertions.assertNotNull(this.userController.create(userCreateRequest));
    }

    @Test
    public void findByAlias() {
        Mockito.when(this.userService.findUserByAlias(ArgumentMatchers.anyString())).thenReturn(userMessageActivity);
        Assertions.assertNotNull(this.userController.findByAlias("pperez"));
    }

}
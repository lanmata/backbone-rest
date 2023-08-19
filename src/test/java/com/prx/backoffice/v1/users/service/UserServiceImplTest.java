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
package com.prx.backoffice.v1.users.service;

import com.prx.backoffice.MockLoaderBase;
import com.prx.backoffice.v1.people.service.PersonService;
import com.prx.backoffice.v1.roles.mapper.RoleMapper;
import com.prx.backoffice.v1.roles.service.RoleService;
import com.prx.backoffice.v1.users.api.to.UserTO;
import com.prx.backoffice.v1.users.mapper.UserMapper;
import com.prx.backoffice.v1.util.UserTemplateTest;
import com.prx.commons.pojo.Person;
import com.prx.persistence.general.domains.PersonEntity;
import com.prx.persistence.general.domains.UserEntity;
import com.prx.persistence.general.repositories.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * UserServiceTest.
 *
 * @author Luis Antonio Mata
 * @version 1.0.0, 27-10-2020
 */
@ContextConfiguration(classes = {UserServiceImpl.class})
@ExtendWith(SpringExtension.class)
class UserServiceImplTest extends MockLoaderBase {

    @MockBean
    private RoleService roleService;

    @Autowired
    private UserServiceImpl userServiceImpl;

    @MockBean
    PersonService personService;
    @MockBean
    UserRepository userRepository;
    @MockBean
    UserMapper userMapper;
    @MockBean
    RoleMapper roleMapper;

    /**
     * Method under test: {@link UserServiceImpl#findAll()}
     */
    @Test
    void testFindAll() {
        when(userRepository.findAll()).thenReturn(new ArrayList<>());
        ResponseEntity<List<UserTO>> actualFindAllResult = userServiceImpl.findAll();
        assertNull(actualFindAllResult.getBody());
        assertEquals(HttpStatus.NOT_FOUND, actualFindAllResult.getStatusCode());
        assertTrue(actualFindAllResult.getHeaders().isEmpty());
        verify(userRepository).findAll();
    }

    /**
     * Method under test: {@link UserServiceImpl#findAll()}
     */
    @Test
    void testFindAll2() {
        PersonEntity person = new PersonEntity();
        person.setBirthdate(LocalDate.of(1970, 1, 1));
        person.setGender("Gender");
        person.setId(UUID.randomUUID());
        person.setLastName("Doe");
        person.setMiddleName("Middle Name");
        person.setName("Name");

        UserEntity userEntity = new UserEntity();
        userEntity.setActive(true);
        userEntity.setAlias("Alias");
        userEntity.setId(UUID.randomUUID());
        userEntity.setPassword("iloveyou");
        userEntity.setPerson(person);
        userEntity.setUserRole(new HashSet<>());

        ArrayList<UserEntity> userEntityList = new ArrayList<>();
        userEntityList.add(userEntity);
        when(userRepository.findAll()).thenReturn(userEntityList);

        Person person2 = new Person();
        person2.setBirthdate(LocalDate.of(1970, 1, 1));
        person2.setFirstName("Jane");
        person2.setGender("Gender");
        person2.setId("42");
        person2.setLastName("Doe");
        person2.setMiddleName("Middle Name");

        UserTO userTO = new UserTO();
        userTO.setActive(true);
        userTO.setAlias("Alias");
        userTO.setId("42");
        userTO.setPassword("iloveyou");
        userTO.setPerson(person2);
        userTO.setRoles(new HashSet<>());
        when(userMapper.toTarget(Mockito.<UserEntity>any())).thenReturn(userTO);
        ResponseEntity<List<UserTO>> actualFindAllResult = userServiceImpl.findAll();
        assertTrue(actualFindAllResult.hasBody());
        assertEquals(HttpStatus.OK, actualFindAllResult.getStatusCode());
        assertTrue(actualFindAllResult.getHeaders().isEmpty());
        verify(userRepository).findAll();
        verify(userMapper).toTarget(Mockito.<UserEntity>any());
    }

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }


    @Test
    void create_user_password_required() {
        final var user = UserTemplateTest.USER.getModel();
        user.setPassword("");
        final var httpHeaders = new HttpHeaders();
        httpHeaders.set(HttpHeaders.WARNING, "password is required");
        final ResponseEntity<UserTO> responseEntity = ResponseEntity.badRequest().headers(httpHeaders).build();
        Assertions.assertEquals(responseEntity, this.userServiceImpl.create(user));
    }

    @Test
    void create_role_null() {
        final var user = UserTemplateTest.USER.getModel();
        user.setRoles(null);
        final var httpHeaders = new HttpHeaders();
        httpHeaders.set(HttpHeaders.WARNING, "Role is required");
        final ResponseEntity<UserTO> responseEntity = ResponseEntity.badRequest().headers(httpHeaders).build();
        Assertions.assertEquals(responseEntity, this.userServiceImpl.create(user));
    }

    @Test
    void create_user_null() {
        final ResponseEntity<UserTO> responseEntity = ResponseEntity.badRequest().build();
        Assertions.assertEquals(responseEntity, this.userServiceImpl.create(null));
    }

}

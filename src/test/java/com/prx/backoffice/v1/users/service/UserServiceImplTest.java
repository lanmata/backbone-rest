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
import com.prx.commons.pojo.Person;
import com.prx.persistence.general.domains.*;
import com.prx.persistence.general.repositories.PersonRepository;
import com.prx.persistence.general.repositories.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDate;
import java.util.*;

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
class
UserServiceImplTest extends MockLoaderBase {

    @MockBean
    private RoleService roleService;

    @Autowired
    private UserServiceImpl userServiceImpl;

    @MockBean
    PersonService personService;
    @MockBean
    UserRepository userRepository;
    @MockBean
    PersonRepository personRepository;
    @MockBean
    UserMapper userMapper;
    @MockBean
    RoleMapper roleMapper;

    /**
     * Method under test: {@link UserServiceImpl#update(String, UserTO)}
     */
    @Test
    void testUpdate() {
        final var userId = UUID.randomUUID();
        Person person = new Person();
        person.setBirthdate(LocalDate.of(1970, 1, 1));
        person.setFirstName("Jane");
        person.setGender("Gender");
        person.setId(UUID.randomUUID().toString());
        person.setLastName("Doe");
        person.setMiddleName("Middle Name");

        UserTO user = new UserTO();
        user.setActive(true);
        user.setAlias("Alias");
        user.setId(userId.toString());
        user.setPassword("iloveyou");
        user.setPerson(person);
        user.setRoles(new HashSet<>());

        PersonEntity personEntity = new PersonEntity();
        personEntity.setId(UUID.fromString(person.getId()));
        personEntity.setBirthdate(person.getBirthdate());
        personEntity.setGender(person.getGender());
        personEntity.setLastName(person.getLastName());
        personEntity.setName(person.getFirstName());
        personEntity.setMiddleName(person.getMiddleName());

        UserEntity userEntity = new UserEntity();
        userEntity.setId(userId);
        userEntity.setAlias(user.getAlias());
        userEntity.setActive(user.isActive());
        userEntity.setPassword(user.getPassword());
        userEntity.setPerson(personEntity);
        userEntity.setUserRole(new HashSet<>());

        final var responsePerson = ResponseEntity.ok(person);

        when(userMapper.toSource(user)).thenReturn(userEntity);
        when(userRepository.findById(Mockito.<UUID>any())).thenReturn(Optional.of(userEntity));
        when(personRepository.findById(Mockito.<UUID>any())).thenReturn(Optional.of(personEntity));
        when(personService.find(Mockito.anyString())).thenReturn(responsePerson);
        when(userMapper.toTarget(Mockito.<UserEntity>any())).thenReturn(user);
        when(userRepository.save(Mockito.<UserEntity>any())).thenReturn(userEntity);
        final var responseEntity = userServiceImpl.update(userId.toString(), user);
        Assertions.assertNotNull(responseEntity);
        Assertions.assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
    }

    /**
     * Method under test: {@link UserServiceImpl#update(String, UserTO)}
     */
    @Test
    void testUpdate1() {
        final var userId = UUID.randomUUID();
        Person person = new Person();
        person.setBirthdate(LocalDate.of(1970, 1, 1));
        person.setFirstName("Jane");
        person.setGender("Gender");
        person.setId(UUID.randomUUID().toString());
        person.setLastName("Doe");
        person.setMiddleName("Middle Name");

        UserTO user = new UserTO();
        user.setActive(true);
        user.setAlias("Alias");
        user.setId(userId.toString());
        user.setPassword("iloveyou");
        user.setPerson(person);
        user.setRoles(new HashSet<>());

        RoleEntity roleEntity = new RoleEntity();
        roleEntity.setDescription("Coast deaths jumping matthew line. ");
        roleEntity.setActive(true);
        roleEntity.setId(UUID.randomUUID());
        roleEntity.setName("Aftan Langley");
        roleEntity.setUserRoleEntities(new HashSet<>());
        roleEntity.setRoleFeatures(new HashSet<>());

        PersonEntity personEntity = new PersonEntity();
        personEntity.setId(UUID.fromString(person.getId()));
        personEntity.setBirthdate(person.getBirthdate());
        personEntity.setGender(person.getGender());
        personEntity.setLastName(person.getLastName());
        personEntity.setName(person.getFirstName());
        personEntity.setMiddleName(person.getMiddleName());

        UserEntity userEntity = new UserEntity();
        userEntity.setId(userId);
        userEntity.setAlias(user.getAlias());
        userEntity.setActive(user.isActive());
        userEntity.setPassword(user.getPassword());
        userEntity.setPerson(personEntity);

        UserRoleEntity userRoleEntity = new UserRoleEntity();
        userRoleEntity.setUser(userEntity);
        userRoleEntity.setRole(roleEntity);
        userRoleEntity.setActive(true);
        UserRolePK userRolePK = new UserRolePK();
        userRolePK.setUserId(userEntity.getId());
        userRolePK.setRoleId(roleEntity.getId());
        userRoleEntity.setUserRolePK(userRolePK);
        userEntity.setUserRole(Set.of(userRoleEntity));

        userEntity.setUserRole(Set.of(userRoleEntity));
        final var responsePerson = ResponseEntity.ok(person);

        when(userMapper.toSource(user)).thenReturn(userEntity);
        when(userRepository.findById(Mockito.<UUID>any())).thenReturn(Optional.of(userEntity));
        when(personRepository.findById(Mockito.<UUID>any())).thenReturn(Optional.of(personEntity));
        when(personService.find(Mockito.anyString())).thenReturn(responsePerson);
        when(userMapper.toTarget(Mockito.<UserEntity>any())).thenReturn(user);
        when(userRepository.save(Mockito.<UserEntity>any())).thenReturn(userEntity);
        final var responseEntity = userServiceImpl.update(userId.toString(), user);
        Assertions.assertNotNull(responseEntity);
        Assertions.assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
    }

    /**
     * Method under test: {@link UserServiceImpl#update(String, UserTO)}
     */
    @Test
    void testUpdate2() {
        final var userId = UUID.randomUUID();
        Person person = new Person();
        person.setBirthdate(LocalDate.of(1970, 1, 1));
        person.setFirstName("Jane");
        person.setGender("Gender");
        person.setId(UUID.randomUUID().toString());
        person.setLastName("Doe");
        person.setMiddleName("Middle Name");

        UserTO user = new UserTO();
        user.setActive(true);
        user.setAlias("Alias");
        user.setId(userId.toString());
        user.setPassword("iloveyou");
        user.setPerson(person);
        user.setRoles(new HashSet<>());

        PersonEntity personEntity = new PersonEntity();
        personEntity.setId(UUID.fromString(person.getId()));
        personEntity.setBirthdate(person.getBirthdate());
        personEntity.setGender(person.getGender());
        personEntity.setLastName(person.getLastName());
        personEntity.setName(person.getFirstName());
        personEntity.setMiddleName(person.getMiddleName());

        UserEntity userEntity = new UserEntity();
        userEntity.setId(userId);
        userEntity.setAlias(user.getAlias());
        userEntity.setActive(user.isActive());
        userEntity.setPassword(user.getPassword());
        userEntity.setPerson(personEntity);
        userEntity.setUserRole(new HashSet<>());

        when(userMapper.toSource(user)).thenReturn(userEntity);
        when(userRepository.findById(Mockito.<UUID>any())).thenReturn(Optional.of(userEntity));
        when(personRepository.findById(Mockito.<UUID>any())).thenReturn(Optional.empty());
        when(personService.find(Mockito.anyString())).thenReturn(ResponseEntity.notFound().build());
        when(userMapper.toTarget(Mockito.<UserEntity>any())).thenReturn(user);
        when(userRepository.save(Mockito.<UserEntity>any())).thenReturn(userEntity);
        final var responseEntity = userServiceImpl.update(userId.toString(), user);
        Assertions.assertNotNull(responseEntity);
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
    }

    /**
     * Method under test: {@link UserServiceImpl#update(String, UserTO)}
     */
    @Test
    void testUpdate3() {
        UserTO user = new UserTO();
        user.setActive(true);
        user.setAlias("Alias");
        user.setPassword("iloveyou");
        user.setRoles(new HashSet<>());

        final var responseEntity = userServiceImpl.update(null, user);
        Assertions.assertNotNull(responseEntity);
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
    }

    /**
     * Method under test: {@link UserServiceImpl#update(String, UserTO)}
     */
    @Test
    void testUpdate4() {
        UserTO user = new UserTO();
        user.setActive(true);
        user.setAlias("Alias");
        user.setPassword("iloveyou");
        user.setRoles(new HashSet<>());

        final var responseEntity = userServiceImpl.update("", user);
        Assertions.assertNotNull(responseEntity);
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
    }

    /**
     * Method under test: {@link UserServiceImpl#update(String, UserTO)}
     */
    @Test
    void testUpdate5() {
        final var userId = UUID.randomUUID();
        Person person = new Person();
        person.setBirthdate(LocalDate.of(1970, 1, 1));
        person.setFirstName("Jane");
        person.setGender("Gender");
        person.setId(UUID.randomUUID().toString());
        person.setLastName("Doe");
        person.setMiddleName("Middle Name");

        UserTO user = new UserTO();
        user.setActive(true);
        user.setAlias("Alias");
        user.setPassword("iloveyou");
        user.setPerson(person);
        user.setRoles(new HashSet<>());

        when(userRepository.findById(Mockito.<UUID>any())).thenReturn(Optional.empty());
        final var responseEntity = userServiceImpl.update(userId.toString(), user);
        Assertions.assertNotNull(responseEntity);
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
    }

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
    void create_user_null() {
        final ResponseEntity<UserTO> responseEntity = ResponseEntity.badRequest().build();
        Assertions.assertEquals(responseEntity, this.userServiceImpl.create(null));
    }

    /**
     * Method under test: {@link UserService#aliasValidate(String)}
     */
    @Test
    void testAliasValidate() {
        String alias = "Alias";
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

        when(userMapper.toTarget(Mockito.any())).thenReturn(userTO);
        when(userRepository.findByAlias(Mockito.anyString())).thenReturn(userEntity);
        // Act
        ResponseEntity<String> actualAliasValidateResult = userServiceImpl.aliasValidate(alias);

        // Assert
        assertNotNull(actualAliasValidateResult);
        assertEquals(HttpStatus.NOT_ACCEPTABLE, actualAliasValidateResult.getStatusCode());
    }

    /**
     * Method under test: {@link UserService#aliasValidate(String)}
     */
    @Test
    void testAliasValidate_not_acceptable() {
        String alias = "Alias";
        when(userRepository.findByAlias(Mockito.anyString())).thenReturn(null);
        // Act
        ResponseEntity<String> actualAliasValidateResult = userServiceImpl.aliasValidate(alias);

        // Assert
        assertNotNull(actualAliasValidateResult);
        assertEquals(HttpStatus.OK, actualAliasValidateResult.getStatusCode());
    }

}

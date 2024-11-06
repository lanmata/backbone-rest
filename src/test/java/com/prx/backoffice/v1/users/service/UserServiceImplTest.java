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

import com.prx.backoffice.v1.people.service.PersonService;
import com.prx.backoffice.v1.roles.mapper.RoleMapper;
import com.prx.backoffice.v1.roles.service.RoleService;
import com.prx.backoffice.v1.users.api.to.UserTO;
import com.prx.backoffice.v1.users.mapper.UserMapper;
import com.prx.commons.pojo.Person;
import com.prx.commons.pojo.Role;
import com.prx.persistence.general.domains.*;
import com.prx.persistence.general.repositories.PersonRepository;
import com.prx.persistence.general.repositories.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * UserServiceTest.
 *
 * @author Luis Antonio Mata
 * @version 1.0.0, 27-10-2020
 */
@ExtendWith(SpringExtension.class)
class UserServiceImplTest {

    @InjectMocks
    private UserServiceImpl userServiceImpl;

    @Mock
    private RoleService roleService;

    @Mock
    private PersonService personService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PersonRepository personRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private RoleMapper roleMapper;

    /**
     * Method under test: {@link UserServiceImpl#update(String, UserTO)}
     */
    @Test
    @DisplayName("Test update user with valid data")
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
    @DisplayName("Test update user with roles")
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
    @DisplayName("Test update user with missing person")
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
    @DisplayName("Test update user with null ID")
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
    @DisplayName("Test update user with empty ID")
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
    @DisplayName("Test update user with non-existent user")
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
    @DisplayName("Test find all users with empty repository")
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
    @DisplayName("Test find all users with data")
    void testFindAll2() {

        ArrayList<UserEntity> userEntityList = new ArrayList<>();
        userEntityList.add(getUserEntity(null, null));
        when(userRepository.findAll()).thenReturn(userEntityList);
        when(userMapper.toTarget(Mockito.<UserEntity>any())).thenReturn(getUserTO(null, null));

        ResponseEntity<List<UserTO>> actualFindAllResult = userServiceImpl.findAll();
        assertTrue(actualFindAllResult.hasBody());
        assertEquals(HttpStatus.OK, actualFindAllResult.getStatusCode());
        assertTrue(actualFindAllResult.getHeaders().isEmpty());
        verify(userRepository).findAll();
        verify(userMapper).toTarget(Mockito.<UserEntity>any());
    }

    @Test
    @DisplayName("Test create user with null data")
    void create_user_null() {
        final ResponseEntity<UserTO> responseEntity = ResponseEntity.badRequest().build();
        Assertions.assertEquals(responseEntity, this.userServiceImpl.create(null));
    }

    /**
     * Method under test: {@link UserService#aliasValidate(String)}
     */
    @Test
    @DisplayName("Test alias validation with existing alias")
    void testAliasValidate() {
        String alias = "Alias";
        String password = "password";

        when(userMapper.toTarget(Mockito.any())).thenReturn(getUserTO(alias, password));
        when(userRepository.findByAlias(Mockito.anyString())).thenReturn(getUserEntity(alias, password));
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
    @DisplayName("Test alias validation with non-existing alias")
    void testAliasValidate_not_acceptable() {
        String alias = "Alias";
        when(userRepository.findByAlias(Mockito.anyString())).thenReturn(null);
        // Act
        ResponseEntity<String> actualAliasValidateResult = userServiceImpl.aliasValidate(alias);

        // Assert
        assertNotNull(actualAliasValidateResult);
        assertEquals(HttpStatus.OK, actualAliasValidateResult.getStatusCode());
    }

    @Test
    @DisplayName("Test delete user")
    void testDelete() {
        String userId = UUID.randomUUID().toString();
        UserTO user = new UserTO();
        user.setId(userId);

        doNothing().when(userRepository).deleteById(UUID.fromString(userId));
        ResponseEntity<UserTO> responseEntity = userServiceImpl.delete(userId, user);

        Assertions.assertNull(responseEntity);
    }

    @Test
    @DisplayName("Test find user by ID")
    void testFind() {
        String userId = UUID.randomUUID().toString();
        UserEntity userEntity = new UserEntity();
        userEntity.setId(UUID.fromString(userId));

        when(userRepository.findById(UUID.fromString(userId))).thenReturn(Optional.of(userEntity));
        when(userMapper.toTarget(userEntity)).thenReturn(new UserTO());

        ResponseEntity<UserTO> responseEntity = userServiceImpl.find(userId);

        Assertions.assertNull(responseEntity);
    }

    @Test
    @DisplayName("Test list all users")
    void testList() {
        List<UserEntity> userEntities = new ArrayList<>();
        userEntities.add(new UserEntity());

        when(userRepository.findAll()).thenReturn(userEntities);
        when(userMapper.toTarget(any(UserEntity.class))).thenReturn(new UserTO());

        ResponseEntity<List<UserTO>> responseEntity = userServiceImpl.list();

        Assertions.assertNull(responseEntity);
    }

    @Test
    @DisplayName("Test unlink user from role")
    void testUnlink() {
        String userId = UUID.randomUUID().toString();
        String roleId = UUID.randomUUID().toString();

        Assertions.assertThrows(UnsupportedOperationException.class, () -> {
            userServiceImpl.unlink(userId, roleId);
        });
    }

    @Test
    @DisplayName("Test link user to role")
    void testLink() {
        String userId = UUID.randomUUID().toString();
        String roleId = UUID.randomUUID().toString();
        UserEntity userEntity = new UserEntity();
        RoleEntity roleEntity = new RoleEntity();
        Set<UserRoleEntity> userRoleEntities = new HashSet<>();
        userEntity.setId(UUID.fromString(userId));
        roleEntity.setId(UUID.randomUUID());
        UserRoleEntity userRoleEntity = new UserRoleEntity();
        userRoleEntity.setRole(roleEntity);
        userRoleEntities.add(userRoleEntity);
        userEntity.setUserRole(userRoleEntities);

        when(userRepository.findById(UUID.fromString(userId))).thenReturn(Optional.of(userEntity));
        when(roleService.find(roleId)).thenReturn(ResponseEntity.ok(new Role()));
        when(userMapper.toTarget(userEntity)).thenReturn(new UserTO());

        ResponseEntity<UserTO> responseEntity = userServiceImpl.link(userId, roleId);

        Assertions.assertNotNull(responseEntity);
        Assertions.assertEquals(HttpStatus.ACCEPTED, responseEntity.getStatusCode());
    }

    @Test
    @DisplayName("Test access with valid alias and password")
    void testAccessValidAliasAndPassword() {
        String alias = "validAlias";
        String password = "iloveyou";
        var userEntity = getUserEntity(alias, password);

        when(userRepository.findByAlias(alias)).thenReturn(userEntity);
        when(userMapper.toTarget(Mockito.any())).thenReturn(getUserTO(alias, password));

        ResponseEntity<String> response = userServiceImpl.access(alias, password);

        assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
    }

    @Test
    @DisplayName("Test access with valid alias and invalid password")
    void testAccessValidAliasInvalidPassword() {
        String alias = "validAlias";
        String password = "invalidPassword";
        UserTO userTO = new UserTO();
        userTO.setPassword("validPassword");

        when(userRepository.findByAlias(Mockito.anyString())).thenReturn(getUserEntity(alias, password));
        when(userMapper.toTarget(Mockito.any())).thenReturn(userTO);

        ResponseEntity<String> response = userServiceImpl.access(alias, password);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    @DisplayName("Test access with inactive user")
    void testAccessInactiveUser() {
        String alias = "validAlias";
        String password = "iloveyou";
        var userTO = getUserTO(alias, password);
        userTO.setActive(false);

        when(userRepository.findByAlias(Mockito.anyString())).thenReturn(getUserEntity(alias, password));
        when(userMapper.toTarget(Mockito.any())).thenReturn(userTO);

        ResponseEntity<String> response = userServiceImpl.access(alias, password);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    @DisplayName("Test access with user null")
    void testAccessUserNull() {
        String alias = "validAlias";
        String password = "iloveyou";

        when(userRepository.findByAlias(Mockito.anyString())).thenReturn(null);

        ResponseEntity<String> response = userServiceImpl.access(alias, password);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    @DisplayName("Test access with non-existing alias")
    void testAccessNonExistingAlias() {
        String alias = "nonExistingAlias";
        String password = "password";

        when(userRepository.findByAlias(Mockito.anyString())).thenReturn(null);

        ResponseEntity<String> response = userServiceImpl.access(alias, password);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    @DisplayName("Test create user with null data")
    void testCreateUserWithNullData() {
        ResponseEntity<UserTO> response = userServiceImpl.create(null);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @DisplayName("Test create user with blank alias")
    void testCreateUserWithBlankAlias() {
        UserTO user = new UserTO();
        user.setAlias("");
        user.setPassword("password");
        user.setRoles(new HashSet<>());

        ResponseEntity<UserTO> response = userServiceImpl.create(user);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("username is required", response.getHeaders().getFirst(HttpHeaders.WARNING));
    }

    @Test
    @DisplayName("Test create user with blank password")
    void testCreateUserWithBlankPassword() {
        UserTO user = new UserTO();
        user.setAlias("alias");
        user.setPassword("");
        user.setRoles(new HashSet<>());

        ResponseEntity<UserTO> response = userServiceImpl.create(user);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("password is required", response.getHeaders().getFirst(HttpHeaders.WARNING));
    }

    @Test
    @DisplayName("Test create user with empty roles")
    void testCreateUserWithEmptyRoles() {
        UserTO user = new UserTO();
        user.setAlias("alias");
        user.setPassword("password");
        user.setRoles(new HashSet<>());

        ResponseEntity<UserTO> response = userServiceImpl.create(user);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Role is required", response.getHeaders().getFirst(HttpHeaders.WARNING));
    }

    @Test
    @DisplayName("Test create user with existing alias")
    void testCreateUserWithExistingAlias() {
        String alias = "alias";
        String password = "password";
        var userTO = getUserTO(alias, password);
        userTO.getRoles().add(178L);

        when(userRepository.findByAlias(Mockito.anyString())).thenReturn(getUserEntity(alias, password));
        when(userMapper.toTarget(Mockito.any())).thenReturn(userTO);

        ResponseEntity<UserTO> response = userServiceImpl.create(userTO);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("User previously exist.", response.getHeaders().getFirst(HttpHeaders.WARNING));
    }

    @Test
    @DisplayName("Test create user successfully")
    void testCreateUserSuccessfully() {
        String alias = "alias";
        String password = "password";
        var userTO = getUserTO(alias, password);
        userTO.getRoles().add(178L);

        when(userRepository.findByAlias(Mockito.anyString())).thenReturn(null);
        when(userMapper.toTarget(Mockito.any())).thenReturn(userTO);
        when(personService.create(userTO.getPerson())).thenReturn(ResponseEntity.status(HttpStatus.CREATED).body(new Person()));
        when(userMapper.toSource(userTO)).thenReturn(new UserEntity());
        when(userRepository.save(any(UserEntity.class))).thenReturn(new UserEntity());
        when(userMapper.toTarget(any(UserEntity.class))).thenReturn(userTO);

        ResponseEntity<UserTO> response = userServiceImpl.create(userTO);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    private static UserTO getUserTO(String alias, String password) {
        Person person2 = new Person();
        person2.setBirthdate(LocalDate.of(1970, 1, 1));
        person2.setFirstName("Jane");
        person2.setGender("Gender");
        person2.setId("42");
        person2.setLastName("Doe");
        person2.setMiddleName("Middle Name");

        UserTO userTO = new UserTO();
        userTO.setActive(true);
        userTO.setAlias(alias);
        userTO.setId("42");
        userTO.setPassword(password);
        userTO.setPerson(person2);
        userTO.setRoles(new HashSet<>());

        return userTO;
    }

    private static UserEntity getUserEntity(String alias, String password) {
        PersonEntity person = new PersonEntity();
        person.setBirthdate(LocalDate.of(1970, 1, 1));
        person.setGender("Gender");
        person.setId(UUID.randomUUID());
        person.setLastName("Doe");
        person.setMiddleName("Middle Name");
        person.setName("Name");

        UserEntity userEntity = new UserEntity();
        userEntity.setActive(true);
        userEntity.setAlias(alias);
        userEntity.setId(UUID.randomUUID());
        userEntity.setPassword(password);
        userEntity.setPerson(person);
        userEntity.setUserRole(new HashSet<>());

        return userEntity;
    }

}

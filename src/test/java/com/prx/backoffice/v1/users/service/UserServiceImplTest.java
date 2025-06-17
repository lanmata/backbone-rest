/*
 *  @(#)UserServiceImplTest.java
 *
 *  Copyright (c) Luis Antonio Mata Mata. All rights reserved.
 *
 *   All rights to this product are owned by Luis Antonio Mata Mata and may only
 *  be used under the terms of its associated license document. You may NOT
 *  copy, modify, sublicense, or distribute this source file or portions of
 *  it unless previously authorized in writing by Luis Antonio Mata Mata.
 *  In any event, this notice and the above copyright must always be included
 *  verbatim with this file.
 */
package com.prx.backoffice.v1.users.service;

import com.prx.backoffice.v1.people.mapper.PersonMapper;
import com.prx.backoffice.v1.people.service.PersonService;
import com.prx.backoffice.v1.roles.mapper.RoleMapper;
import com.prx.backoffice.v1.roles.service.RoleService;
import com.prx.backoffice.v1.users.api.to.UserCreateRequest;
import com.prx.backoffice.v1.users.api.to.UserCreateResponse;
import com.prx.backoffice.v1.users.api.to.UserTO;
import com.prx.backoffice.v1.users.mapper.UserMapper;
import com.prx.commons.general.pojo.Person;
import com.prx.commons.general.pojo.Role;
import com.prx.persistence.general.domains.*;
import com.prx.persistence.general.repositories.ApplicationRoleUserRepository;
import com.prx.persistence.general.repositories.PersonRepository;
import com.prx.persistence.general.repositories.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
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
    private ApplicationRoleUserRepository applicationRoleUserRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PersonMapper personMapper;

    @Mock
    private RoleMapper roleMapper;

    /**
     * Method under test: {@link UserServiceImpl#update(UUID, UserTO)}
     */
    @Test
    @DisplayName("Test update user with valid data")
    void testUpdate() {
        final var userId = UUID.randomUUID();
        Person person = new Person();
        person.setBirthdate(LocalDate.of(1970, 1, 1));
        person.setFirstName("Jane");
        person.setGender("Gender");
        person.setId(UUID.randomUUID());
        person.setLastName("Doe");
        person.setMiddleName("Middle Name");

        Role role = new Role();
        role.setActive(true);
        role.setDescription("Description");
        role.setId(UUID.randomUUID());
        role.setName("Role");

        UserTO user = new UserTO();
        user.setActive(true);
        user.setAlias("Alias");
        user.setId(userId);
        user.setPassword("iloveyou");
        user.setPerson(person);
        user.setRoles(Set.of(role));

        PersonEntity personEntity = new PersonEntity();
        personEntity.setId(person.getId());
        personEntity.setBirthdate(person.getBirthdate());
        personEntity.setGender(person.getGender());
        personEntity.setLastName(person.getLastName());
        personEntity.setName(person.getFirstName());
        personEntity.setMiddleName(person.getMiddleName());

        RoleEntity roleEntity = new RoleEntity();
        role.setActive(true);
        role.setDescription("Description");
        role.setId(UUID.randomUUID());
        role.setName("Role");

        UserEntity userEntity = new UserEntity();
        userEntity.setId(userId);
        userEntity.setAlias(user.getAlias());
        userEntity.setActive(user.isActive());
        userEntity.setPassword(user.getPassword());
        userEntity.setPerson(personEntity);

        ApplicationRoleUserEntity applicationRoleUserEntity = new ApplicationRoleUserEntity();
        applicationRoleUserEntity.setRole(roleEntity);
        applicationRoleUserEntity.setUser(userEntity);

        userEntity.setApplicationRoleUser(Set.of(applicationRoleUserEntity));

        final var responsePerson = ResponseEntity.ok(person);

        when(userMapper.toSource(user)).thenReturn(userEntity);
        when(userRepository.findById(Mockito.<UUID>any())).thenReturn(Optional.of(userEntity));
        when(personRepository.findById(Mockito.<UUID>any())).thenReturn(Optional.of(personEntity));
        when(personService.find(Mockito.any(UUID.class))).thenReturn(responsePerson);
        when(userMapper.toTarget(Mockito.<UserEntity>any())).thenReturn(user);
        when(userRepository.save(Mockito.<UserEntity>any())).thenReturn(userEntity);
        final var responseEntity = userServiceImpl.update(userId, user);
        Assertions.assertNotNull(responseEntity);
        Assertions.assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
    }

    /**
     * Method under test: {@link UserServiceImpl#update(UUID, UserTO)}
     */
    @Test
    @DisplayName("Test update user with roles")
    void testUpdate1() {
        final var userId = UUID.randomUUID();
        Person person = new Person();
        person.setBirthdate(LocalDate.of(1970, 1, 1));
        person.setFirstName("Jane");
        person.setGender("Gender");
        person.setId(UUID.randomUUID());
        person.setLastName("Doe");
        person.setMiddleName("Middle Name");

        UserTO user = new UserTO();
        user.setActive(true);
        user.setAlias("Alias");
        user.setId(userId);
        user.setPassword("iloveyou");
        user.setPerson(person);
        user.setRoles(new HashSet<>());

        RoleEntity roleEntity = new RoleEntity();
        roleEntity.setDescription("Coast deaths jumping matthew line. ");
        roleEntity.setActive(true);
        roleEntity.setId(UUID.randomUUID());
        roleEntity.setName("Aftan Langley");
        roleEntity.setApplicationRoleUser(new HashSet<>());
        roleEntity.setRoleFeatures(new HashSet<>());

        PersonEntity personEntity = new PersonEntity();
        personEntity.setId(person.getId());
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

        ApplicationRoleUserEntity applicationRoleUserEntity = new ApplicationRoleUserEntity();
        applicationRoleUserEntity.setUser(userEntity);
        applicationRoleUserEntity.setRole(roleEntity);
        applicationRoleUserEntity.setActive(true);
        ApplicationRoleUserEntityId userRolePK = new ApplicationRoleUserEntityId();
        userRolePK.setUserId(userEntity.getId());
        userRolePK.setRoleId(roleEntity.getId());
//        applicationRoleUserEntity.setUserRolePK(userRolePK);
        userEntity.setApplicationRoleUser(Set.of(applicationRoleUserEntity));

        userEntity.setApplicationRoleUser(Set.of(applicationRoleUserEntity));
        final var responsePerson = ResponseEntity.ok(person);

        when(userMapper.toSource(user)).thenReturn(userEntity);
        when(userRepository.findById(Mockito.<UUID>any())).thenReturn(Optional.of(userEntity));
        when(personRepository.findById(Mockito.<UUID>any())).thenReturn(Optional.of(personEntity));
        when(personService.find(Mockito.any(UUID.class))).thenReturn(responsePerson);
        when(userMapper.toTarget(Mockito.<UserEntity>any())).thenReturn(user);
        when(userRepository.save(Mockito.<UserEntity>any())).thenReturn(userEntity);
        final var responseEntity = userServiceImpl.update(userId, user);
        Assertions.assertNotNull(responseEntity);
        Assertions.assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
    }

    /**
     * Method under test: {@link UserServiceImpl#update(UUID, UserTO)}
     */
    @Test
    @DisplayName("Test update user with missing person")
    void testUpdate2() {
        final var userId = UUID.randomUUID();
        Person person = new Person();
        person.setBirthdate(LocalDate.of(1970, 1, 1));
        person.setFirstName("Jane");
        person.setGender("Gender");
        person.setId(UUID.randomUUID());
        person.setLastName("Doe");
        person.setMiddleName("Middle Name");

        UserTO user = new UserTO();
        user.setActive(true);
        user.setAlias("Alias");
        user.setId(userId);
        user.setPassword("iloveyou");
        user.setPerson(person);
        user.setRoles(new HashSet<>());

        PersonEntity personEntity = new PersonEntity();
        personEntity.setId(person.getId());
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
        userEntity.setApplicationRoleUser(new HashSet<>());

        when(userMapper.toSource(user)).thenReturn(userEntity);
        when(userRepository.findById(Mockito.<UUID>any())).thenReturn(Optional.of(userEntity));
        when(personRepository.findById(Mockito.<UUID>any())).thenReturn(Optional.empty());
        when(personService.find(Mockito.any(UUID.class))).thenReturn(ResponseEntity.notFound().build());
        when(userMapper.toTarget(Mockito.<UserEntity>any())).thenReturn(user);
        when(userRepository.save(Mockito.<UserEntity>any())).thenReturn(userEntity);
        final var responseEntity = userServiceImpl.update(userId, user);
        Assertions.assertNotNull(responseEntity);
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
    }

    /**
     * Method under test: {@link UserServiceImpl#update(UUID, UserTO)}
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
     * Method under test: {@link UserServiceImpl#update(UUID, UserTO)}
     */
    @Test
    @DisplayName("Test update user with empty ID")
    void testUpdate4() {
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
     * Method under test: {@link UserServiceImpl#update(UUID, UserTO)}
     */
    @Test
    @DisplayName("Test update user with non-existent user")
    void testUpdate5() {
        final var userId = UUID.randomUUID();
        Person person = new Person();
        person.setBirthdate(LocalDate.of(1970, 1, 1));
        person.setFirstName("Jane");
        person.setGender("Gender");
        person.setId(UUID.randomUUID());
        person.setLastName("Doe");
        person.setMiddleName("Middle Name");

        UserTO user = new UserTO();
        user.setActive(true);
        user.setAlias("Alias");
        user.setPassword("iloveyou");
        user.setPerson(person);
        user.setRoles(new HashSet<>());

        when(userRepository.findById(Mockito.<UUID>any())).thenReturn(Optional.empty());
        final var responseEntity = userServiceImpl.update(userId, user);
        Assertions.assertNotNull(responseEntity);
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
    }

    /**
     * Method under test: {@link UserServiceImpl#findAll(UUID)}
     */
    @Test
    @DisplayName("Test find all users with empty repository")
    void testFindAll() {
        when(userRepository.findAll()).thenReturn(new ArrayList<>());
        ResponseEntity<List<UserTO>> actualFindAllResult = userServiceImpl.findAll(null);
        assertNull(actualFindAllResult.getBody());
        assertEquals(HttpStatus.NOT_FOUND, actualFindAllResult.getStatusCode());
        assertTrue(actualFindAllResult.getHeaders().isEmpty());
        verify(userRepository).findAll();
    }

    /**
     * Method under test: {@link UserServiceImpl#findAll(UUID)}
     */
    @Test
    @DisplayName("Test find all users byApplicationId with data")
    void testFindAllByApplication() {

        ArrayList<UserEntity> userEntityList = new ArrayList<>();
        userEntityList.add(getUserEntity(null, null));
        when(userRepository.findByApplication(Mockito.any(UUID.class))).thenReturn(userEntityList);
        when(userMapper.toTarget(Mockito.<UserEntity>any())).thenReturn(getUserTO(null, null));

        ResponseEntity<List<UserTO>> actualFindAllResult = userServiceImpl.findAll(UUID.randomUUID());
        assertTrue(actualFindAllResult.hasBody());
        assertEquals(HttpStatus.OK, actualFindAllResult.getStatusCode());
        assertTrue(actualFindAllResult.getHeaders().isEmpty());
        verify(userRepository).findByApplication(Mockito.any(UUID.class));
        verify(userMapper).toTarget(Mockito.<UserEntity>any());
    }

    /**
     * Method under test: {@link UserServiceImpl#findAll(UUID)}
     */
    @Test
    @DisplayName("Test find all users with data")
    void testFindAll2() {
        ArrayList<UserEntity> userEntityList = new ArrayList<>();
        userEntityList.add(getUserEntity(null, null));
        when(userRepository.findAll()).thenReturn(userEntityList);
        when(userMapper.toTarget(Mockito.<UserEntity>any())).thenReturn(getUserTO(null, null));

        ResponseEntity<List<UserTO>> actualFindAllResult = userServiceImpl.findAll(null);
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
        Assertions.assertEquals(responseEntity, this.userServiceImpl.create((UserCreateRequest) null));
    }

    @Test
    @DisplayName("Test delete user")
    void testDelete() {
        UUID userId = UUID.randomUUID();
        UserTO user = new UserTO();
        user.setId(userId);

        doNothing().when(userRepository).deleteById(userId);
        ResponseEntity<UserTO> responseEntity = userServiceImpl.delete(userId, user);

        assertEquals(HttpStatus.NOT_IMPLEMENTED, responseEntity.getStatusCode());
    }

    @Test
    @DisplayName("Test find user by ID")
    void testFind() {
        var userId = UUID.randomUUID();
        UserEntity userEntity = new UserEntity();
        userEntity.setId(userId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(userEntity));
        when(userMapper.toTarget(userEntity)).thenReturn(new UserTO());

        assertNull(userServiceImpl.find(userId));
    }

    @Test
    @DisplayName("Test list all users")
    void testList() {
        List<UserEntity> userEntities = new ArrayList<>();
        userEntities.add(new UserEntity());

        when(userRepository.findAll()).thenReturn(userEntities);
        when(userMapper.toTarget(any(UserEntity.class))).thenReturn(new UserTO());

        ResponseEntity<List<UserTO>> responseEntity = userServiceImpl.list();

        assertEquals(HttpStatus.NOT_IMPLEMENTED, responseEntity.getStatusCode());
    }

    @Test
    @DisplayName("Test unlink user from role")
    void testUnlink() {
        var userId = UUID.randomUUID();
        var roleId = UUID.randomUUID();

        Assertions.assertThrows(UnsupportedOperationException.class, () -> {
            userServiceImpl.unlink(userId, roleId);
        });
    }

    @Test
    @DisplayName("Test link user to role")
    void testLink() {
        var userId = UUID.randomUUID();
        var roleId = UUID.randomUUID();
        UserEntity userEntity = new UserEntity();
        RoleEntity roleEntity = new RoleEntity();
        Set<ApplicationRoleUserEntity> userRoleEntities = new HashSet<>();
        userEntity.setId(userId);
        roleEntity.setId(UUID.randomUUID());
        ApplicationRoleUserEntity applicationRoleUserEntity = new ApplicationRoleUserEntity();
        applicationRoleUserEntity.setRole(roleEntity);
        userRoleEntities.add(applicationRoleUserEntity);
        userEntity.setApplicationRoleUser(userRoleEntities);

        when(userRepository.findById(userId)).thenReturn(Optional.of(userEntity));
        when(roleService.find(roleId)).thenReturn(ResponseEntity.ok(new Role()));
        when(userMapper.toTarget(userEntity)).thenReturn(new UserTO());

        ResponseEntity<UserTO> responseEntity = userServiceImpl.roleLink(userId, roleId);

        Assertions.assertNotNull(responseEntity);
        Assertions.assertEquals(HttpStatus.ACCEPTED, responseEntity.getStatusCode());
    }

    @Test
    @DisplayName("Test create user with null data")
    void testCreateUserWithNullData() {
        ResponseEntity<UserCreateResponse> response = userServiceImpl.create((UserCreateRequest) null);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @DisplayName("Test create user with blank alias")
    void testCreateUserWithBlankAlias() {
        ResponseEntity<UserCreateResponse> response = userServiceImpl.create(getUserCreateRequest(
                "", "nvbgd233", " user@domain.ext",
                UUID.randomUUID(), UUID.randomUUID()));
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("username is required", response.getHeaders().getFirst(HttpHeaders.WARNING));
    }

    @Test
    @DisplayName("Test create user with blank password")
    void testCreateUserWithBlankPassword() {
        ResponseEntity<UserCreateResponse> response = userServiceImpl.create(getUserCreateRequest("alias", "", " user@domain.ext", UUID.randomUUID(), UUID.randomUUID()));
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("password is required", response.getHeaders().getFirst(HttpHeaders.WARNING));
    }

    @Test
    @DisplayName("Test create user with empty roles")
    void testCreateUserWithEmptyRoles() {
        ResponseEntity<UserCreateResponse> response = userServiceImpl.create(getUserCreateRequest("alias", "nvbgd233", " user@domain.ext", null, UUID.randomUUID()));
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Role is required", response.getHeaders().getFirst(HttpHeaders.WARNING));
    }

//    @Test
//    @DisplayName("Test create user with existing alias")
//    void testCreateUserWithExistingAlias() {
//        String alias = "alias";
//        String password = "password";
//        var userTO = getUserTO(alias, password);
//        userTO.getRoles().add(new Role());
//
//        when(userRepository.findByAlias(Mockito.anyString())).thenReturn(getUserEntity(alias, password));
//        when(userMapper.toTarget(Mockito.any(UserEntity.class))).thenReturn(userTO);
//
//        ResponseEntity<UserTO> response = userServiceImpl.create(userTO);
//        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
//        assertEquals("User previously exist.", response.getHeaders().getFirst(HttpHeaders.WARNING));
//    }

    @Test
    @DisplayName("Test create user successfully")
    void testCreateUserSuccessfully() {
        String alias = "alias";
        String password = "password";
        var application = new ApplicationEntity();
        var applicationRoleUserEntity = new ApplicationRoleUserEntity();
        var applicationRoleUserEntityId = new ApplicationRoleUserEntityId();
        var userCreateRequest = getUserCreateRequest("alias", "nvbgd233", " user@domain.ext", UUID.randomUUID(), UUID.randomUUID());
        Set<ApplicationRoleUserEntity> applicationUserSet = new HashSet<>();

        var userEntity = new UserEntity();
        userEntity.setCreatedDate(LocalDateTime.now());
        userEntity.setLastUpdate(LocalDateTime.now());
        userEntity.setId(UUID.randomUUID());
        userEntity.setActive(true);
        application.setId(UUID.randomUUID());
        applicationRoleUserEntity.setApplication(application);
        applicationRoleUserEntity.setUser(userEntity);
        applicationRoleUserEntityId.setApplicationId(applicationRoleUserEntity.getApplication().getId());
        applicationRoleUserEntityId.setUserId(applicationRoleUserEntity.getUser().getId());
        applicationRoleUserEntity.setId(applicationRoleUserEntityId);
        applicationUserSet.add(applicationRoleUserEntity);
        userEntity.setApplicationRoleUser(applicationUserSet);

        when(userRepository.findByAlias(Mockito.anyString())).thenReturn(null);
        when(userMapper.toUserCreateResponse(Mockito.any(UserEntity.class))).thenReturn(getUserCreateResponse(userCreateRequest));
        when(applicationRoleUserRepository.save(Mockito.any(ApplicationRoleUserEntity.class))).thenReturn(applicationRoleUserEntity);
        when(personService.create(Mockito.any(Person.class))).thenReturn(ResponseEntity.status(HttpStatus.CREATED).body(createIfNonExist()));
        when(userMapper.toSource(userCreateRequest)).thenReturn(userEntity);
        when(userRepository.save(any(UserEntity.class))).thenReturn(userEntity);
        when(userMapper.toUserCreateResponse(any(UserEntity.class))).thenReturn(getUserCreateResponse(userCreateRequest));

        ResponseEntity<UserCreateResponse> response = userServiceImpl.create(userCreateRequest);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    private static UserCreateRequest getUserCreateRequest(
            final String alias,
            final String password,
            final String email,
            final UUID roleId,
            final UUID applicationId
    ) {
        return new UserCreateRequest(
                null,
                alias,
                "display name",
                password,
                email,
                true,
                true,
                true,
                true,
                new Person(),
                roleId,
                applicationId
        );
    }

    private static UserCreateResponse getUserCreateResponse(UserCreateRequest userCreateRequest) {
        return new UserCreateResponse(
                UUID.randomUUID(),
                userCreateRequest.alias(),
                userCreateRequest.displayName(),
                userCreateRequest.email(),
                LocalDateTime.now(),
                LocalDateTime.now(),
                userCreateRequest.notificationEmail(),
                userCreateRequest.notificationEmail(),
                userCreateRequest.privacyDataOutActive(),
                userCreateRequest.active(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID()
        );
    }

    private static UserTO getUserTO(String alias, String password) {
        var uuid = UUID.randomUUID();

        UserTO userTO = new UserTO();
        userTO.setActive(true);
        userTO.setAlias(alias);
        userTO.setId(uuid);
        userTO.setPassword(password);
        userTO.setPerson(createIfNonExist());
        userTO.setRoles(new HashSet<>());

        return userTO;
    }

    private static Person createIfNonExist() {
        Person person = new Person();
        var uuid = UUID.randomUUID();
        person.setBirthdate(LocalDate.of(1970, 1, 1));
        person.setFirstName("Jane");
        person.setGender("Gender");
        person.setId(uuid);
        person.setLastName("Doe");
        person.setMiddleName("Middle Name");

        return person;
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
        userEntity.setApplicationRoleUser(new HashSet<>());

        return userEntity;
    }

    @Test
    @DisplayName("Validate alias is available")
    void validateAliasAvailable() {
        var alias = "availableAlias";
        var applicationId = UUID.randomUUID();
        when(userRepository.findByAliasAndApplication(alias, applicationId)).thenReturn(Optional.empty());
        var response = userServiceImpl.validateAlias(alias, applicationId);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    @DisplayName("Validate alias is not available")
    void validateAliasNotAvailable() {
        var alias = "unavailableAlias";
        var applicationId = UUID.randomUUID();
        when(userRepository.findByAliasAndApplication(alias, applicationId)).thenReturn(Optional.of(new UserEntity()));
        var response = userServiceImpl.validateAlias(alias, applicationId);
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    }

    @Test
    @DisplayName("Validate email is available")
    void validateEmailAvailable() {
        var email = "available@example.com";
        var applicationId = UUID.randomUUID();
        when(userRepository.findByEmailAndApplication(email, applicationId)).thenReturn(Optional.empty());
        var response = userServiceImpl.validateEmail(email, applicationId);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    @DisplayName("Validate email is not available")
    void validateEmailNotAvailable() {
        var email = "unavailable@example.com";
        var applicationId = UUID.randomUUID();
        when(userRepository.findByEmailAndApplication(email, applicationId)).thenReturn(Optional.of(new UserEntity()));
        var response = userServiceImpl.validateEmail(email, applicationId);
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    }

}

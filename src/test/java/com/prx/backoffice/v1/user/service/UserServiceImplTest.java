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
package com.prx.backoffice.v1.user.service;

import com.prx.backoffice.MockLoaderBase;
import com.prx.backoffice.v1.person.mapper.PersonMapperImpl;
import com.prx.backoffice.v1.person.service.PersonService;
import com.prx.backoffice.v1.person.service.PersonServiceImpl;
import com.prx.backoffice.v1.role.mapper.RoleMapper;
import com.prx.backoffice.v1.user.mapper.UserMapper;
import com.prx.commons.pojo.Feature;
import com.prx.commons.pojo.Person;
import com.prx.commons.pojo.Role;
import com.prx.commons.pojo.User;
import com.prx.persistence.general.domains.PersonEntity;
import com.prx.persistence.general.domains.RoleEntity;
import com.prx.persistence.general.domains.UserEntity;
import com.prx.persistence.general.domains.UserRoleEntity;
import com.prx.persistence.general.repositories.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.net.URI;
import java.time.LocalDate;
import java.util.ArrayList;

/**
 * UserServiceTest.
 *
 * @author Luis Antonio Mata
 * @version 1.0.0, 27-10-2020
 */
class UserServiceImplTest extends MockLoaderBase {

	@Spy
	PersonService personService = new PersonServiceImpl(null, new PersonMapperImpl());
	@Mock
	UserRepository userRepository;
	@Mock
	UserMapper userMapper;
	@Mock
	RoleMapper roleMapper;
	@InjectMocks
    UserServiceImpl userService;

	UserEntity userEntity;
	PersonEntity personEntity;

	@BeforeEach
	void setup() {
		MockitoAnnotations.openMocks(this);
		userEntity = new UserEntity();
		personEntity = new PersonEntity();

		personEntity.setName("Carlos");
		personEntity.setMiddleName("Ciro");
		personEntity.setLastName("Castro");
		personEntity.setId(1L);
		personEntity.setGender("M");
		personEntity.setBirthdate(LocalDate.parse("1985-05-26"));
		userEntity.setPerson(personEntity);
		userEntity.setPassword("5as46fdas7fs");
		userEntity.setActive(true);
		userEntity.setAlias("ccastro");
		userEntity.setId(1L);


	}

	@Test
	void findUserById() {
        Mockito.when(this.userRepository.findByAlias(ArgumentMatchers.anyString())).thenReturn(userEntity);
        Mockito.when(this.userMapper.toTarget(ArgumentMatchers.any(UserEntity.class))).thenReturn(getUser());
        Assertions.assertNotNull(this.userService.findUserById(12L));
	}

	@Test
	void testAccess() {
		Mockito.when(this.userRepository.findByAlias(ArgumentMatchers.anyString())).thenReturn(userEntity);
		Mockito.when(this.userMapper.toTarget(ArgumentMatchers.any(UserEntity.class))).thenReturn(getUser());
		Assertions.assertNotNull(this.userService.access("ccastro", "123456"));
	}

	@Test
	void findUserByAlias() {
		Mockito.when(this.userRepository.findByAlias(ArgumentMatchers.anyString())).thenReturn(userEntity);
		Mockito.when(this.userMapper.toTarget(ArgumentMatchers.any(UserEntity.class))).thenReturn(getUser());
		Assertions.assertNotNull(this.userService.findUserByAlias("ccastro"));
	}

	@Test
	void testFindAll() {
		Mockito.when(this.userRepository.findByAlias(ArgumentMatchers.anyString())).thenReturn(userEntity);
		Mockito.when(this.userMapper.toTarget(ArgumentMatchers.any(UserEntity.class))).thenReturn(getUser());
		Mockito.when(this.roleMapper.userRoleToRole(ArgumentMatchers.any(UserRoleEntity.class))).thenReturn(getRole());
		Assertions.assertNotNull(this.userService.findUserByAlias("pepe"));
	}

	@Test
	void testCreate() {
		final var user = getUser();
		final var userRoleEntity = new UserRoleEntity();
		final var responseEntity = ResponseEntity.ok().build();
		final var personResponseEntity = ResponseEntity.created(URI.create("")).body(new Person());
		userRoleEntity.setUser(new UserEntity());
		userRoleEntity.setRole(new RoleEntity());

		Mockito.doReturn(personResponseEntity).when(this.personService).create(ArgumentMatchers.any(Person.class));
		Mockito.when(this.userMapper.toSource(ArgumentMatchers.any(User.class))).thenReturn(userEntity);
		Mockito.when(this.userMapper.toTarget(ArgumentMatchers.any(UserEntity.class))).thenReturn(user);
		Assertions.assertEquals(responseEntity, this.userService.create(user));
	}

	@Test
	void create_user_username_required(){
		final var user = getUser();
		user.setAlias("");
		final var httpHeaders = new HttpHeaders();
		httpHeaders.set("error-message", "username is required");
		final ResponseEntity<User> responseEntity = ResponseEntity.badRequest().headers(httpHeaders).build();
		Assertions.assertEquals(responseEntity, this.userService.create(user));
	}

	@Test
	void create_user_password_required(){
		final var user = getUser();
		user.setPassword("");
		final var httpHeaders = new HttpHeaders();
		httpHeaders.set("error-message", "password is required");
		final ResponseEntity<User> responseEntity = ResponseEntity.badRequest().headers(httpHeaders).build();
		Assertions.assertEquals(responseEntity, this.userService.create(user));
	}

	@Test
	void create_role_null() {
		final var user = getUser();
		user.setRoles(null);
		final var httpHeaders = new HttpHeaders();
		httpHeaders.set("error-message", "Role is required");
		final ResponseEntity<User> responseEntity = ResponseEntity.badRequest().headers(httpHeaders).build();
		Assertions.assertEquals(responseEntity, this.userService.create(user));
	}

	@Test
	void create_role_empty() {
		final var user = getUser();
		user.setRoles(new ArrayList<>());
		final var httpHeaders = new HttpHeaders();
		httpHeaders.set("error-message", "Role is required");
		final ResponseEntity<User> responseEntity = ResponseEntity.badRequest().headers(httpHeaders).build();
		Assertions.assertEquals(responseEntity, this.userService.create(user));
	}

	@Test
	void create_user_null(){
		final ResponseEntity<User> responseEntity = ResponseEntity.badRequest().build();
		final var personResponseEntity = ResponseEntity.ok(new Person());
		Assertions.assertEquals(responseEntity, this.userService.create(null));
	}

	private static User getUser(){
		final var user = new User();
		user.setActive(true);
		user.setAlias("pepe");
		user.setId(1L);
		user.setPassword("234567890");
		user.setPerson(getPerson());
		user.setRoles(new ArrayList<>());
		user.getRoles().add(getRole());

		return user;
	}

	private static Person getPerson() {
		final var person = new Person();
		person.setMiddleName("Pepito");
		person.setFirstName("Pepe");
		person.setGender("M");
		person.setLastName("Perez");
		person.setId(1L);
		person.setBirthdate(LocalDate.of(1985, 5, 25));
		return person;
	}

	private static Feature getFeature() {
		final var feature = new Feature();
		feature.setId(1L);
		feature.setActive(true);
		feature.setName("Nombre de feature");
		feature.setDescription("Descripcin de feature");
		return feature;
	}

	private static Role getRole() {
		final var role = new Role();
		role.setId(11L);
		role.setActive(true);
		role.setName("Nombre de rol");
		role.setDescription("Descripcion de rol");
		role.setFeatures(new ArrayList<>());
		role.getFeatures().add(getFeature());
		return role;
	}

}

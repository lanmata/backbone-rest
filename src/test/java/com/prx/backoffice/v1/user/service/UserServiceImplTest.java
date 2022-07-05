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
import com.prx.backoffice.v1.user.api.to.UserTO;
import com.prx.backoffice.v1.user.mapper.UserMapper;
import com.prx.backoffice.v1.util.UserTemplateTest;
import com.prx.commons.pojo.Feature;
import com.prx.commons.pojo.Person;
import com.prx.commons.pojo.Role;
import com.prx.persistence.general.domains.RoleEntity;
import com.prx.persistence.general.domains.UserEntity;
import com.prx.persistence.general.domains.UserRoleEntity;
import com.prx.persistence.general.repositories.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

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

	@BeforeEach
	void setup() {
		MockitoAnnotations.openMocks(this);
	}

	@Test
	void findUserById() {
        Mockito.when(this.userRepository.findByAlias(ArgumentMatchers.anyString())).thenReturn(UserTemplateTest.USER.getEntity());
        Mockito.when(this.userMapper.toTarget(ArgumentMatchers.any(UserEntity.class))).thenReturn(UserTemplateTest.USER.getModel());
        Assertions.assertNotNull(this.userService.findUserById(12L));
	}

	@Test
	void testAccess() {
		Mockito.when(this.userRepository.findByAlias(ArgumentMatchers.anyString())).thenReturn(UserTemplateTest.USER.getEntity());
		Mockito.when(this.userMapper.toTarget(ArgumentMatchers.any(UserEntity.class))).thenReturn(UserTemplateTest.USER.getModel());
		Assertions.assertNotNull(this.userService.access("ccastro", "123456"));
	}

	@Test
	void findUserByAlias() {
		Mockito.when(this.userRepository.findByAlias(ArgumentMatchers.anyString())).thenReturn(UserTemplateTest.USER.getEntity());
		Mockito.when(this.userMapper.toTarget(ArgumentMatchers.any(UserEntity.class))).thenReturn(UserTemplateTest.USER.getModel());
		Assertions.assertNotNull(this.userService.findUserByAlias("ccastro"));
	}

	@Test
	void testFindAll() {
		Mockito.when(this.userRepository.findAll()).thenReturn(List.of(UserTemplateTest.USER.getEntity()));
		Mockito.when(this.userMapper.toTarget(ArgumentMatchers.any(UserEntity.class)))
				.thenReturn(UserTemplateTest.USER.getModel());
//		Mockito.when(this.roleMapper.userRoleToRole(ArgumentMatchers.any(UserRoleEntity.class)))
//				.thenReturn(RoleTemplateTest.ROLE_TO.getModel());
		Assertions.assertNotNull(this.userService.findAll());
	}

	@Test
	void testFindAll_empty() {
		Mockito.when(this.userRepository.findAll()).thenReturn(Collections.emptyList());
		Mockito.when(this.userMapper.toTarget(ArgumentMatchers.any(UserEntity.class)))
				.thenReturn(UserTemplateTest.USER.getModel());
//		Mockito.when(this.roleMapper.userRoleToRole(ArgumentMatchers.any(UserRoleEntity.class)))
//				.thenReturn(RoleTemplateTest.ROLE_TO.getModel());
		Assertions.assertNotNull(this.userService.findAll());
	}

	@Test
	void testCreate() {
		final var user = UserTemplateTest.USER.getModel();
		final var userRoleEntity = new UserRoleEntity();
		final var personResponseEntity = ResponseEntity.ok(new Person());
		userRoleEntity.setUser(new UserEntity());
		userRoleEntity.setRole(new RoleEntity());

		Mockito.doReturn(personResponseEntity).when(this.personService).create(ArgumentMatchers.any(Person.class));
		Mockito.when(this.userMapper.toSource(ArgumentMatchers.any(UserTO.class))).thenReturn(UserTemplateTest.USER.getEntity());
		Mockito.when(this.userMapper.toTarget(ArgumentMatchers.any(UserEntity.class))).thenReturn(user);
		Assertions.assertNotNull(this.userService.create(user));
	}

	@Test
	void create_user_username_required(){
		final var user = UserTemplateTest.USER.getModel();
		user.setAlias("");
		final var httpHeaders = new HttpHeaders();
		httpHeaders.set(HttpHeaders.WARNING, "username is required");
		final ResponseEntity<UserTO> responseEntity = ResponseEntity.badRequest().headers(httpHeaders).build();
		Assertions.assertEquals(responseEntity, this.userService.create(user));
	}

	@Test
	void create_user_password_required(){
		final var user = UserTemplateTest.USER.getModel();
		user.setPassword("");
		final var httpHeaders = new HttpHeaders();
		httpHeaders.set(HttpHeaders.WARNING, "password is required");
		final ResponseEntity<UserTO> responseEntity = ResponseEntity.badRequest().headers(httpHeaders).build();
		Assertions.assertEquals(responseEntity, this.userService.create(user));
	}

	@Test
	void create_role_null() {
		final var user = UserTemplateTest.USER.getModel();
		user.setRoles(null);
		final var httpHeaders = new HttpHeaders();
		httpHeaders.set(HttpHeaders.WARNING, "Role is required");
		final ResponseEntity<UserTO> responseEntity = ResponseEntity.badRequest().headers(httpHeaders).build();
		Assertions.assertEquals(responseEntity, this.userService.create(user));
	}

	@Test
	void create_role_empty() {
		final var user = UserTemplateTest.USER.getModel();
		user.setRoles(new HashSet<>());
		final var httpHeaders = new HttpHeaders();
		httpHeaders.set(HttpHeaders.WARNING, "Role is required");
		final ResponseEntity<UserTO> responseEntity = ResponseEntity.badRequest().headers(httpHeaders).build();
		Assertions.assertEquals(responseEntity, this.userService.create(user));
	}

	@Test
	void create_user_null(){
		final ResponseEntity<UserTO> responseEntity = ResponseEntity.badRequest().build();
		Assertions.assertEquals(responseEntity, this.userService.create(null));
	}

	private static UserTO getUser(){
		final var user = new UserTO();
		user.setActive(true);
		user.setAlias("pepe");
		user.setId(1L);
		user.setPassword("234567890");
		user.setPerson(getPerson());
		user.setRoles(new HashSet<>());
		user.getRoles().add(1L);

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

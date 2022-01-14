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
import com.prx.backoffice.v1.person.service.PersonService;
import com.prx.backoffice.v1.role.mapper.RoleMapper;
import com.prx.backoffice.v1.user.service.UserServiceImpl;
import com.prx.backoffice.v1.user.mapper.UserMapper;
import com.prx.commons.pojo.*;
import com.prx.persistence.general.domains.PersonEntity;
import com.prx.persistence.general.domains.RoleEntity;
import com.prx.persistence.general.domains.UserEntity;
import com.prx.persistence.general.domains.UserRoleEntity;
import com.prx.persistence.general.repositories.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.ArrayList;

/**
 * UserServiceTest.
 *
 * @author Luis Antonio Mata
 * @version 1.0.0, 27-10-2020
 */
@SpringBootTest
@ActiveProfiles("local")
class UserServiceImplTest extends MockLoaderBase {

	@Mock
	PersonService personService;
	@Mock
	UserRepository userRepository;
	@Mock
	UserMapper userMapper;
	@Mock
	RoleMapper roleMapper;
	@InjectMocks
    UserServiceImpl userService;

	Role role;
	User user;
	Person person;
	Feature feature;
	UserEntity userEntity;
	PersonEntity personEntity;

	@BeforeEach
	void setup() {
		role = new Role();
		user = new User();
		person = new Person();
		feature = new Feature();
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
		feature.setId(1L);
		feature.setActive(true);
		feature.setName("Nombre de feature");
		feature.setDescription("Descripcin de feature");
		role.setId(11L);
		role.setActive(true);
		role.setName("Nombre de rol");
		role.setDescription("Descripcion de rol");
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
	void findUserById() {
        Mockito.when(this.userRepository.findByAlias(ArgumentMatchers.anyString())).thenReturn(userEntity);
        Mockito.when(this.userMapper.toTarget(ArgumentMatchers.any(UserEntity.class))).thenReturn(user);
        Assertions.assertNotNull(this.userService.findUserById(12L));
	}

	@Test
	void testAccess() {
		Mockito.when(this.userRepository.findByAlias(ArgumentMatchers.anyString())).thenReturn(userEntity);
		Mockito.when(this.userMapper.toTarget(ArgumentMatchers.any(UserEntity.class))).thenReturn(user);
		Assertions.assertNotNull(this.userService.access("ccastro", "123456"));
	}

	@Test
	void findUserByAlias() {
		Mockito.when(this.userRepository.findByAlias(ArgumentMatchers.anyString())).thenReturn(userEntity);
		Mockito.when(this.userMapper.toTarget(ArgumentMatchers.any(UserEntity.class))).thenReturn(user);
		Assertions.assertNotNull(this.userService.findUserByAlias("ccastro"));
	}

	@Test
	void testFindAll() {
		Mockito.when(this.userRepository.findByAlias(ArgumentMatchers.anyString())).thenReturn(userEntity);
		Mockito.when(this.userMapper.toTarget(ArgumentMatchers.any(UserEntity.class))).thenReturn(user);
		Mockito.when(this.roleMapper.userRoletoRole(ArgumentMatchers.any(UserRoleEntity.class))).thenReturn(role);
		Assertions.assertNotNull(this.userService.findUserByAlias("pepe"));
	}

	@Test
	void testCreate() {
		final var userRoleEntity = new UserRoleEntity();
		final ResponseEntity<User> responseEntity;
		final ResponseEntity<Person> personResponseEntity;

		personResponseEntity = ResponseEntity.noContent().build();
		responseEntity = new ResponseEntity<User>(user, HttpStatus.ACCEPTED);
		userRoleEntity.setUser(new UserEntity());
		userRoleEntity.setRole(new RoleEntity());

		Mockito.when(this.userMapper.toSource(ArgumentMatchers.any(User.class))).thenReturn(userEntity);
		Mockito.when(this.userMapper.toTarget(ArgumentMatchers.any(UserEntity.class))).thenReturn(user);
		Mockito.when(this.personService.create(ArgumentMatchers.any(Person.class))).thenReturn(personResponseEntity);
		Mockito.when(this.personService.find(ArgumentMatchers.any(Long.class))).thenReturn(personResponseEntity);
//		Assertions.assertNotNull(this.userService.create(user));
	}

}

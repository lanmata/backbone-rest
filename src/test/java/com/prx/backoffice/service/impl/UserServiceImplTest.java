/*
 *
 *  * @(#)UserServiceImplTest.java.
 *  *
 *  * Copyright (c) Luis Antonio Mata Mata. All rights reserved.
 *  *
 *  * All rights to this product are owned by Luis Antonio Mata Mata and may only
 *  * be used under the terms of its associated license document. You may NOT
 *  * copy, modify, sublicense, or distribute this source file or portions of
 *  * it unless previously authorized in writing by Luis Antonio Mata Mata.
 *  * In any event, this notice and the above copyright must always be included
 *  * verbatim with this file.
 *
 */
package com.prx.backoffice.service.impl;

import com.prx.backoffice.MockLoaderBase;
import com.prx.backoffice.mapper.RolMapper;
import com.prx.backoffice.mapper.UserMapper;
import com.prx.backoffice.service.PersonService;
import com.prx.commons.pojo.*;
import com.prx.persistence.general.domains.PersonEntity;
import com.prx.persistence.general.domains.RolEntity;
import com.prx.persistence.general.domains.UserEntity;
import com.prx.persistence.general.domains.UserRolEntity;
import com.prx.persistence.general.repositories.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.time.LocalDate;
import java.util.ArrayList;

/**
 * UserServiceTest.
 *
 * @author Luis Antonio Mata
 * @version 1.0.0, 27-10-2020
 */
public class UserServiceImplTest extends MockLoaderBase {

	@Mock
	PersonService personService;
	@Mock
	UserRepository userRepository;
	@Mock
	UserMapper userMapper;
	@Mock
	RolMapper rolMapper;
	@InjectMocks
	UserServiceImpl userService;

	Rol rol;
	User user;
	Person person;
	Feature feature;
	UserEntity userEntity;
	PersonEntity personEntity;

	@BeforeEach
	void setup() {
		rol = new Rol();
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

	}

	@Test
	public void findUserById() {
        Mockito.when(this.userRepository.findByAlias(ArgumentMatchers.anyString())).thenReturn(userEntity);
        Mockito.when(this.userMapper.toTarget(ArgumentMatchers.any(UserEntity.class))).thenReturn(user);
        Assertions.assertNotNull(this.userService.findUserById(12L));
	}

	@Test
	public void testAccess() {
		Mockito.when(this.userRepository.findByAlias(ArgumentMatchers.anyString())).thenReturn(userEntity);
		Mockito.when(this.userMapper.toTarget(ArgumentMatchers.any(UserEntity.class))).thenReturn(user);
		Assertions.assertNotNull(this.userService.access("ccastro", "123456"));
	}

	@Test
	public void findUserByAlias() {
		Mockito.when(this.userRepository.findByAlias(ArgumentMatchers.anyString())).thenReturn(userEntity);
		Mockito.when(this.userMapper.toTarget(ArgumentMatchers.any(UserEntity.class))).thenReturn(user);
		Assertions.assertNotNull(this.userService.findUserByAlias("ccastro"));
	}

	@Test
	public void testFindAll() {
		Mockito.when(this.userRepository.findByAlias(ArgumentMatchers.anyString())).thenReturn(userEntity);
		Mockito.when(this.userMapper.toTarget(ArgumentMatchers.any(UserEntity.class))).thenReturn(user);
		Mockito.when(this.rolMapper.userRoltoRol(ArgumentMatchers.any(UserRolEntity.class))).thenReturn(rol);
		Assertions.assertNotNull(this.userService.findUserByAlias("pepe"));
	}

	@Test
	public void testCreate() {
		final var userRolEntity = new UserRolEntity();
		final var messageActivityUser = new MessageActivity<User>();
		final var messageActivityPerson = new MessageActivity<Person>();

		messageActivityPerson.setCode(404);
		messageActivityPerson.setObjectResponse(null);
		messageActivityPerson.setMessage("No existe persona");
		messageActivityUser.setCode(200);
		messageActivityUser.setMessage("Usuario encontrado");
		messageActivityUser.setObjectResponse(user);
		userRolEntity.setUser(new UserEntity());
		userRolEntity.setRol(new RolEntity());

		Mockito.when(this.userMapper.toSource(ArgumentMatchers.any(User.class))).thenReturn(userEntity);
		Mockito.when(this.userMapper.toTarget(ArgumentMatchers.any(UserEntity.class))).thenReturn(user);
		Mockito.when(this.personService.create(ArgumentMatchers.any(Person.class))).thenReturn(messageActivityPerson);
		Mockito.when(this.personService.find(ArgumentMatchers.any(Person.class))).thenReturn(messageActivityPerson);
		Assertions.assertNotNull(this.userService.create(user));
	}

}
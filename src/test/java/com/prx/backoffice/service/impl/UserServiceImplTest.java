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

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;

import com.prx.backoffice.MockLoaderBase;
import com.prx.backoffice.mapper.UserMapper;
import com.prx.backoffice.service.PersonService;
import com.prx.commons.pojo.Feature;
import com.prx.commons.pojo.MessageActivity;
import com.prx.commons.pojo.Person;
import com.prx.commons.pojo.Rol;
import com.prx.commons.pojo.User;
import com.prx.persistence.general.domains.PersonEntity;
import com.prx.persistence.general.domains.RolEntity;
import com.prx.persistence.general.domains.UserEntity;
import com.prx.persistence.general.domains.UserRolEntity;
import com.prx.persistence.general.repositories.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;

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
	@InjectMocks
	UserServiceImpl userService;

	@Test
	public void findUserById() {
//        final var userEntity = new UserEntity();
//        final var personEntity = new PersonEntity();
//
//        personEntity.setName("Carlos");
//        personEntity.setMiddleName("Ciro");
//        personEntity.setLastName("Castro");
//        personEntity.setId(1L);
//        personEntity.setGender("M");
//        personEntity.setBirthdate(LocalDate.parse("1985-05-26"));
//        userEntity.setPerson(personEntity);
//        userEntity.setPassword("5as46fdas7fs");
//        userEntity.setActive(true);
//        userEntity.setAlias("ccastro");
//        userEntity.setId(1L);
//        final var user = this.userMapper.toTarget(userEntity);

//        Mockito.when(this.userRepository.findByAlias(ArgumentMatchers.anyString())).thenReturn(userEntity);
//        Mockito.when(this.userMapper.toTarget(ArgumentMatchers.any(UserEntity.class))).thenReturn(user);
//        Assertions.assertNotNull(this.userService.findUserByAlias("ccastro"));
	}

	@Test
	public void testAccess() {
	}

	@Test
	public void testFindAll() {
		final var userEntity = new UserEntity();
		final var personEntity = new PersonEntity();

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

		Mockito.when(this.userRepository.findByAlias(ArgumentMatchers.anyString())).thenReturn(userEntity);
		Assertions.assertNotNull(this.userService.findUserByAlias("pepe"));
	}

	@Test
	public void testCreate() {
		final var rol = new Rol();
		final var user = new User();
		final var person = new Person();
		final var feature = new Feature();
		final var userEntity = new UserEntity();
		final var userRolEntity = new UserRolEntity();
		final var messageActivityUser = new MessageActivity<User>();
		final var messageActivityPerson = new MessageActivity<Person>();

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
		messageActivityPerson.setCode(404);
		messageActivityPerson.setObjectResponse(null);
		messageActivityPerson.setMessage("No existe persona");
		messageActivityUser.setCode(200);
		messageActivityUser.setMessage("Usuario encontrado");
		messageActivityUser.setObjectResponse(user);
		userEntity.setId(user.getId());
		userRolEntity.setUser(new UserEntity());
		userRolEntity.setRol(new RolEntity());
		userEntity.setAlias(user.getAlias());
		userEntity.setActive(user.isActive());
		userEntity.setPassword(user.getPassword());
		userEntity.setUserRol(new HashSet<>());
		userEntity.getUserRol().add(userRolEntity);

		Mockito.when(this.userMapper.toSource(ArgumentMatchers.any(User.class))).thenReturn(userEntity);
		Mockito.when(this.personService.create(ArgumentMatchers.any(Person.class))).thenReturn(messageActivityPerson);
		Mockito.when(this.personService.find(ArgumentMatchers.any(Person.class))).thenReturn(messageActivityPerson);
		Assertions.assertNotNull(this.userService.create(user));
	}

}
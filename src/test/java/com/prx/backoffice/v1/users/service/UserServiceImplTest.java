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
import com.prx.backoffice.v1.people.mapper.PersonMapperImpl;
import com.prx.backoffice.v1.people.service.PersonService;
import com.prx.backoffice.v1.people.service.PersonServiceImpl;
import com.prx.backoffice.v1.roles.mapper.RoleMapper;
import com.prx.backoffice.v1.users.api.to.UserTO;
import com.prx.backoffice.v1.users.mapper.UserMapper;
import com.prx.backoffice.v1.util.UserTemplateTest;
import com.prx.commons.pojo.Person;
import com.prx.persistence.general.domains.RoleEntity;
import com.prx.persistence.general.domains.UserEntity;
import com.prx.persistence.general.domains.UserRoleEntity;
import com.prx.persistence.general.repositories.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

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
	@Autowired
	UserMapper userMapper;
	@Autowired
	RoleMapper roleMapper;
	@InjectMocks
    UserServiceImpl userService;

	@BeforeEach
	void setup() {
		MockitoAnnotations.openMocks(this);
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
	void create_user_null(){
		final ResponseEntity<UserTO> responseEntity = ResponseEntity.badRequest().build();
		Assertions.assertEquals(responseEntity, this.userService.create(null));
	}


}

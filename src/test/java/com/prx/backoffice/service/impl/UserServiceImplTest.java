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

import com.prx.backoffice.PrxBackofficeRestApplication;
import com.prx.backoffice.mapper.UserMapper;
import com.prx.persistence.general.domains.PersonEntity;
import com.prx.persistence.general.domains.UserEntity;
import com.prx.persistence.general.repositories.UserRepository;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDate;

/**
 * UserServiceTest.
 *
 * @author Luis Antonio Mata
 * @version 1.0.0, 27-10-2020
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = PrxBackofficeRestApplication.class)
class UserServiceImplTest {

    @Mock
    UserRepository userRepository;
    @Mock
    UserMapper userMapper;

    @InjectMocks
    UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void findUserById() {
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

        when(userRepository.findByAlias(ArgumentMatchers.anyString())).thenReturn(userEntity);
        doAnswer(invocationOnMock -> userMapper.toTarget(invocationOnMock.getArgument(0))).when(userMapper).toTarget(userEntity);
        assertNotNull(userService.findUserByAlias("ccastro"));
    }

    @Test
    void access() {
    }

    @Test
    void findAll() {
    }

    @Test
    void create() {
    }

}
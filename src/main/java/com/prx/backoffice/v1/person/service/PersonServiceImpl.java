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

package com.prx.backoffice.v1.person.service;

import com.prx.backoffice.enums.keys.UserMessageKey;
import com.prx.backoffice.v1.person.mapper.PersonMapper;
import com.prx.backoffice.util.MessageUtil;
import com.prx.commons.exception.StandardException;
import com.prx.commons.pojo.Person;
import com.prx.persistence.general.domains.PersonEntity;
import com.prx.persistence.general.repositories.PersonRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.prx.commons.util.ValidatorCommonsUtil.esNulo;

/**
 * Modelo para la gesti&oacute;n de persona.
 *
 * @author <a href="mailto:luis.antonio.mata@gmail.com">Luis Antonio Mata.</a>
 * @version 1.0.1.20200904-01, 2019-11-14
 */
@Service
@RequiredArgsConstructor
public class PersonServiceImpl implements PersonService {
    private final PersonRepository personRepository;
    private final PersonMapper personMapper;
    private static final Logger LOGGER = LoggerFactory.getLogger(PersonServiceImpl.class);

	/** {@inheritDoc} */
    public ResponseEntity<Person> create(Person person) {
    	final var responseEntity = save(person);
		return new ResponseEntity<>(
				personMapper.toTarget(responseEntity.getBody()), responseEntity.getStatusCode());
	}

	@Override
	public ResponseEntity<Person> update(Person person) {
		return null;
	}

	@Override
	public ResponseEntity<Person> delete(Person person) {
		return null;
	}

	@Override
	public ResponseEntity<Person> find(Long id) {
		return null;
	}

	@Override
	public ResponseEntity<List<Person>> list(Long... id) {
		return null;
	}

	/** {@inheritDoc} */
    public ResponseEntity<PersonEntity> save(Person person) {
		ResponseEntity<PersonEntity> responseEntity;
		if (esNulo(person)) {
			responseEntity = ResponseEntity.notFound().build();
		} else {
			responseEntity = new ResponseEntity<>(personRepository.save(personMapper.toSource(person)), HttpStatus.CREATED);
		}
		LOGGER.info(responseEntity.getStatusCode().toString());
		return responseEntity;
	}

    /** {@inheritDoc} */
    public ResponseEntity<Person> find(Person person) {
		ResponseEntity<Person> responseEntity;
        try {
            final var personResult = personRepository.findByFirstNameMiddleNameLastName(person.getFirstName(),
                person.getMiddleName(), person.getLastName());
            if (esNulo(personResult)) {
            	responseEntity = ResponseEntity.notFound().build();
			} else {
				responseEntity = new ResponseEntity<>(personMapper.toTarget(personResult), HttpStatus.FOUND);
			}
            LOGGER.info("{} {} {}",responseEntity.getStatusCode() , MessageUtil.LOG_PATH_SEPARATOR , person);
            return responseEntity;
        } catch (Exception e) {
            LOGGER.warn("Se ha producido un error inesperado");
            throw new StandardException(UserMessageKey.USER_CREATE_ERROR, e);
        }
    }

}

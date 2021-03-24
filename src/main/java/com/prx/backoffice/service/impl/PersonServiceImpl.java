/*
 *
 *  * @(#)PersonServiceImpl.java.
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

import javax.ws.rs.core.Response;

import com.prx.backoffice.enums.keys.UserMessageKey;
import com.prx.backoffice.mapper.PersonMapper;
import com.prx.backoffice.service.PersonService;
import com.prx.commons.exception.StandardException;
import com.prx.commons.pojo.MessageActivity;
import com.prx.commons.pojo.Person;
import com.prx.persistence.general.domains.PersonEntity;
import com.prx.persistence.general.repositories.PersonRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Service;

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
    public MessageActivity<Person> create(Person person) {
		var messageActivity = save(person);
		var messageActivityRs = new MessageActivity<Person>();

		messageActivityRs.setCode(messageActivity.getCode());
		messageActivityRs.setMessage(messageActivity.getMessage());
		messageActivityRs.setObjectResponse(personMapper.toTarget(messageActivity.getObjectResponse()));

		return messageActivityRs;
	}

    /** {@inheritDoc} */
    public MessageActivity<PersonEntity> save(Person person) {
		final var messageActivity = new MessageActivity<PersonEntity>();

		if (esNulo(person)) {
			messageActivity.setCode(Response.Status.NO_CONTENT.getStatusCode());
			messageActivity.setMessage(Response.Status.NO_CONTENT.toString());
		}
		else {
			messageActivity.setObjectResponse(personRepository.save(personMapper.toSource(person)));
			messageActivity.setCode(Response.Status.CREATED.getStatusCode());
			messageActivity.setMessage(Response.Status.CREATED.toString());
		}

		return messageActivity;
	}

    /** {@inheritDoc} */
    public MessageActivity<Person> find(Person person) {
        try {
            final var messageActivity = new MessageActivity<Person>();
            final var personResult = personRepository.findByFirstNameMiddleNameLastName(person.getFirstName(),
                person.getMiddleName(), person.getLastName());
            if (!esNulo(personResult)) {
				messageActivity.setObjectResponse(personMapper.toTarget(personResult));
				messageActivity.setCode(Response.Status.CREATED.getStatusCode());
				messageActivity.setMessage(Response.Status.CREATED.toString());
			}

            return messageActivity;
        } catch (Exception e) {
            LOGGER.warn("Se ha producido un error inesperado");
            throw new StandardException(UserMessageKey.USER_CREATE_ERROR, e);
        }
    }

}

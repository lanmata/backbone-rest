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

package com.prx.backoffice.v1.people.service;

import com.prx.backoffice.util.MessageUtil;
import com.prx.backoffice.v1.people.mapper.PersonMapper;
import com.prx.commons.pojo.Person;
import com.prx.persistence.general.domains.PersonEntity;
import com.prx.persistence.general.repositories.PersonRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.*;

import static com.prx.commons.util.ValidatorCommonsUtil.esNulo;

/**
 * Modelo para la gesti&oacute;n de persona.
 *
 * @author <a href="mailto:luis.antonio.mata@gmail.com">Luis Antonio Mata.</a>
 * @version 1.0.1.20200904-01, 2019-11-14
 */
@Service
public class PersonServiceImpl implements PersonService {
	private final PersonRepository personRepository;
	private final PersonMapper personMapper;
	private static final Logger LOGGER = LoggerFactory.getLogger(PersonServiceImpl.class);

	public PersonServiceImpl(PersonRepository personRepository, PersonMapper personMapper) {
		this.personRepository = personRepository;
		this.personMapper = personMapper;
	}

	/** {@inheritDoc} */
	public ResponseEntity<Person> create(Person person) {
		ResponseEntity<PersonEntity> responseEntity = esNulo(person) ?
				ResponseEntity.notFound().build(): new ResponseEntity<>(personRepository
				.save(personMapper.toSource(person)), HttpStatus.CREATED);
		LOGGER.info(responseEntity.getStatusCode().toString());
		return new ResponseEntity<>(
				personMapper.toTarget(responseEntity.getBody()), responseEntity.getStatusCode());
	}

	@Override
	public ResponseEntity<Person> update(String personId, Person person) {
		if(esNulo(personId)) {
			return ResponseEntity.badRequest().header(MessageUtil.MESSAGE_HEADER_STR, "PersonId invalid").build();
		}
		if(esNulo(person)){
			return ResponseEntity.badRequest().header(MessageUtil.MESSAGE_HEADER_STR, "Person request invalid").build();
		}
		if(personRepository.findById(UUID.fromString(personId)).isEmpty()){
			return ResponseEntity.badRequest().header(MessageUtil.MESSAGE_HEADER_STR, "Person not founded").build();
		}
		var newValuePersonEntity = personMapper.toSource(person);
		newValuePersonEntity.setId(UUID.fromString(personId));
		return ResponseEntity.ok(personMapper.toTarget(personRepository.save(newValuePersonEntity)));
	}

	@Override
	public ResponseEntity<Person> find(String personId) {
		if (esNulo(personId)) {
			return ResponseEntity.unprocessableEntity().build();
		}
		var personEntity = personRepository.findById(UUID.fromString(personId));
		return personEntity.map(entity -> ResponseEntity.ok(personMapper.toTarget(entity)))
				.orElseGet(() -> ResponseEntity.notFound().build());
	}

	@Override
	public ResponseEntity<List<Person>> list(String... ids) {
		Iterable<PersonEntity> personEntityListResult;
		List<Person> personList = new ArrayList<>();
		List<UUID> uuidList = new ArrayList<>();
		if(Objects.nonNull(ids) && Objects.nonNull(ids[0])){
			Arrays.stream(ids).toList().forEach(s -> uuidList.add(UUID.fromString(s)));
		}
		personEntityListResult = uuidList.isEmpty() ? personRepository.findAll():personRepository.findAllById(uuidList);
		personEntityListResult.forEach(personEntity ->
				personList.add(personMapper.toTarget(personEntity))
		);
		return personList.isEmpty() ? ResponseEntity.notFound().build(): ResponseEntity.ok(sort(personList));
	}

	private List<Person> sort(List<Person> people) {
		return people.stream().sorted(Comparator.comparing(Person::getId)).toList();
	}

}

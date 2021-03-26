/*
 * @(#)PersonCreateRequestTest.java.
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

package com.prx.backoffice.to.person;

import java.time.LocalDate;

import com.prx.commons.pojo.Person;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * PersonCreateRequestTest.
 *
 * @author <a href='mailto:luis.antonio.mata@gmail.com'>Luis Antonio Mata</a>
 * @version 1.0.0, 18-02-2021
 */
public class PersonCreateRequestTest {

	@Test
	public void testGettersAndSetters() {
		final var personCreateRequest = new PersonCreateRequest();
		final var person = new Person();
		person.setBirthdate(LocalDate.of(1979, 4, 14));
		person.setFirstName("Pepe");
		person.setGender("M");
		person.setId(1);
		person.setLastName("Perez");
		person.setMiddleName("Peter");
		personCreateRequest.setPerson(person);

		assertAll(() -> assertNotNull(personCreateRequest),
				() -> assertNotNull(personCreateRequest.getPerson()),
				() -> assertEquals(1L, personCreateRequest.getPerson().getId()),
				() -> assertNotNull(personCreateRequest.getPerson().getGender()),
				() -> assertNotNull(personCreateRequest.getPerson().getBirthdate()),
				() -> assertNotNull(personCreateRequest.getPerson().getFirstName()),
				() -> assertNotNull(personCreateRequest.getPerson().getMiddleName()),
				() -> assertNotNull(personCreateRequest.getPerson().getLastName()),
				() -> assertNotNull(personCreateRequest.getPerson().toString()),
				() -> assertNotNull(personCreateRequest.toString())
		);
	}

}
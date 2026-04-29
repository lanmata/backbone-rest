/*
 *  @(#)PersonRequestTest.java
 *
 *  Copyright (c) Luis Antonio Mata Mata. All rights reserved.
 *
 *   All rights to this product are owned by Luis Antonio Mata Mata and may only
 *  be used under the terms of its associated license document. You may NOT
 *  copy, modify, sublicense, or distribute this source file or portions of
 *  it unless previously authorized in writing by Luis Antonio Mata Mata.
 *  In any event, this notice and the above copyright must always be included
 *  verbatim with this file.
 */

package com.umdc.backoffice.v1.people.api.to;

import com.prx.commons.general.pojo.Person;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * PersonCreateRequestTest.
 *
 * @author <a href='mailto:luis.antonio.mata@gmail.com'>Luis Antonio Mata</a>
 * @version 1.0.0, 18-02-2021
 */
class PersonRequestTest {

    @Test
    @DisplayName("Test getters and setters of PersonRequest")
    void testGettersAndSetters() {
        final var uuid = UUID.fromString("b1c4fbd2-a6b1-4615-af78-d2a037f66d29");
        final var personCreateRequest = new PersonRequest();
        final var person = new Person();
        person.setBirthdate(LocalDate.of(1979, 4, 14));
        person.setFirstName("Pepe");
        person.setGender("M");
        person.setId(uuid);
        person.setLastName("Perez");
        person.setMiddleName("Peter");
        personCreateRequest.setPerson(person);

        assertAll(() -> assertNotNull(personCreateRequest),
                () -> assertNotNull(personCreateRequest.getPerson()),
                () -> assertEquals(uuid, personCreateRequest.getPerson().getId()),
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

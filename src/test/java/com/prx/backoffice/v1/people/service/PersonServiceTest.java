/*
 *  @(#)PersonServiceTest.java
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

package com.prx.backoffice.v1.people.service;

import com.prx.commons.general.pojo.Person;
import org.apache.commons.lang.NotImplementedException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class PersonServiceTest {

    private final PersonService personService = new PersonService() {
    };

    @Test
    @DisplayName("Test deleting a person")
    void delete() {
        assertThrows(NotImplementedException.class, () -> personService.delete(UUID.randomUUID(), new Person()));
    }
}

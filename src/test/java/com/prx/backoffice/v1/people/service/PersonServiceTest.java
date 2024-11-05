package com.prx.backoffice.v1.people.service;

import com.prx.commons.pojo.Person;
import org.apache.commons.lang.NotImplementedException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PersonServiceTest {

    private final PersonService personService = new PersonService() {
    };

    @Test
    @DisplayName("Test deleting a person")
    void delete() {
        assertThrows(NotImplementedException.class, () -> personService.delete("abc11", new Person()));
    }
}

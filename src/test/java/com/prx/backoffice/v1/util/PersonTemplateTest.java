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

package com.prx.backoffice.v1.util;

import com.prx.commons.pojo.Person;
import com.prx.persistence.general.domains.PersonEntity;

import java.time.LocalDate;

/**
 * PersonTemplateTest.
 *
 * @author Luis Antonio Mata
 * @version 1.0.0, 14-05-2022
 * @since 11
 */
public enum PersonTemplateTest implements TemplateUtil<Person, PersonEntity>{
    PERSON {
        @Override
        public Person getModel() {
            final var person = new Person();
            person.setMiddleName("Pepito");
            person.setFirstName("Pepe");
            person.setGender("M");
            person.setLastName("Perez");
            person.setId(1L);
            person.setBirthdate(LocalDate.of(1985, 5, 25));
            return person;
        }

        @Override
        public PersonEntity getEntity() {
            final var personEntity = new PersonEntity();
            personEntity.setName("Carlos");
            personEntity.setMiddleName("Ciro");
            personEntity.setLastName("Castro");
            personEntity.setId(1L);
            personEntity.setGender("M");
            personEntity.setBirthdate(LocalDate.parse("1985-05-26"));
            return personEntity;
        }
    }
}

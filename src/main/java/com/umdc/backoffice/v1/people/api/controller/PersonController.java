/*
 *  @(#)PersonController.java
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
package com.umdc.backoffice.v1.people.api.controller;

import com.umdc.backoffice.v1.people.api.to.PersonRequest;
import com.umdc.backoffice.v1.people.service.PersonService;
import com.prx.commons.general.pojo.Person;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * PersonController. Clase controladora para la exposición de los endpoint pertenecientes a la gestión de usuario
 *
 * @author Luis Antonio Mata
 * @version 1.0.1.20200904-01, 04-11-2020
 */
@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/v1/people")
public class PersonController implements PersonApi {
    /**
     * personService
     */
    private final PersonService personService;

    public PersonController(PersonService personService) {
        this.personService = personService;
    }

    @Override
    public PersonService getService() {
        return this.personService;
    }

    @Override
    public ResponseEntity<Person> create(final PersonRequest personRequest) {
        return personService.create(personRequest.getPerson());
    }

    @Override
    public ResponseEntity<Person> find(final UUID personId) {
        return personService.find(personId);
    }

    @Override
    public ResponseEntity<Person> update(final UUID personId, final PersonRequest personRequest) {
        return personService.update(personId, personRequest.getPerson());
    }

    @Override
    public ResponseEntity<List<Person>> list() {
        return personService.list((UUID) null);
    }
}

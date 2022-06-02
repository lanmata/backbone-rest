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
package com.prx.backoffice.v1.person.api.controller;

import com.prx.backoffice.util.MessageUtil;
import com.prx.backoffice.v1.person.api.to.PersonRequest;
import com.prx.backoffice.v1.person.service.PersonService;
import com.prx.commons.pojo.Person;
import com.prx.commons.to.Response;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * PersonController. Clase controladora para la exposición de los endpoint pertenecientes a la gestión de usuario
 *
 * @author Luis Antonio Mata
 * @version 1.0.1.20200904-01, 04-11-2020
 */
@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@RequestMapping("v1/person")
public class PersonController {
    /** personService */
    private final PersonService personService;

    /**
     *
     * @param personRequest {@link PersonRequest}
     * @return Objeto de tipo {@link Response}
     */
    @Operation(description = "Create a persona")
    @ApiResponses(value = {
        @ApiResponse(responseCode = MessageUtil.OK_VALUE, description = "Person created")
    })
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE, path = "/")
    public ResponseEntity<Person> create(@Parameter(description = "Request to create a person", required = true)
    @RequestBody final PersonRequest personRequest) {
        return personService.create(personRequest.getPerson());
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE, path = "/{personId}")
    public ResponseEntity<Person> find(@PathVariable final Long personId) {
        return personService.find(personId);
    }

    @PutMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE, path = "/{personId}")
    public ResponseEntity<Person> update(@PathVariable final Long personId, @RequestBody final PersonRequest personRequest) {
        return personService.update(personId, personRequest.getPerson());
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE, path = "/")
    public ResponseEntity<List<Person>> list() {
        return personService.list((Long) null);
    }

    //TODO - metodo post para actualizas los datos de una persona

    //TODO - metodo post para obtener agregar un nuevo usuario a una persona

    //TODO - metodo post para obtener elminar un usuario a una persona
}

/*
 *  @(#)PersonApi.java
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
import com.umdc.commons.general.pojo.Person;
import com.umdc.commons.util.HttpStatusUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * PersonApi interface for person endpoints.
 */
public interface PersonApi {
    /**
     * Returns the PersonService instance.
     * @return PersonService instance
     */
    default PersonService getService() {
        return null;
    }

    /**
     * Create and return the person.
     * @param personRequest Request to create a person
     * @return ResponseEntity with created Person
     * @throws Exception if creation fails
     */
    @Operation(summary = "Create a person", description = "Create and return the person.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = HttpStatusUtil.OK_STR, description = "Person created")
    })
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    default ResponseEntity<Person> create(@Parameter(description = "Request to create a person", required = true)
                                          @RequestBody final PersonRequest personRequest) {
        return getService().create(personRequest.getPerson());
    }

    /**
     * Return a person by ID.
     * @param personId Person identifier
     * @return ResponseEntity with found Person
     * @throws Exception if not found
     */
    @Operation(summary = "Fetch person by ID", description = "Return a person.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = HttpStatusUtil.OK_STR, description = "OK"),
            @ApiResponse(responseCode = HttpStatusUtil.NOT_FOUND_STR, description = "Person not found")
    })
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE, path = "/{personId}")
    default ResponseEntity<Person> find(@PathVariable final UUID personId) {
        return getService().find(personId);
    }

    /**
     * Update and return the person.
     * @param personId Person identifier
     * @param personRequest Request to update a person
     * @return ResponseEntity with updated Person
     * @throws Exception if update fails
     */
    @Operation(summary = "Update a person", description = "Update and return the person.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = HttpStatusUtil.OK_STR, description = "Person updated"),
            @ApiResponse(responseCode = HttpStatusUtil.BAD_REQUEST_STR, description = "PersonId invalid"),
            @ApiResponse(responseCode = HttpStatusUtil.BAD_REQUEST_STR, description = "Person request invalid"),
            @ApiResponse(responseCode = HttpStatusUtil.BAD_REQUEST_STR, description = "Person not founded")
    })
    @PutMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE, path = "/{personId}")
    default ResponseEntity<Person> update(@PathVariable final UUID personId, @RequestBody final PersonRequest personRequest) {
        return getService().update(personId, personRequest.getPerson());
    }

    /**
     * Return all people.
     * @return ResponseEntity with a list of Person
     * @throws Exception if fetch fails
     */
    @Operation(summary = "Fetch all people", description = "Return all people.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = HttpStatusUtil.OK_STR, description = "OK"),
            @ApiResponse(responseCode = HttpStatusUtil.NOT_FOUND_STR, description = "Person not found")
    })
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    default ResponseEntity<List<Person>> list() {
        return getService().list((UUID) null);
    }
}


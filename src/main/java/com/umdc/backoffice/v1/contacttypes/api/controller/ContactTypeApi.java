/*
 *  @(#)ContactTypeApi.java
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

package com.umdc.backoffice.v1.contacttypes.api.controller;

import com.umdc.backoffice.v1.contacttypes.service.ContactTypeService;
import com.umdc.backoffice.v1.contacttypes.api.to.ContactTypeRequest;
import com.prx.commons.general.pojo.ContactType;
import com.prx.commons.util.HttpStatusUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * ContactTypeApi interface for contact type endpoints.
 */
public interface ContactTypeApi {
    /**
     * Returns the ContactTypeService instance.
     * @return ContactTypeService
     */
    default ContactTypeService getService() {
        return new ContactTypeService() {
        };
    }

    /**
     * Creates a new contact type based on the provided request.
     *
     * @param contactTypeRequest the request object containing the details of the contact type to be created
     * @return a ResponseEntity containing the details of the created contact type
     */
    @Operation(description = "Create a contact type.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = HttpStatusUtil.OK_STR, description = "OK")
    })
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE, path = "/")
    default ResponseEntity<ContactType> create(@RequestBody final ContactTypeRequest contactTypeRequest) {
        return getService().create(contactTypeRequest);
    }

    /**
     * Finds a specific contact type based on the provided contact type ID.
     *
     * @param contactTypeId the unique identifier of the contact type to retrieve
     * @return a ResponseEntity containing the found ContactType object, or an appropriate HTTP status
     */
    @Operation(description = "Find a contact type list.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = HttpStatusUtil.OK_STR, description = "OK")
    })
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE, path = "/{contactTypeId}")
    default ResponseEntity<ContactType> find(@PathVariable final UUID contactTypeId) {
        return getService().findById(contactTypeId);
    }

    /**
     * Updates an existing contact type by its unique identifier and the provided contact type details.
     *
     * @param contactTypeId the unique identifier of the contact type to be updated
     * @param contactType the contact type object containing the updated details
     * @return a ResponseEntity containing the updated ContactType object, or an appropriate HTTP status
     */
    @Operation(description = "Find a contact type list.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = HttpStatusUtil.OK_STR, description = "Contact Type updated."),
            @ApiResponse(responseCode = HttpStatusUtil.NOT_FOUND_STR, description = "Contact Type not found."),
            @ApiResponse(responseCode = HttpStatusUtil.BAD_REQUEST_STR, description = "Contact Type couldn't be updated.")
    })
    @PutMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE, path = "/{contactTypeId}")
    default ResponseEntity<ContactType> update(@PathVariable final UUID contactTypeId, @RequestBody ContactType contactType) {
        return getService().update(contactTypeId, contactType);
    }

    /**
     * Finds a list of contact types based on the provided list of unique identifiers.
     *
     * @param contactTypeIds a list of unique identifiers for the contact types to retrieve
     * @return a ResponseEntity containing a list of found ContactType objects, or an appropriate HTTP status
     */
    @Operation(description = "Find a contact type list by ids.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = HttpStatusUtil.OK_STR, description = "OK")
    })
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE, path = "/list/{contactTypeIds}")
    default ResponseEntity<List<ContactType>> list(@PathVariable final List<UUID> contactTypeIds){
        return getService().listById(contactTypeIds);
    }

    /**
     * Retrieves a list of contact types.
     *
     * @return a ResponseEntity containing a list of ContactType objects
     */
    @Operation(description = "Get a contact type list.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = HttpStatusUtil.OK_STR, description = "OK")
    })
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE, path = "/list-all")
    default ResponseEntity<List<ContactType>> list() {
        return getService().list();
    }

    /**
     * Deletes a contact type based on the provided unique identifier.
     *
     * @param contactTypeId the unique identifier of the contact type to be deleted
     * @return a ResponseEntity containing the deleted ContactType object or an appropriate HTTP status
     */
    @Operation(description = "Delete a contact type.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = HttpStatusUtil.OK_STR, description = "OK")
    })
    @DeleteMapping(path = "/{contactTypeId}")
    default ResponseEntity<ContactType> delete(@PathVariable final UUID contactTypeId) {
        return getService().delete(contactTypeId);
    }
}


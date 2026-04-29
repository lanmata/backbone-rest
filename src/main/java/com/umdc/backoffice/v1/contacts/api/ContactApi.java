/*
 *  @(#)ContactApi.java
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

package com.umdc.backoffice.v1.contacts.api;

import com.umdc.backoffice.v1.contacts.service.ContactService;
import com.prx.commons.general.pojo.Contact;
import com.prx.commons.util.HttpStatusUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

public interface ContactApi {

    default ContactService getService() {
        return new ContactService() {
        };
    }

    /**
     * Creates a new contact.
     *
     * @param contact The {@link Contact} object containing the details of the contact to be created.
     * @return A {@link ResponseEntity} containing the created {@link Contact} object.
     */
    @Operation(description = "Create a contact.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = HttpStatusUtil.OK_STR, description = "OK")
    })
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE, path = "/")
    default ResponseEntity<Contact> create(@RequestBody final Contact contact) {
        return getService().create(contact);
    }

    /**
     * Updates the specified contact with the provided details.
     *
     * @param contactId the unique identifier of the contact to be updated
     * @param contact the contact object containing updated information
     * @return the updated contact wrapped in a ResponseEntity
     */
    @Operation(description = "Update a contact.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = HttpStatusUtil.OK_STR, description = "OK")
    })
    @PutMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE, path = "/{contactId}")
    default ResponseEntity<Contact> update(@PathVariable final UUID contactId, @RequestBody Contact contact) {
        return getService().update(contactId, contact);
    }

    /**
     * Finds a contact by its unique identifier.
     *
     * @param contactId the unique identifier of the contact to be retrieved
     * @return a ResponseEntity containing the found contact or an appropriate status if not found
     */
    @Operation(description = "Find a contact list.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = HttpStatusUtil.OK_STR, description = "OK")
    })
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE, path = "/{contactId}")
    default ResponseEntity<Contact> find(@PathVariable(value = "contactId") final UUID contactId) {
        return getService().find(contactId);
    }

    /**
     * Retrieves a list of contacts based on the provided contact IDs.
     *
     * @param contactIds the list of UUIDs representing the IDs of the contacts to retrieve
     * @return a ResponseEntity containing a list of Contact objects
     */
    @Operation(description = "Find a contact list by ids.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = HttpStatusUtil.OK_STR, description = "OK")
    })
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE, path = "/list/{contactIds}")
    default ResponseEntity<List<Contact>> list(@PathVariable List<UUID> contactIds){
        return getService().list(contactIds);
    }

    /**
     * Lists contacts for a given person by their unique identifier.
     *
     * @param personId the UUID of the person whose contacts are to be retrieved
     * @return a ResponseEntity containing the list of contacts associated with the specified person
     */
    @Operation(description = "List contacts by person Id.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = HttpStatusUtil.OK_STR, description = "OK")
    })
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE, path = "/person/{personId}")
    default ResponseEntity<List<Contact>> list(@PathVariable UUID personId){
        return getService().listByPersonId(personId);
    }

    /**
     * Retrieves a list of contacts.
     *
     * @return a ResponseEntity containing a list of Contact objects and the HTTP response status.
     */
    @Operation(description = "Get a contact list.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = HttpStatusUtil.OK_STR, description = "OK")
    })
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE, path = "/list-all")
    default ResponseEntity<List<Contact>> list(){
        return ResponseEntity.status(org.springframework.http.HttpStatus.NOT_IMPLEMENTED).build();
    }

    /**
     * Deletes a contact identified by the provided contact ID.
     *
     * @param contactId the unique identifier of the contact to be deleted
     * @return a ResponseEntity containing a status message indicating the outcome of the deletion operation
     */
    @Operation(description = "Delete a contact.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = HttpStatusUtil.OK_STR, description = "OK")
    })
    @DeleteMapping(path = "/{contactId}")
    default ResponseEntity<String> delete(@PathVariable final UUID contactId) {
        return getService().deleteById(contactId);
    }
}


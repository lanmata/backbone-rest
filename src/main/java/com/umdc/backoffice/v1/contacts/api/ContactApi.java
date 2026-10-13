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

import com.umdc.backoffice.util.PageResponse;
import com.umdc.backoffice.v1.contacts.api.to.ContactCreateRequest;
import com.umdc.backoffice.v1.contacts.service.ContactSearchCriteria;
import com.umdc.backoffice.v1.contacts.service.ContactService;
import com.umdc.commons.general.pojo.Contact;
import com.umdc.commons.util.HttpStatusUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "contacts", description = "Contact management")
public interface ContactApi {

    default ContactService getService() {
        return new ContactService() {
        };
    }

    /**
     * Creates a new contact.
     *
     * @param request the contact creation request, referencing its contact type by id
     * @return A {@link ResponseEntity} containing the created {@link Contact} object.
     */
    @Operation(description = "Create a contact.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = HttpStatusUtil.OK_STR, description = "OK"),
            @ApiResponse(responseCode = HttpStatusUtil.BAD_REQUEST_STR, description = "Invalid contact payload")
    })
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE, path = "/")
    default ResponseEntity<Contact> create(@RequestBody final ContactCreateRequest request) {
        return getService().create(request);
    }

    /**
     * Updates the specified contact with the provided details.
     *
     * @param contactId the unique identifier of the contact to be updated
     * @param request the request object containing updated information
     * @return the updated contact wrapped in a ResponseEntity
     */
    @Operation(description = "Update a contact.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = HttpStatusUtil.OK_STR, description = "OK")
    })
    @PutMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE, path = "/{contactId}")
    default ResponseEntity<Contact> update(@PathVariable final UUID contactId, @RequestBody ContactCreateRequest request) {
        return getService().update(contactId, request);
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
     * Lists the contacts of a person, optionally limited to one application.
     *
     * @param personId      the UUID of the person whose contacts are to be retrieved
     * @param applicationId optional application filter
     * @return a ResponseEntity containing the contacts associated with the person
     */
    @Operation(description = "List contacts by person Id, optionally limited to one application.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = HttpStatusUtil.OK_STR, description = "OK")
    })
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE, path = "/person/{personId}")
    default ResponseEntity<List<Contact>> listByPerson(@PathVariable UUID personId,
                                                       @RequestParam(required = false) UUID applicationId) {
        return getService().listByPersonId(personId, applicationId);
    }

    /**
     * Lists the contacts of an external owner that is neither a person nor an application.
     *
     * @param ownerType     kind of the owner (e.g. {@code MERCHANT})
     * @param ownerId       id of the owner
     * @param applicationId optional application filter
     * @return a ResponseEntity containing the owner's contacts
     */
    @Operation(description = "List contacts of an external owner (e.g. a merchant of an application).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = HttpStatusUtil.OK_STR, description = "OK")
    })
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE, path = "/owner/{ownerType}/{ownerId}")
    default ResponseEntity<List<Contact>> listByOwner(@PathVariable String ownerType, @PathVariable UUID ownerId,
                                                      @RequestParam(required = false) UUID applicationId) {
        return getService().listByOwner(ownerType, ownerId, applicationId);
    }

    /**
     * Paginated contact directory of ONE application. {@code applicationId} is mandatory: contacts are
     * never listed across applications.
     *
     * @param applicationId the owning application (required)
     * @param contactTypeId optional contact type filter
     * @param active        optional active filter
     * @param q             optional case-insensitive "contains" filter on the content
     * @param page          zero-based page index (default 0)
     * @param size          page size (default 20, maximum 100)
     * @return the requested {@link PageResponse}
     */
    @Operation(description = "Paginated contact directory of one application (applicationId is mandatory).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = HttpStatusUtil.OK_STR, description = "OK"),
            @ApiResponse(responseCode = HttpStatusUtil.BAD_REQUEST_STR, description = "applicationId is missing")
    })
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE, path = "/")
    default ResponseEntity<PageResponse<Contact>> search(@RequestParam(required = false) UUID applicationId,
                                                         @RequestParam(required = false) UUID contactTypeId,
                                                         @RequestParam(required = false) Boolean active,
                                                         @RequestParam(required = false) String q,
                                                         @RequestParam(defaultValue = "0") int page,
                                                         @RequestParam(defaultValue = "20") int size) {
        return getService().search(new ContactSearchCriteria(applicationId, contactTypeId, active, q), page, size);
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


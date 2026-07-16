/*
 *  @(#)IdentificationDocumentApi.java
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
package com.umdc.backoffice.v1.identificationdocuments.api.controller;

import com.umdc.backoffice.v1.identificationdocuments.api.to.IdentificationDocument;
import com.umdc.backoffice.v1.identificationdocuments.api.to.IdentificationDocumentRequest;
import com.umdc.backoffice.v1.identificationdocuments.service.IdentificationDocumentService;
import com.umdc.commons.util.HttpStatusUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.UUID;

/**
 * Interface for the Identification Document API.
 */
@Tag(name = "identification-documents", description = "Identification document management")
public interface IdentificationDocumentApi {

    /**
     * Gets the identification document service.
     *
     * @return the identification document service
     */
    default IdentificationDocumentService getService() {
        return new IdentificationDocumentService() {
        };
    }

    /**
     * Creates a new identification document.
     *
     * @param identificationDocumentRequest the identification document creation request
     * @return the created identification document wrapped in a ResponseEntity
     */
    @Operation(summary = "Create an identification document", description = "Creates a new identification document.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = HttpStatusUtil.CREATED_STR, description = "Identification document created."),
            @ApiResponse(responseCode = HttpStatusUtil.BAD_REQUEST_STR, description = "Invalid request body.")
    })
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    default ResponseEntity<IdentificationDocument> createIdentificationDocument(
            @RequestBody IdentificationDocumentRequest identificationDocumentRequest) {
        return this.getService().create(identificationDocumentRequest.getIdentificationDocument());
    }

    /**
     * Finds an identification document by its ID.
     *
     * @param identificationDocumentId the identification document UUID
     * @return the found identification document wrapped in a ResponseEntity
     */
    @Operation(summary = "Find identification document by ID",
            description = "Returns a single identification document by its UUID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = HttpStatusUtil.OK_STR, description = "Identification document found."),
            @ApiResponse(responseCode = HttpStatusUtil.NOT_FOUND_STR, description = "Identification document not found.")
    })
    @GetMapping(value = "/{identificationDocumentId}", produces = MediaType.APPLICATION_JSON_VALUE)
    default ResponseEntity<IdentificationDocument> findIdentificationDocumentById(
            @PathVariable UUID identificationDocumentId) {
        return this.getService().find(identificationDocumentId);
    }

    /**
     * Updates an existing identification document.
     *
     * @param identificationDocumentId      the identification document UUID
     * @param identificationDocumentRequest the identification document update request
     * @return the updated identification document wrapped in a ResponseEntity
     */
    @Operation(summary = "Update an identification document",
            description = "Updates an existing identification document by its UUID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = HttpStatusUtil.OK_STR, description = "Identification document updated."),
            @ApiResponse(responseCode = HttpStatusUtil.NOT_FOUND_STR, description = "Identification document not found.")
    })
    @PutMapping(value = "/{identificationDocumentId}", produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    default ResponseEntity<IdentificationDocument> updateIdentificationDocument(
            @PathVariable UUID identificationDocumentId,
            @RequestBody IdentificationDocumentRequest identificationDocumentRequest) {
        return this.getService().update(identificationDocumentId, identificationDocumentRequest.getIdentificationDocument());
    }

    /**
     * Deletes an identification document by its ID.
     *
     * @param identificationDocumentId the identification document UUID
     * @return the deleted identification document wrapped in a ResponseEntity
     */
    @Operation(summary = "Delete an identification document",
            description = "Deletes the identification document identified by the given UUID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = HttpStatusUtil.OK_STR, description = "Identification document deleted."),
            @ApiResponse(responseCode = HttpStatusUtil.NOT_FOUND_STR, description = "Identification document not found.")
    })
    @DeleteMapping(value = "/{identificationDocumentId}", produces = MediaType.APPLICATION_JSON_VALUE)
    default ResponseEntity<IdentificationDocument> deleteIdentificationDocument(
            @PathVariable UUID identificationDocumentId) {
        return this.getService().delete(identificationDocumentId, null);
    }

    /**
     * Lists identification documents belonging to the given person.
     *
     * @param personId the owning person's UUID
     * @return the matching identification documents wrapped in a ResponseEntity
     */
    @Operation(summary = "List identification documents by person",
            description = "Business rule: a person has a document (11); a document belongs to one person "
                    + "in this instance-level model.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = HttpStatusUtil.OK_STR, description = "Identification document list returned."),
            @ApiResponse(responseCode = HttpStatusUtil.NOT_FOUND_STR, description = "Person not found.")
    })
    @GetMapping(value = "/person/{personId}", produces = MediaType.APPLICATION_JSON_VALUE)
    default ResponseEntity<List<IdentificationDocument>> listIdentificationDocumentsByPerson(
            @PathVariable UUID personId) {
        return this.getService().listByPerson(personId);
    }
}

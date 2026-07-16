/*
 *  @(#)IdentificationDocumentService.java
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
package com.umdc.backoffice.v1.identificationdocuments.service;

import com.umdc.backoffice.v1.identificationdocuments.api.to.IdentificationDocument;
import com.umdc.commons.services.CrudService;
import org.apache.commons.lang3.NotImplementedException;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.UUID;

/**
 * Interface for identification document operations.
 * Extends the CrudService interface to provide CRUD operations for
 * {@link com.umdc.persistence.general.domains.IdentificationDocumentEntity}.
 */
public interface IdentificationDocumentService extends CrudService<UUID, IdentificationDocument> {

    /**
     * Creates a new identification document.
     *
     * @param identificationDocument the identification document to create
     * @return the created identification document wrapped in a ResponseEntity
     * @throws NotImplementedException if the method is not implemented
     */
    @Override
    default ResponseEntity<IdentificationDocument> create(IdentificationDocument identificationDocument) {
        throw new NotImplementedException();
    }

    /**
     * Finds an identification document by its ID.
     *
     * @param id the ID of the identification document to find
     * @return the found identification document wrapped in a ResponseEntity
     * @throws NotImplementedException if the method is not implemented
     */
    @Override
    default ResponseEntity<IdentificationDocument> find(UUID id) {
        throw new NotImplementedException();
    }

    /**
     * Updates an existing identification document.
     *
     * @param id                     the ID of the identification document to update
     * @param identificationDocument the identification document with updated information
     * @return the updated identification document wrapped in a ResponseEntity
     * @throws NotImplementedException if the method is not implemented
     */
    @Override
    default ResponseEntity<IdentificationDocument> update(UUID id, IdentificationDocument identificationDocument) {
        throw new NotImplementedException();
    }

    /**
     * Deletes an identification document.
     *
     * @param id                     the ID of the identification document to delete
     * @param identificationDocument unused, kept for {@link CrudService} signature compatibility
     * @return the deleted identification document wrapped in a ResponseEntity
     * @throws NotImplementedException if the method is not implemented
     */
    @Override
    default ResponseEntity<IdentificationDocument> delete(UUID id, IdentificationDocument identificationDocument) {
        throw new NotImplementedException();
    }

    /**
     * Lists identification documents by their IDs.
     *
     * @param id the IDs of the identification documents to list
     * @return a list of identification documents wrapped in a ResponseEntity
     * @throws NotImplementedException if the method is not implemented
     */
    @Override
    default ResponseEntity<List<IdentificationDocument>> list(UUID... id) {
        throw new NotImplementedException();
    }

    /**
     * Lists identification documents belonging to the given person.
     *
     * @param personId the owning person's UUID
     * @return the matching identification documents wrapped in a ResponseEntity, or 404 if
     *         the person does not exist or has no identification documents
     * @throws NotImplementedException if the method is not implemented
     */
    default ResponseEntity<List<IdentificationDocument>> listByPerson(UUID personId) {
        throw new NotImplementedException();
    }
}

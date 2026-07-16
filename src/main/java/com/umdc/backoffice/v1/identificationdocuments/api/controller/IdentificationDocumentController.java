/*
 *  @(#)IdentificationDocumentController.java
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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for managing identification documents.
 */
@RestController
@RequestMapping("/api/v1/identification-documents")
public class IdentificationDocumentController implements IdentificationDocumentApi {

    private final IdentificationDocumentService identificationDocumentService;

    /**
     * Constructor for IdentificationDocumentController.
     *
     * @param identificationDocumentService the identification document service
     */
    public IdentificationDocumentController(IdentificationDocumentService identificationDocumentService) {
        this.identificationDocumentService = identificationDocumentService;
    }

    @Override
    public ResponseEntity<IdentificationDocument> createIdentificationDocument(
            IdentificationDocumentRequest identificationDocumentRequest) {
        return identificationDocumentService.create(identificationDocumentRequest.getIdentificationDocument());
    }

    @Override
    public ResponseEntity<IdentificationDocument> findIdentificationDocumentById(UUID identificationDocumentId) {
        return identificationDocumentService.find(identificationDocumentId);
    }

    @Override
    public ResponseEntity<IdentificationDocument> updateIdentificationDocument(
            UUID identificationDocumentId, IdentificationDocumentRequest identificationDocumentRequest) {
        return identificationDocumentService.update(
                identificationDocumentId, identificationDocumentRequest.getIdentificationDocument());
    }

    @Override
    public ResponseEntity<IdentificationDocument> deleteIdentificationDocument(UUID identificationDocumentId) {
        return identificationDocumentService.delete(identificationDocumentId, null);
    }

    @Override
    public ResponseEntity<List<IdentificationDocument>> listIdentificationDocumentsByPerson(UUID personId) {
        return identificationDocumentService.listByPerson(personId);
    }
}

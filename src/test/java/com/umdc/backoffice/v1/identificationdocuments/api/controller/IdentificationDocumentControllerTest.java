/*
 *  @(#)IdentificationDocumentControllerTest.java
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

/// Unit tests for {@link IdentificationDocumentController} verifying correct delegation to the service layer.
@ExtendWith(MockitoExtension.class)
class IdentificationDocumentControllerTest {

    private static final UUID DOCUMENT_ID = UUID.randomUUID();
    private static final UUID PERSON_ID = UUID.randomUUID();

    @Mock
    private IdentificationDocumentService identificationDocumentService;

    private IdentificationDocumentController controller;

    @BeforeEach
    void setUp() {
        controller = new IdentificationDocumentController(identificationDocumentService);
    }

    @Test
    @DisplayName("POST /identification-documents — delegates create and returns 201")
    void createIdentificationDocument_delegates_returns201() {
        IdentificationDocumentRequest request = buildRequest();
        doReturn(ResponseEntity.status(HttpStatus.CREATED).body(buildPojo()))
                .when(identificationDocumentService).create(request.getIdentificationDocument());

        ResponseEntity<IdentificationDocument> response = controller.createIdentificationDocument(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        verify(identificationDocumentService).create(request.getIdentificationDocument());
    }

    @Test
    @DisplayName("GET /identification-documents/{id} — delegates find and returns 200")
    void findIdentificationDocumentById_delegates_returns200() {
        doReturn(ResponseEntity.ok(buildPojo()))
                .when(identificationDocumentService).find(DOCUMENT_ID);

        ResponseEntity<IdentificationDocument> response = controller.findIdentificationDocumentById(DOCUMENT_ID);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(identificationDocumentService).find(DOCUMENT_ID);
    }

    @Test
    @DisplayName("GET /identification-documents/{id} — delegates find and returns 404")
    void findIdentificationDocumentById_delegates_returns404() {
        doReturn(ResponseEntity.notFound().build())
                .when(identificationDocumentService).find(DOCUMENT_ID);

        ResponseEntity<IdentificationDocument> response = controller.findIdentificationDocumentById(DOCUMENT_ID);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(identificationDocumentService).find(DOCUMENT_ID);
    }

    @Test
    @DisplayName("PUT /identification-documents/{id} — delegates update and returns 200")
    void updateIdentificationDocument_delegates_returns200() {
        IdentificationDocumentRequest request = buildRequest();
        doReturn(ResponseEntity.ok(buildPojo()))
                .when(identificationDocumentService).update(DOCUMENT_ID, request.getIdentificationDocument());

        ResponseEntity<IdentificationDocument> response =
                controller.updateIdentificationDocument(DOCUMENT_ID, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(identificationDocumentService).update(DOCUMENT_ID, request.getIdentificationDocument());
    }

    @Test
    @DisplayName("PUT /identification-documents/{id} — delegates update and returns 404")
    void updateIdentificationDocument_delegates_returns404() {
        IdentificationDocumentRequest request = buildRequest();
        doReturn(ResponseEntity.notFound().build())
                .when(identificationDocumentService).update(DOCUMENT_ID, request.getIdentificationDocument());

        ResponseEntity<IdentificationDocument> response =
                controller.updateIdentificationDocument(DOCUMENT_ID, request);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(identificationDocumentService).update(DOCUMENT_ID, request.getIdentificationDocument());
    }

    @Test
    @DisplayName("DELETE /identification-documents/{id} — delegates delete and returns 200")
    void deleteIdentificationDocument_delegates_returns200() {
        doReturn(ResponseEntity.ok(buildPojo()))
                .when(identificationDocumentService).delete(DOCUMENT_ID, null);

        ResponseEntity<IdentificationDocument> response = controller.deleteIdentificationDocument(DOCUMENT_ID);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(identificationDocumentService).delete(DOCUMENT_ID, null);
    }

    @Test
    @DisplayName("DELETE /identification-documents/{id} — delegates delete and returns 404")
    void deleteIdentificationDocument_delegates_returns404() {
        doReturn(ResponseEntity.notFound().build())
                .when(identificationDocumentService).delete(DOCUMENT_ID, null);

        ResponseEntity<IdentificationDocument> response = controller.deleteIdentificationDocument(DOCUMENT_ID);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(identificationDocumentService).delete(DOCUMENT_ID, null);
    }

    @Test
    @DisplayName("GET /identification-documents/person/{personId} — delegates listByPerson and returns 200")
    void listIdentificationDocumentsByPerson_delegates_returns200() {
        doReturn(ResponseEntity.ok(List.of(buildPojo())))
                .when(identificationDocumentService).listByPerson(PERSON_ID);

        ResponseEntity<List<IdentificationDocument>> response =
                controller.listIdentificationDocumentsByPerson(PERSON_ID);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(identificationDocumentService).listByPerson(PERSON_ID);
    }

    @Test
    @DisplayName("GET /identification-documents/person/{personId} — delegates listByPerson and returns 404")
    void listIdentificationDocumentsByPerson_delegates_returns404() {
        doReturn(ResponseEntity.notFound().build())
                .when(identificationDocumentService).listByPerson(PERSON_ID);

        ResponseEntity<List<IdentificationDocument>> response =
                controller.listIdentificationDocumentsByPerson(PERSON_ID);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(identificationDocumentService).listByPerson(PERSON_ID);
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private IdentificationDocument buildPojo() {
        IdentificationDocument dto = new IdentificationDocument();
        dto.setId(DOCUMENT_ID);
        dto.setNumber("123456");
        dto.setExpirationDate(LocalDate.now().plusYears(5));
        dto.setIdentificationType(0);
        dto.setPersonId(PERSON_ID);
        return dto;
    }

    private IdentificationDocumentRequest buildRequest() {
        IdentificationDocumentRequest request = new IdentificationDocumentRequest();
        request.setIdentificationDocument(buildPojo());
        return request;
    }
}

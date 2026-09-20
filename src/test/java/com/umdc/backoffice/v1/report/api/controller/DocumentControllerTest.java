/*
 *  @(#)DocumentControllerTest.java
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
package com.umdc.backoffice.v1.report.api.controller;

import com.umdc.backoffice.v1.report.api.to.TemplateDocumentModel;
import com.umdc.backoffice.v1.report.service.DocumentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Luis Mata
 */
class DocumentControllerTest {

    @Mock
    private DocumentService documentService;

    private DocumentController documentController;

    private final MockMultipartFile template = new MockMultipartFile("documentTemplate", new byte[0]);

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        documentController = new DocumentController(documentService);
    }

    @Test
    @DisplayName("createWordDocument delegates to DocumentService.process")
    void createWordDocumentDelegatesToService() {
        Map<String, String> values = Map.of("name", "Alice");
        ResponseEntity<org.springframework.core.io.Resource> expected = ResponseEntity.ok().build();
        when(documentService.process(values, template)).thenReturn(expected);

        var response = documentController.createWordDocument(values, template);

        assertEquals(expected, response);
    }

    @Test
    @DisplayName("placeholderValues returns 400 without calling the service when the model is null")
    void placeholderValuesReturnsBadRequestWhenModelIsNull() {
        ResponseEntity<List<String>> response = documentController.placeholderValues(null, template);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(documentService, never()).findPlaceholderValues(template);
    }

    @Test
    @DisplayName("placeholderValues delegates to DocumentService.findPlaceholderValues when the model is present")
    void placeholderValuesDelegatesToServiceWhenModelPresent() {
        TemplateDocumentModel model = new TemplateDocumentModel();
        ResponseEntity<List<String>> expected = ResponseEntity.status(HttpStatus.FOUND).body(List.of("~{name}~"));
        when(documentService.findPlaceholderValues(template)).thenReturn(expected);

        var response = documentController.placeholderValues(model, template);

        assertEquals(expected, response);
    }
}

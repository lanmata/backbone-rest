/*
 *  @(#)DocumentController.java
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
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * DocumentController.
 *
 * @author &lt;a href='mailto:luis.antonio.mata@gmail.com'&gt;Luis Antonio Mata&lt;/a&gt;
 * @version 1.0.0, 27-12-2021
 * @since 11
 */
@RestController
@RequestMapping("/api/v1/report")
public class DocumentController implements DocumentApi {

    private final DocumentService documentService;

    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    @Override
    public ResponseEntity<Resource> createWordDocument(Map<String, String> values, MultipartFile documentTemplate) {
        return documentService.process(values, documentTemplate);
    }

    @Override
    public ResponseEntity<List<String>> placeholderValues(TemplateDocumentModel templateDocumentModel, MultipartFile documentTemplate) {
        if (null == templateDocumentModel) {
            return ResponseEntity.badRequest().build();
        }
        return documentService.findPlaceholderValues(documentTemplate);
    }
}

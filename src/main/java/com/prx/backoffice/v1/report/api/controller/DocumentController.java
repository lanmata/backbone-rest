/*
 * @(#)$file.className.java.
 *
 * Copyright (c) Luis Antonio Mata Mata. All rights reserved.
 *
 * All rights to this product are owned by Luis Antonio Mata Mata and may only
 * be used under the terms of its associated license document. You may NOT
 * copy, modify, sublicense, or distribute this source file or portions of
 * it unless previously authorized in writing by Luis Antonio Mata Mata.
 * In any event, this notice and the above copyright must always be included
 * verbatim with this file.
 */

package com.prx.backoffice.v1.report.api.controller;

import com.prx.backoffice.v1.report.api.to.TemplateDocumentModel;
import com.prx.backoffice.v1.report.service.DocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
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
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@RequestMapping("v1/report")
public class DocumentController {

    private final DocumentService documentService;

    @GetMapping(consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE},
            produces = {MediaType.APPLICATION_OCTET_STREAM_VALUE}, path = "/template")
    public ResponseEntity<Resource> createWordDocument(@RequestParam Map<String, String> values, @RequestParam("documentTemplate") MultipartFile documentTemplate){
        return documentService.process(values, documentTemplate);
    }

    @GetMapping(consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE},
            produces = {MediaType.APPLICATION_OCTET_STREAM_VALUE}, path = "/placeholdervalues")
    public ResponseEntity<List<String>> placeholderValues(@RequestParam("templateDocumentModel") TemplateDocumentModel templateDocumentModel, @RequestParam("documentTemplate") MultipartFile documentTemplate){
        if(null == templateDocumentModel) {
            return ResponseEntity.badRequest().build();
        }
        return documentService.findPlaceholderValues(documentTemplate);
    }
}

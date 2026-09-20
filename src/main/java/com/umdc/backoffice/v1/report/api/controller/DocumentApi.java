/*
 *  @(#)DocumentApi.java
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
import com.umdc.commons.util.HttpStatusUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * Interface for the Document/Report API.
 * Provides endpoints for generating Word documents from a template and
 * inspecting the placeholders a template declares.
 */
@Tag(name = "report", description = "Word document template processing")
@RequestMapping("/api/v1/report")
public interface DocumentApi {

    /**
     * Fills the given Word template with the supplied values and returns the
     * generated document.
     *
     * @param values           placeholder name to replacement value
     * @param documentTemplate the .docx template file
     * @return the generated document as a downloadable resource
     */
    @Operation(summary = "Create a Word document from a template",
            description = "Replaces every {placeholder} in the uploaded .docx template with the given values.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = HttpStatusUtil.OK_STR, description = "Document generated."),
            @ApiResponse(responseCode = HttpStatusUtil.BAD_REQUEST_STR, description = "Template could not be processed.")
    })
    @PostMapping(consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE},
            produces = {MediaType.APPLICATION_OCTET_STREAM_VALUE}, path = "/template")
    ResponseEntity<Resource> createWordDocument(
            @Parameter(description = "Placeholder name to replacement value") @RequestParam Map<String, String> values,
            @Parameter(description = "Word (.docx) template file", required = true) @RequestParam("documentTemplate") MultipartFile documentTemplate);

    /**
     * Lists the placeholders declared in the given Word template.
     *
     * @param templateDocumentModel metadata describing the template
     * @param documentTemplate      the .docx template file
     * @return the placeholder names found in the template
     */
    @Operation(summary = "List placeholders in a template",
            description = "Scans the uploaded .docx template and returns every ~{placeholder}~ found.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = HttpStatusUtil.FOUND_STR, description = "Placeholders found."),
            @ApiResponse(responseCode = HttpStatusUtil.NOT_FOUND_STR, description = "No placeholders found in the template."),
            @ApiResponse(responseCode = HttpStatusUtil.BAD_REQUEST_STR, description = "Missing template metadata.")
    })
    @PostMapping(consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE},
            produces = {MediaType.APPLICATION_OCTET_STREAM_VALUE}, path = "/placeholdervalues")
    ResponseEntity<List<String>> placeholderValues(
            @Parameter(description = "Template metadata", required = true) @RequestParam("templateDocumentModel") TemplateDocumentModel templateDocumentModel,
            @Parameter(description = "Word (.docx) template file", required = true) @RequestParam("documentTemplate") MultipartFile documentTemplate);
}

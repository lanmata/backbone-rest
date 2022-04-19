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
package com.prx.backoffice.v1.contacttype.api;

import com.prx.backoffice.util.MessageUtil;
import com.prx.backoffice.v1.contacttype.service.ContactTypeService;
import com.prx.backoffice.v1.contacttype.to.ContactTypeRequest;
import com.prx.commons.pojo.ContactType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * ContactTypeApiController.
 *
 * @author <a href="mailto:luis.antonio.mata@gmail.com">Luis Antonio Mata</a>
 * @version 1.0.0, 10-04-2022
 * @since 11
 */
@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@RequestMapping("v1/contact-type")
public class ContactTypeController {
    private final ContactTypeService contactTypeService;

    @Operation(description = "Create a contact type.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = MessageUtil.OK_VALUE, description = "OK")
    })
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE, path = "/")
    public ResponseEntity<ContactType> create(@RequestBody final ContactTypeRequest contactTypeRequest) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }

    @Operation(description = "Find a contact type list.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = MessageUtil.OK_VALUE, description = "OK")
    })
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE, path = "/{contactTypeId}")
    public ResponseEntity<ContactType> find(@PathVariable final long contactTypeId) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }

    @Operation(description = "Find a contact type list by ids.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = MessageUtil.OK_VALUE, description = "OK")
    })
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE, path = "/list/{contactTypeIds}")
    public ResponseEntity<List<ContactType>> list(@PathVariable final List<Long> contactTypeIds){
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }

    @Operation(description = "Get a contact type list.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = MessageUtil.OK_VALUE, description = "OK")
    })
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE, path = "/list")
    public ResponseEntity<List<ContactType>> list() {
        return contactTypeService.list();
    }


    @Operation(description = "Delete a contact type.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = MessageUtil.OK_VALUE, description = "OK")
    })
    @DeleteMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE, path = "/")
    public ResponseEntity<ContactType> delete(@PathVariable final Long contactTypeId) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }
}

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
package com.prx.backoffice.v1.contacttypes.api;

import com.prx.backoffice.util.MessageUtil;
import com.prx.backoffice.v1.contacttypes.service.ContactTypeService;
import com.prx.backoffice.v1.contacttypes.to.ContactTypeRequest;
import com.prx.commons.pojo.ContactType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
@CrossOrigin(origins = "*")
@RequestMapping("v1/contact-types")
public class ContactTypeController {
    private final ContactTypeService contactTypeService;

    public ContactTypeController(ContactTypeService contactTypeService) {
        this.contactTypeService = contactTypeService;
    }

    @Operation(description = "Create a contact type.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = MessageUtil.OK, description = "OK")
    })
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE, path = "/")
    public ResponseEntity<ContactType> create(@RequestBody final ContactTypeRequest contactTypeRequest) {
        return contactTypeService.create(contactTypeRequest);
    }

    @Operation(description = "Find a contact type list.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = MessageUtil.OK, description = "OK")
    })
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE, path = "/{contactTypeId}")
    public ResponseEntity<ContactType> find(@PathVariable final String contactTypeId) {
        return contactTypeService.findById(contactTypeId);
    }

    @Operation(description = "Find a contact type list.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = MessageUtil.OK, description = "Contact Type updated."),
            @ApiResponse(responseCode = MessageUtil.NOT_FOUND, description = "Contact Type not found."),
            @ApiResponse(responseCode = MessageUtil.BAD_REQUEST, description = "Contact Type couldn't be updated.")
    })
    @PutMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE, path = "/{contactTypeId}")
    public ResponseEntity<ContactType> update(@PathVariable final String contactTypeId, @RequestBody ContactType contactType) {
        return contactTypeService.update(contactTypeId, contactType);
    }

    @Operation(description = "Find a contact type list by ids.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = MessageUtil.OK, description = "OK")
    })
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE, path = "/list/{contactTypeIds}")
    public ResponseEntity<List<ContactType>> list(@PathVariable final List<String> contactTypeIds){
        return contactTypeService.listById(contactTypeIds);
    }

    @Operation(description = "Get a contact type list.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = MessageUtil.OK, description = "OK")
    })
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE, path = "/list")
    public ResponseEntity<List<ContactType>> list() {
        return contactTypeService.list();
    }


    @Operation(description = "Delete a contact type.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = MessageUtil.OK, description = "OK")
    })
    @DeleteMapping(path = "/{contactTypeId}")
    public ResponseEntity<ContactType> delete(@PathVariable final String contactTypeId) {
        return contactTypeService.delete(contactTypeId);
    }
}

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

package com.prx.backoffice.v1.contact.api;

import com.prx.backoffice.util.MessageUtil;
import com.prx.backoffice.v1.contact.service.ContactService;
import com.prx.backoffice.v1.contact.to.ContactRequest;
import com.prx.commons.pojo.Contact;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;
import java.util.List;

/**
 * ContactApiController.
 *
 * @author <a href="mailto:luis.antonio.mata@gmail.com">Luis Antonio Mata</a>
 * @version 1.0.0, 07-04-2022
 * @since 11
 */
@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@RequestMapping("v1/contact")
public class ContactController {

    private final ContactService contactService;

    @Operation(description = "Create a contact.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = MessageUtil.OK_VALUE, description = "OK")
    })
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE, path = "/")
    public ResponseEntity<Contact> create(@RequestBody final ContactRequest contactRequest) {
        return contactService.create(contactRequest);
    }

    @Operation(description = "Update a contact.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = MessageUtil.OK_VALUE, description = "OK")
    })
    @PutMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE, path = "/{contactId}")
    public ResponseEntity<Contact> update(@PathVariable final BigInteger contactId, @RequestBody Contact contact) {
        return contactService.update(contact, contactId);
    }

    @Operation(description = "Find a contact list.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = MessageUtil.OK_VALUE, description = "OK")
    })
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE, path = "/{contactId}")
    public ResponseEntity<Contact> find(@PathVariable final BigInteger contactId) {
        return contactService.find(contactId);
    }

    @Operation(description = "Find a contact list by ids.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = MessageUtil.OK_VALUE, description = "OK")
    })
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE, path = "/list/{contactIds}")
    public ResponseEntity<List<Contact>> list(@PathVariable List<BigInteger> contactIds){
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    @Operation(description = "Get a contact list.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = MessageUtil.OK_VALUE, description = "OK")
    })
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE, path = "/list")
    public ResponseEntity<List<Contact>> list(){
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    @Operation(description = "Delete a contact.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = MessageUtil.OK_VALUE, description = "OK")
    })
    @DeleteMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE, path = "/")
    public ResponseEntity<Contact> delete(@PathVariable final BigInteger contactId) {
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

}

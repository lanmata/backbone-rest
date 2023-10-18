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

package com.prx.backoffice.v1.contacts.api;

import com.prx.backoffice.util.MessageUtil;
import com.prx.backoffice.v1.contacts.service.ContactService;
import com.prx.commons.pojo.Contact;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * ContactApiController.
 *
 * @author <a href="mailto:luis.antonio.mata@gmail.com">Luis Antonio Mata</a>
 * @version 1.0.0, 07-04-2022
 * @since 11
 */
@RestController
@CrossOrigin(origins = "*")
@RequestMapping("v1/contacts")
public class ContactController {

    private final ContactService contactService;

    public ContactController(ContactService contactService) {
        this.contactService = contactService;
    }

    @Operation(description = "Create a contact.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = MessageUtil.OK, description = "OK")
    })
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE, path = "/")
    public ResponseEntity<Contact> create(@RequestBody final Contact contact) {
        return contactService.create(contact);
    }

    @Operation(description = "Update a contact.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = MessageUtil.OK, description = "OK")
    })
    @PutMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE, path = "/{contactId}")
    public ResponseEntity<Contact> update(@PathVariable final String contactId, @RequestBody Contact contact) {
        return contactService.update(contact, contactId);
    }

    @Operation(description = "Find a contact list.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = MessageUtil.OK, description = "OK")
    })
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE, path = "/{contactId}")
    public ResponseEntity<Contact> find(@PathVariable(value = "contactId") final String contactId) {
        return contactService.find(contactId);
    }

    @Operation(description = "Find a contact list by ids.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = MessageUtil.OK, description = "OK")
    })
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE, path = "/list/{contactIds}")
    public ResponseEntity<List<Contact>> list(@PathVariable List<String> contactIds){
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    @Operation(description = "List contacts by person Id.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = MessageUtil.OK, description = "OK")
    })
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE, path = "/person/{personId}")
    public ResponseEntity<List<Contact>> list(@PathVariable String personId){
        return contactService.listByPersonId(personId);
    }

    @Operation(description = "Get a contact list.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = MessageUtil.OK, description = "OK")
    })
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE, path = "/list")
    public ResponseEntity<List<Contact>> list(){
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    @Operation(description = "Delete a contact.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = MessageUtil.OK, description = "OK")
    })
    @DeleteMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE, path = "/")
    public ResponseEntity<Contact> delete(@PathVariable final String contactId) {
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

}

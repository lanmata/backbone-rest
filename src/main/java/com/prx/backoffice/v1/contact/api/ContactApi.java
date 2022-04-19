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
import com.prx.backoffice.v1.contact.to.ContactRequest;
import com.prx.commons.pojo.Contact;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * ContactApi.
 *
 * @author <a href="mailto:luis.antonio.mata@gmail.com">Luis Antonio Mata</a>
 * @version 1.0.0, 07-04-2022
 * @since 11
 */
@Validated
public interface ContactApi {

    default ContactApiDelegate getDelegate() {
        return new ContactApiDelegate() {};
    }

    @Operation(description = "Create a contact.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = MessageUtil.OK_VALUE, description = "OK")
    })
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE, path = "/")
    default ResponseEntity<Contact> create(@RequestBody final ContactRequest contactRequest) {
        return getDelegate().create(contactRequest);
    }

    @Operation(description = "Find a contact list.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = MessageUtil.OK_VALUE, description = "OK")
    })
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE, path = "/{contactId}")
    default ResponseEntity<Contact> find(@PathVariable final Long contactId) {
        return getDelegate().find(contactId);
    }

    @Operation(description = "Find a contact list by ids.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = MessageUtil.OK_VALUE, description = "OK")
    })
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE, path = "/list/{contactIds}")
    default ResponseEntity<List<Contact>> list(@PathVariable List<Long> contactIds){
        return getDelegate().list(contactIds);
    }

    @Operation(description = "Get a contact list.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = MessageUtil.OK_VALUE, description = "OK")
    })
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE, path = "/list")
    default ResponseEntity<List<Contact>> list(){
        return getDelegate().list();
    }

    @Operation(description = "Delete a contact.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = MessageUtil.OK_VALUE, description = "OK")
    })
    @DeleteMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE, path = "/")
    default ResponseEntity<Contact> delete(@PathVariable final Long contactId) {
        return getDelegate().delete(contactId);
    }
}

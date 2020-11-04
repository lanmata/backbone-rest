/*
 *
 *  * @(#)PersonController.java.
 *  *
 *  * Copyright (c) Luis Antonio Mata Mata. All rights reserved.
 *  *
 *  * All rights to this product are owned by Luis Antonio Mata Mata and may only
 *  * be used under the terms of its associated license document. You may NOT
 *  * copy, modify, sublicense, or distribute this source file or portions of
 *  * it unless previously authorized in writing by Luis Antonio Mata Mata.
 *  * In any event, this notice and the above copyright must always be included
 *  * verbatim with this file.
 *
 */
package com.prx.backoffice.controller;

import com.prx.backoffice.service.PersonService;
import com.prx.backoffice.to.person.PersonCreateRequest;
import com.prx.commons.to.Response;
import com.prx.commons.util.MessageActivityUtil;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * PersonController.
 *
 * @author Luis Antonio Mata
 * @version 1.0.1.20200904-01, 04-11-2020
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/person")
@CrossOrigin(origins = "*")
public class PersonController {
    private final PersonService personService;

    @ApiOperation(value = "Crea un nueva persona",
        notes = "Crea un nuevo persona")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK!", response = Response.class),
        @ApiResponse(code = 401, message = "Persona no creada", response = Response.class),
        @ApiResponse(code = 500, message = "Error interno durante la creación de persona", response = String.class)
    })
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE, path = "/create")
    public Response create(@ApiParam(value = "Solicitud para crear persona", required = true, readOnly = true)
    @RequestBody final PersonCreateRequest personCreateRequest){
        return MessageActivityUtil.toResponse(personService.create(personCreateRequest.getPerson()));
    }
}

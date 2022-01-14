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
package com.prx.backoffice.v1.person.api.controller;

import com.prx.backoffice.v1.person.service.PersonService;
import com.prx.backoffice.v1.person.api.to.PersonCreateRequest;
import com.prx.backoffice.util.MessageUtil;
import com.prx.commons.pojo.Person;
import com.prx.commons.to.Response;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * PersonController. Clase controladora para la exposición de los endpoint pertenecientes a la gestión de usuario
 *
 * @author Luis Antonio Mata
 * @version 1.0.1.20200904-01, 04-11-2020
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/person")
public class PersonController {
    /** personService */
    private final PersonService personService;

    /**
     *
     * @param personCreateRequest {@link PersonCreateRequest}
     * @return Objeto de tipo {@link Response}
     */
    @Operation(description = "Crea un nueva persona")
    @ApiResponses(value = {
        @ApiResponse(responseCode = MessageUtil.OK_VALUE, description = "Persona creada")
//        @ApiResponse(responseCode = "400", description = "Solicitud errada"),
//        @ApiResponse(responseCode = "401", description = "Solicitante no tiene permisos, requiere autenticación"),
//            @ApiResponse(responseCode = "403", description = "Persona no creada"),
//            @ApiResponse(responseCode = "405", description = "Método no permitido"),
//            @ApiResponse(responseCode = "500", description = "Error interno durante la creación de persona")
    })
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE, path = "/create")
    public ResponseEntity<Person> create(@Parameter(description = "Solicitud para crear persona", required = true)
    @RequestBody final PersonCreateRequest personCreateRequest) {
        return personService.create(personCreateRequest.getPerson());
    }

    //TODO - metodo get para obtener los usuarios vinculados a una persona

    //TODO - metodo post para actualizas los datos de una persona

    //TODO - metodo post para obtener agregar un nuevo usuario a una persona

    //TODO - metodo post para obtener elminar un usuario a una persona
}

/*
 * @(#)RolController.java.
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
package com.prx.backoffice.controller;

import com.prx.backoffice.service.RolService;
import com.prx.backoffice.to.featurerol.RolLinkRequest;
import com.prx.backoffice.to.rol.RolCollectionResponse;
import com.prx.backoffice.to.rol.RolFindResponse;
import com.prx.backoffice.to.rol.RolRequest;
import com.prx.commons.pojo.MessageActivity;
import com.prx.commons.pojo.Rol;
import com.prx.commons.to.Response;
import com.prx.commons.util.MessageActivityUtil;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

/**
 * RolController.
 *
 * @author <a href='mailto:luis.antonio.mata@gmail.com'>Luis Antonio Mata</a>
 * @version 1.0.0, 12-02-2021
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/rol")
@CrossOrigin(origins = "*")
public class RolController {
    private final RolService rolService;

    /**
     *
     * @param rolId {@link RolRequest}
     * @return Objeto de tipo {@link RolFindResponse}
     */
    @ApiOperation(value = "Realiza la busqueda da un rol")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Rol encontrado", response = RolFindResponse.class),
            @ApiResponse(code = 404, message = "No existe rol", response = RolFindResponse.class),
            @ApiResponse(code = 500, message = "Error interno durante la busqueda del rol", response = String.class)
    })
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE, path = "/find/{rolId}")
    public RolFindResponse find(@ApiParam(value = "Solicitud para buscar un rol", required = true, readOnly = true)
                           @PathVariable final Integer rolId){
        final var rolFindResponse = new RolFindResponse();
        log.info("Inicia llamada al metodo /find/{rolId}");
        final var messageActivity = rolService.find(rolId);
        MessageActivityUtil.toResponse(messageActivity, rolFindResponse);
        rolFindResponse.setRol(messageActivity.getObjectResponse());
        log.info("Termina llamada al metodo /find/{rolId}");
        return rolFindResponse;
    }

    /**
     *
     * @param rolCreateRequest {@link RolRequest}
     * @return {@link Response}
     */
    @ApiOperation(value = "Crea un rol")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Rol creado", response = Response.class),
            @ApiResponse(code = 500, message = "Error interno durante la creación del rol", response = String.class)
    })
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE, path = "/create")
    public Response create(@ApiParam(value = "Propiedades del rol", required = true, readOnly = true)
                                    @RequestBody final RolRequest rolCreateRequest){
        log.info("Llamada al metodo /create");
        return MessageActivityUtil.toResponse(rolService.create(rolCreateRequest.getRol()));
    }

    /**
     *
     * @param rolRequest {@link RolRequest}
     * @return {@link Response}
     */
    @ApiOperation(value = "Actualiza un rol")
    @ApiResponses(value = {
            @ApiResponse(code = 202, message = "Rol actualiza", response = Response.class),
            @ApiResponse(code = 500, message = "Error interno durante la actualización del rol", response = String.class)
    })
    @PutMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE, path = "/update/{idRol}")
    public Response update(@RequestBody final RolRequest rolRequest){
        log.info("Llamada al metodo /update/{idRol}");
        return MessageActivityUtil.toResponse(rolService.update(rolRequest.getRol()));
    }

    /**
     *
     * @param rolLinkRequest {@link RolRequest}
     * @return {@link Response}
     */
    @ApiOperation(value = "Desvincula un rol con uno o mas features")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Operación unlink realizada", response = Response.class),
            @ApiResponse(code = 500, message = "Error interno durante la desvinculación de un rol con diversos features.", response = String.class)
    })
    @PutMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE, path = "/unlink/{idRol}")
    public Response unlink(@RequestParam Integer idRol, @RequestBody final RolLinkRequest rolLinkRequest){
        log.info("Llamada al metodo /unlink/{idRol}");
        return MessageActivityUtil.toResponse(rolService.unlink(idRol, rolLinkRequest.getFeatureIdList()));
    }

    /**
     *
     * @param rolLinkRequest {@link RolRequest}
     * @return {@link Response}
     */
    @ApiOperation(value = "Vincula un rol con uno o mas features")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Operación link realizada", response = Response.class),
            @ApiResponse(code = 500, message = "Error interno durante la vinculación de un rol con diversos features.", response = String.class)
    })
    @PutMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE, path = "/link/{idRol}")
    public Response link(@RequestParam Integer idRol, @RequestBody final RolLinkRequest rolLinkRequest){
        log.info("llamada al metodo /link/{idRol}");
        return MessageActivityUtil.toResponse(rolService.link(idRol, rolLinkRequest.getFeatureIdList()));
    }

    /**
     *
     * @param includeInactive {@link boolean}
     * @param roles {@link Integer}
     * @return {@link RolCollectionResponse}
     */
    @ApiOperation(value = "Busca los roles en base a un conjunto de id y estado de actividad")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Operación list realizada", response = Response.class),
            @ApiResponse(code = 500, message = "Error interno durante la obtención de los roles.", response = String.class)
    })
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE, path = "/list/{includeInactive}/{roles}")
    public RolCollectionResponse list(@ApiParam(value = "Incluye/excluye la obtención de roles inactivos")
                                          @PathVariable boolean includeInactive,
                                      @ApiParam(value = "Id de roles para a ser buscados ")
                                      @PathVariable List<Integer> roles){
        log.info("Inicia llamada al metodo /list/{includeInactive}/{roles}");
        final var rolCollectionResponse = new RolCollectionResponse();
        final var messageActivity = rolService.list(includeInactive, roles);
        rolCollectionResponse.setMessage(messageActivity.getMessage());
        rolCollectionResponse.setCode(messageActivity.getCode());
        rolCollectionResponse.setRoles(messageActivity.getObjectResponse());
        rolCollectionResponse.setDateTime(LocalDateTime.now(ZoneId.systemDefault()));
        log.info("Termina llamada al metodo /list/{includeInactive}/{roles}");
        return getRolCollectioResponse(rolService.list(includeInactive, roles));
    }

    /**
     *
     * @param includeInactive {@link boolean}
     * @return {@link RolCollectionResponse}
     */
    @ApiOperation(value = "Busca los roles en base al estado de actividad")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Operación list realizada", response = Response.class),
            @ApiResponse(code = 500, message = "Error interno durante la obtención de los roles.", response = String.class)
    })
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE, path = "/list/{includeInactive}")
    public RolCollectionResponse list(@ApiParam(value = "Incluye/excluye la obtención de roles inactivos")
                                      @PathVariable boolean includeInactive) {
        log.info("llamada al metodo /list/{includeInactive}");
        return getRolCollectioResponse(rolService.list(includeInactive, null));
    }

    /**
     * Obtiene una lista de rol en base al id de un usuario determinado.
     * @param userId {@link int}
     * @return Objeto de tipo {@link RolCollectionResponse}
     */
    @ApiOperation(value = "Busca los roles en base al id de un usuario")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Operación list realizada", response = Response.class),
            @ApiResponse(code = 500, message = "Error interno durante la obtención de los roles.", response = String.class)
    })
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE, path = "/listByUser/{userId}")
    public RolCollectionResponse list(@PathVariable long userId) {
        log.info("llamada al metodo /listByUser/{userId}");
        return getRolCollectioResponse(rolService.list(userId));
    }

    private RolCollectionResponse getRolCollectioResponse(MessageActivity<List<Rol>> messageActivity){
        log.info("Inicia la conversion de MessageActivity a RolCollectionResponse");
        final var rolCollectionResponse = new RolCollectionResponse();
        rolCollectionResponse.setCode(messageActivity.getCode());
        rolCollectionResponse.setMessage(messageActivity.getMessage());
        rolCollectionResponse.setRoles(messageActivity.getObjectResponse());
        rolCollectionResponse.setDateTime(LocalDateTime.now(ZoneId.systemDefault()));
        log.info("Termina la conversion de MessageActivity a RolCollectionResponse");
        return rolCollectionResponse;
    }

}

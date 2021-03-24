/*
 * @(#)FeatureController.java.
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

import com.prx.backoffice.service.FeatureService;
import com.prx.backoffice.to.feature.FeatureListResponse;
import com.prx.backoffice.to.feature.FeatureResponse;
import com.prx.backoffice.to.rol.RolFindResponse;
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

import java.util.List;

/**
 * FeatureController.
 *
 * @author <a href='mailto:luis.antonio.mata@gmail.com'>Luis Antonio Mata</a>
 * @version 1.0.0, 14-02-2021
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/feature")
@CrossOrigin(origins = "*")
public class FeatureController {
    private final FeatureService featureService;

    /**
     *
     * @param featureId {@link Long}
     * @return Objeto de tipo {@link RolFindResponse}
     */
    @ApiOperation(value = "Realiza la busqueda da un feature")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Feature encontrado creada", response = FeatureResponse.class),
            @ApiResponse(code = 401, message = "Solicitante no tiene permisos, requiere autenticación", response =
                    Response.class),
            @ApiResponse(code = 404, message = "Feature no encontrado", response = FeatureResponse.class),
            @ApiResponse(code = 405, message = "Método no permitido", response = Response.class),
            @ApiResponse(code = 500, message = "Error interno durante la creación del feature", response = String.class)
    })
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE, path = "/find/{featureId}")
    public FeatureResponse find(@ApiParam(value = "Id de feature", required = true, readOnly = true)
    @PathVariable final Long featureId) {
        final var featureResponse = new FeatureResponse();
        final var messageActivity = featureService.find(featureId);

        MessageActivityUtil.toResponse(messageActivity, featureResponse);
        featureResponse.setFeature(messageActivity.getObjectResponse());

        return featureResponse;
    }

    @ApiOperation(value = "Obtiene una lista de feature activos e inactivos si el valor del parametro includeInactive es verdadero")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok", response = FeatureListResponse.class),
            @ApiResponse(code=401, message = "Solicitud no valida", response = FeatureListResponse.class)
    })
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE, path = "/list/{includeInactive}/{featuresIds}")
    public FeatureListResponse list(@ApiParam(value = "Incluye/excluye feature inactivos") boolean includeInactive,
                                    @ApiParam(value = "lista de id requeridos", required = false, readOnly = true) List<Long> featureIds){
        final var featureListResponse = new FeatureListResponse();
        final var messageActivity = featureService.list(featureIds,includeInactive);
        MessageActivityUtil.toResponse(messageActivity, featureListResponse);
        featureListResponse.setList(messageActivity.getObjectResponse());

        return featureListResponse;
    }

    //TODO - metodo post para crear feature

    //TODO - metodo get para actualizar feature

    //TODO - metodo get para obtener los roles vinculados a un feature

}

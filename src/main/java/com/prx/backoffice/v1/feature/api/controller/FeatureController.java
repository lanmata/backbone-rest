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
package com.prx.backoffice.v1.feature.api.controller;

import com.prx.backoffice.util.MessageUtil;
import com.prx.backoffice.v1.feature.api.to.FeatureRequest;
import com.prx.backoffice.v1.feature.service.FeatureService;
import com.prx.commons.pojo.Feature;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * FeatureController.
 *
 * @author <a href='mailto:luis.antonio.mata@gmail.com'>Luis Antonio Mata</a>
 * @version 1.0.0, 14-02-2021
 */
@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@RequestMapping(value = "/v1/feature", consumes = MediaType.APPLICATION_JSON_VALUE,produces = MediaType.APPLICATION_JSON_VALUE)
public class FeatureController {

    private final FeatureService featureService;

    /**
     *
     * @param featureId {@link Long}
     * @return Objeto de tipo {@link ResponseEntity}
     */
    @Operation(description = "Realiza la busqueda da un feature")
    @ApiResponses(value = {
            @ApiResponse(responseCode = MessageUtil.CREATED_VALUE, description = "Feature encontrado creada")
//            ,
//            @ApiResponse(responseCode = "401", description = "Solicitante no tiene permisos, requiere autenticaci??n"),
//            @ApiResponse(responseCode = "404", description = "Feature no encontrado"),
//            @ApiResponse(responseCode = "405", description = "M??todo no permitido"),
//            @ApiResponse(responseCode = "500", description = "Error interno durante la creaci??n del feature")
    })
    @GetMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE, path = "/{featureId}")
    public ResponseEntity<Feature> find(@Parameter(description = "Id de feature", required = true)
                                        @PathVariable final Long featureId) {
        return featureService.find(featureId);
    }

    @Operation(description = "Get a list of active and/or inactive features.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = MessageUtil.OK_VALUE, description = "OK"),
            @ApiResponse(responseCode = MessageUtil.NOT_FOUND_VALUE, description = "NOT FOUND")
    })
    @GetMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE, path = "/list/{includeInactive}")
    public ResponseEntity<List<Feature>> list(@Parameter(description = "Non/Include the features inactive", required = true)
                                              @PathVariable boolean includeInactive){
        return featureService.list(null,includeInactive);
    }

    @Operation(description = "Get a list of active and/or inactive features.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = MessageUtil.OK_VALUE, description = "OK"),
            @ApiResponse(responseCode = MessageUtil.NOT_FOUND_VALUE, description = "NOT FOUND")
    })
    @GetMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE, path = "/list/{includeInactive}/{featuresIds}")
    public ResponseEntity<List<Feature>> list(@Parameter(description = "Non/Include the features inactive", required = true)
                                              @PathVariable boolean includeInactive,
                                              @Parameter(description = "Features id list") @PathVariable List<Long> featuresIds){
        return featureService.list(featuresIds,includeInactive);
    }

    @Operation(description = "Create a Feature")
    @ApiResponses(value = {
            @ApiResponse(responseCode = MessageUtil.OK_VALUE, description = "OK"),
            @ApiResponse(responseCode = MessageUtil.NOT_FOUND_VALUE, description = "NOT FOUND")
    })
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE, path = "/")
    public ResponseEntity<Feature> create(@Parameter(description = "Feature properties", required = true) @RequestBody FeatureRequest featureRequest) {
        return featureService.create(featureRequest.getFeature());
    }

    @Operation(description = "Update a Feature")
    @ApiResponses(value = {
            @ApiResponse(responseCode = MessageUtil.OK_VALUE, description = "Ok")
//            ,
//            @ApiResponse(responseCode = "401", description = "Solicitud no valida")
    })
    @PutMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE, path = "/{featureId}")
    public ResponseEntity<Feature> update(@PathVariable Long featureId, @RequestBody FeatureRequest featureRequest){
        return featureService.update(featureId, featureRequest.getFeature());
    }
}

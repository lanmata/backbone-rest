/*
 *  @(#)FeatureController.java
 *
 *  Copyright (c) Luis Antonio Mata Mata. All rights reserved.
 *
 *   All rights to this product are owned by Luis Antonio Mata Mata and may only
 *  be used under the terms of its associated license document. You may NOT
 *  copy, modify, sublicense, or distribute this source file or portions of
 *  it unless previously authorized in writing by Luis Antonio Mata Mata.
 *  In any event, this notice and the above copyright must always be included
 *  verbatim with this file.
 */
package com.prx.backoffice.v1.features.api.controller;

import com.prx.backoffice.util.MessageUtil;
import com.prx.backoffice.v1.features.api.to.FeatureRequest;
import com.prx.backoffice.v1.features.service.FeatureService;
import com.prx.commons.general.pojo.Feature;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * FeatureController.
 *
 * @author <a href='mailto:luis.antonio.mata@gmail.com'>Luis Antonio Mata</a>
 * @version 1.0.0, 14-02-2021
 */
@RestController
@CrossOrigin(origins = "*")
@RequestMapping(value = "/api/v1/features")
public class FeatureController {

    private final FeatureService featureService;

    public FeatureController(FeatureService featureService) {
        this.featureService = featureService;
    }

    /**
     *
     * @param featureId {@link Long}
     * @return Objeto de tipo {@link ResponseEntity}
     */
    @Operation(description = "Realiza la busqueda da un feature")
    @ApiResponses(value = {
            @ApiResponse(responseCode = MessageUtil.CREATED, description = "Feature encontrado creada"),
            @ApiResponse(responseCode = MessageUtil.NOT_FOUND, description = "NOT FOUND")
    })
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE, path = "/find/{featureId}")
    public ResponseEntity<Feature> find(@Parameter(description = "Id de feature", required = true)
                                        @PathVariable final UUID featureId) {
        return featureService.find(featureId);
    }

    @Operation(description = "Get a list of active and/or inactive features.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = MessageUtil.OK, description = "OK"),
            @ApiResponse(responseCode = MessageUtil.NOT_FOUND, description = "NOT FOUND")
    })
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE, path = "/{includeInactive}")
    public ResponseEntity<List<Feature>> list(@Parameter(description = "Non/Include the features inactive", required = true)
                                              @PathVariable boolean includeInactive){
        return featureService.list(null,includeInactive);
    }

    @Operation(description = "Get a list of active and/or inactive features.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = MessageUtil.OK, description = "OK"),
            @ApiResponse(responseCode = MessageUtil.NOT_FOUND, description = "NOT FOUND")
    })
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE, path = "/{includeInactive}/{featuresIds}")
    public ResponseEntity<List<Feature>> list(@Parameter(description = "Non/Include the features inactive", required = true)
                                              @PathVariable boolean includeInactive,
                                              @Parameter(description = "Features id list") @PathVariable List<String> featuresIds){
        return featureService.list(featuresIds,includeInactive);
    }

    @Operation(description = "Create a Feature")
    @ApiResponses(value = {
            @ApiResponse(responseCode = MessageUtil.CREATED, description = "OK"),
            @ApiResponse(responseCode = MessageUtil.NOT_ACCEPTABLE, description = "NOT ACCEPTABLE")
    })
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE, path = "/")
    public ResponseEntity<Feature> create(@Parameter(description = "Feature properties", required = true) @RequestBody FeatureRequest featureRequest) {
        return featureService.create(featureRequest.getFeature());
    }

    @Operation(description = "Update a Feature")
    @ApiResponses(value = {
            @ApiResponse(responseCode = MessageUtil.ACCEPTED, description = "Ok"),
            @ApiResponse(responseCode = MessageUtil.NOT_ACCEPTABLE, description = "Feature not registered")
    })
    @PutMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE, path = "/{featureId}")
    public ResponseEntity<Feature> update(@PathVariable UUID featureId, @RequestBody FeatureRequest featureRequest){
        return featureService.update(featureId, featureRequest.getFeature());
    }
}

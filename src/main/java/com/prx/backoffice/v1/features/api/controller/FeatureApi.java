/*
 *  @(#)FeatureApi.java
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

import com.prx.backoffice.v1.features.service.FeatureService;
import com.prx.backoffice.v1.features.api.to.FeatureRequest;
import com.prx.commons.general.pojo.Feature;
import com.prx.commons.util.HttpStatusUtil;
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
 * FeatureApi interface for feature endpoints.
 */
public interface FeatureApi {
    /**
     * Returns the FeatureService instance.
     * @return FeatureService
     */
    default FeatureService getService() {
        return new FeatureService() {
        };
    }

    /**
     * Searches for a specific feature by its unique identifier.
     *
     * @param featureId the unique identifier of the feature to be searched
     * @return a {@link ResponseEntity} containing the {@link Feature} if found;
     *         the response code will represent either success or not found status
     */
    @Operation(description = "Realiza la busqueda da un feature")
    @ApiResponses(value = {
            @ApiResponse(responseCode = HttpStatusUtil.CREATED_STR, description = "Feature encontrado creada"),
            @ApiResponse(responseCode = HttpStatusUtil.NOT_FOUND_STR, description = "NOT FOUND")
    })
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE, path = "/find/{featureId}")
    default ResponseEntity<Feature> find(@Parameter(description = "Id de feature", required = true)
                                         @PathVariable final UUID featureId) {
        return getService().find(featureId);
    }

    /**
     * Retrieves a list of features that can include active and/or inactive features based on the specified parameter.
     *
     * @param includeInactive a boolean indicating whether to include inactive features in the result
     * @return a {@link ResponseEntity} containing a list of {@link Feature} objects
     */
    @Operation(description = "Get a list of active and/or inactive features.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = HttpStatusUtil.OK_STR, description = "OK"),
            @ApiResponse(responseCode = HttpStatusUtil.NOT_FOUND_STR, description = "NOT FOUND")
    })
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE, path = "/{includeInactive}")
    default ResponseEntity<List<Feature>> list(@Parameter(description = "Non/Include the features inactive", required = true)
                                               @PathVariable boolean includeInactive){
        return getService().list(null,includeInactive);
    }

    /**
     * Retrieves a list of features that can include active and/or inactive features based on the specified parameters.
     *
     * @param includeInactive a boolean indicating whether to include inactive features in the result
     * @param featuresIds a list of feature IDs to filter the result set
     * @return a {@link ResponseEntity} containing a list of matching {@link Feature} objects
     */
    @Operation(description = "Get a list of active and/or inactive features.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = HttpStatusUtil.OK_STR, description = "OK"),
            @ApiResponse(responseCode = HttpStatusUtil.NOT_FOUND_STR, description = "NOT FOUND")
    })
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE, path = "/{includeInactive}/{featuresIds}")
    default ResponseEntity<List<Feature>> list(@Parameter(description = "Non/Include the features inactive", required = true)
                                               @PathVariable boolean includeInactive,
                                               @Parameter(description = "Features id list") @PathVariable List<String> featuresIds){
        return getService().list(featuresIds,includeInactive);
    }

    /**
     * Creates a new feature based on the given feature request.
     *
     * @param featureRequest an instance of {@link FeatureRequest} containing the feature properties to be created
     * @return a {@link ResponseEntity} containing the created {@link Feature}
     */
    @Operation(description = "Create a Feature")
    @ApiResponses(value = {
            @ApiResponse(responseCode = HttpStatusUtil.CREATED_STR, description = "OK"),
            @ApiResponse(responseCode = HttpStatusUtil.NOT_ACCEPTABLE_STR, description = "NOT ACCEPTABLE")
    })
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE, path = "/")
    default ResponseEntity<Feature> create(@Parameter(description = "Feature properties", required = true) @RequestBody FeatureRequest featureRequest) {
        return getService().create(featureRequest.getFeature());
    }

    /**
     * Updates an existing feature based on the given feature ID and feature request.
     *
     * @param featureId the unique identifier of the feature to be updated
     * @param featureRequest an instance of {@link FeatureRequest} containing the updated feature properties
     * @return a {@link ResponseEntity} containing the updated {@link Feature};
     *         the response code will be 202 (Accepted) if the update succeeds,
     *         or 406 (Not Acceptable) if the feature is not registered
     */
    @Operation(description = "Update a Feature")
    @ApiResponses(value = {
            @ApiResponse(responseCode = HttpStatusUtil.ACCEPTED_STR, description = "Ok"),
            @ApiResponse(responseCode = HttpStatusUtil.NOT_ACCEPTABLE_STR, description = "Feature not registered")
    })
    @PutMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE, path = "/{featureId}")
    default ResponseEntity<Feature> update(@PathVariable UUID featureId, @RequestBody FeatureRequest featureRequest){
        return getService().update(featureId, featureRequest.getFeature());
    }
}


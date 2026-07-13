/*
 *  @(#)ServiceTypeApi.java
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
package com.umdc.backoffice.v1.servicetype.api.controller;

import com.umdc.backoffice.v1.servicetype.api.to.ServiceTypeRequest;
import com.umdc.backoffice.v1.servicetype.service.ServiceTypeService;
import com.umdc.commons.general.pojo.ServiceType;
import com.umdc.commons.util.HttpStatusUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.UUID;

/// Interface for the Service Type API.
/// Provides endpoints for managing service types.
@Tag(name = "service-type", description = "The service type API")
@RequestMapping("/api/v1/service-types")
public interface ServiceTypeApi {

    /// Gets the service type service.
    ///
    /// @return the service type service
    default ServiceTypeService getService() {
        return new ServiceTypeService() {
        };
    }

    /// Returns all registered service types.
    ///
    /// @return all service types wrapped in a ResponseEntity
    @Operation(summary = "List all service types", description = "Returns every registered service type.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = HttpStatusUtil.OK_STR, description = "Service type list returned."),
            @ApiResponse(responseCode = HttpStatusUtil.NOT_FOUND_STR, description = "No service types found.")
    })
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    default ResponseEntity<List<ServiceType>> listAll() {
        return getService().listAll();
    }

    /// Returns service types filtered by active status.
    ///
    /// @param active whether to return active or inactive service types
    /// @return matching service types wrapped in a ResponseEntity
    @Operation(summary = "List service types by status", description = "Returns service types filtered by active flag.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = HttpStatusUtil.OK_STR, description = "Service type list returned."),
            @ApiResponse(responseCode = HttpStatusUtil.NOT_FOUND_STR, description = "No service types found.")
    })
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE, path = "/{active}")
    default ResponseEntity<List<ServiceType>> listByStatus(
            @Parameter(description = "Active status filter", required = true)
            @PathVariable boolean active) {
        return getService().listByStatus(active);
    }

    /// Finds a service type by its ID.
    ///
    /// @param serviceTypeId the unique identifier of the service type
    /// @return the found service type wrapped in a ResponseEntity
    @Operation(summary = "Find service type by ID", description = "Returns a single service type by its UUID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = HttpStatusUtil.OK_STR, description = "Service type found."),
            @ApiResponse(responseCode = HttpStatusUtil.NOT_FOUND_STR, description = "Service type not found."),
            @ApiResponse(responseCode = HttpStatusUtil.BAD_REQUEST_STR, description = "Invalid ID supplied.")
    })
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE, path = "/find/{serviceTypeId}")
    default ResponseEntity<ServiceType> find(
            @Parameter(description = "Service type UUID", required = true)
            @PathVariable UUID serviceTypeId) {
        return getService().find(serviceTypeId);
    }

    /// Creates a new service type.
    ///
    /// @param serviceTypeRequest the service type creation request
    /// @return the created service type wrapped in a ResponseEntity
    @Operation(summary = "Create a service type", description = "Creates a new service type.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = HttpStatusUtil.CREATED_STR, description = "Service type created successfully."),
            @ApiResponse(responseCode = HttpStatusUtil.BAD_REQUEST_STR, description = "Invalid request body."),
            @ApiResponse(responseCode = HttpStatusUtil.CONFLICT_STR, description = "Service type name already in use.")
    })
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE, path = "/")
    default ResponseEntity<ServiceType> create(
            @Parameter(description = "Service type properties", required = true)
            @RequestBody ServiceTypeRequest serviceTypeRequest) {
        return getService().create(serviceTypeRequest.getServiceType());
    }

    /// Updates an existing service type.
    ///
    /// @param serviceTypeId      the unique identifier of the service type to update
    /// @param serviceTypeRequest the service type update request
    /// @return the updated service type wrapped in a ResponseEntity
    @Operation(summary = "Update a service type", description = "Updates an existing service type by its UUID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = HttpStatusUtil.ACCEPTED_STR, description = "Service type updated."),
            @ApiResponse(responseCode = HttpStatusUtil.NOT_FOUND_STR, description = "Service type not found."),
            @ApiResponse(responseCode = HttpStatusUtil.BAD_REQUEST_STR, description = "Invalid request.")
    })
    @PutMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE, path = "/{serviceTypeId}")
    default ResponseEntity<ServiceType> update(
            @Parameter(description = "Service type UUID", required = true)
            @PathVariable UUID serviceTypeId,
            @Parameter(description = "Updated service type properties", required = true)
            @RequestBody ServiceTypeRequest serviceTypeRequest) {
        return getService().update(serviceTypeId, serviceTypeRequest.getServiceType());
    }
}

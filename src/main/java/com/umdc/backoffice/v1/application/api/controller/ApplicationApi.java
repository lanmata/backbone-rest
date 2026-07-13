/*
 *  @(#)ApplicationApi.java
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

package com.umdc.backoffice.v1.application.api.controller;

import com.umdc.backoffice.v1.application.api.to.ApplicationCreateRequest;
import com.umdc.backoffice.v1.application.api.to.ApplicationUpdateRequest;
import com.umdc.backoffice.v1.application.service.ApplicationService;
import com.umdc.commons.general.pojo.Application;
import com.umdc.commons.util.HttpStatusUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.UUID;

/// Interface for the Application API.
@Tag(name = "applications", description = "Application (service) management")
public interface ApplicationApi {

    /// Gets the application service.
    ///
    /// @return the application service
    default ApplicationService getService() {
        return new ApplicationService() {};
    }

    /// Returns all registered applications.
    ///
    /// @return all applications wrapped in a ResponseEntity
    @Operation(summary = "List all applications", description = "Returns every registered application.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = HttpStatusUtil.OK_STR, description = "Application list returned."),
        @ApiResponse(responseCode = HttpStatusUtil.NOT_FOUND_STR, description = "No applications found.")
    })
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    default ResponseEntity<List<Application>> listAll() {
        return this.getService().listAll();
    }

    /// Returns applications matching the given IDs.
    ///
    /// @param ids comma-separated list of application UUIDs
    /// @return matched applications wrapped in a ResponseEntity
    @Operation(summary = "List applications by IDs", description = "Returns applications matching the provided UUIDs.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = HttpStatusUtil.OK_STR, description = "Applications returned."),
        @ApiResponse(responseCode = HttpStatusUtil.NOT_FOUND_STR, description = "No applications found for the given IDs.")
    })
    @GetMapping(params = "ids", produces = MediaType.APPLICATION_JSON_VALUE)
    default ResponseEntity<List<Application>> list(@RequestParam List<UUID> ids) {
        return this.getService().list(ids.toArray(new UUID[0]));
    }

    /// Creates a new application.
    ///
    /// @param applicationCreateRequest the application creation request
    /// @return the created application wrapped in a ResponseEntity
    @Operation(description = "Creates a new application.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = HttpStatusUtil.CREATED_STR, description = "Application created successfully."),
        @ApiResponse(responseCode = HttpStatusUtil.BAD_REQUEST_STR, description = "Invalid request body."),
        @ApiResponse(responseCode = HttpStatusUtil.INTERNAL_SERVER_ERROR_STR, description = "Internal server error.")
    })
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    default ResponseEntity<Application> create(@RequestBody ApplicationCreateRequest applicationCreateRequest) {
        return this.getService().create(applicationCreateRequest.getApplication());
    }

    /// Finds an application by its ID.
    ///
    /// @param id the application UUID
    /// @return the application wrapped in a ResponseEntity
    @Operation(summary = "Find application by ID", description = "Returns the application matching the given UUID.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = HttpStatusUtil.OK_STR, description = "Application found."),
        @ApiResponse(responseCode = HttpStatusUtil.NOT_FOUND_STR, description = "Application not found."),
        @ApiResponse(responseCode = HttpStatusUtil.UNAUTHORIZED_STR, description = "Unauthorized.")
    })
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    default ResponseEntity<Application> find(@PathVariable UUID id) {
        return this.getService().find(id);
    }

    /// Updates an existing application.
    ///
    /// @param id      the application UUID
    /// @param request the update request body
    /// @return the updated application wrapped in a ResponseEntity
    @Operation(summary = "Update an application", description = "Updates the application identified by the given UUID.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = HttpStatusUtil.OK_STR, description = "Application updated successfully."),
        @ApiResponse(responseCode = HttpStatusUtil.BAD_REQUEST_STR, description = "Invalid request body."),
        @ApiResponse(responseCode = HttpStatusUtil.NOT_FOUND_STR, description = "Application not found."),
        @ApiResponse(responseCode = HttpStatusUtil.UNAUTHORIZED_STR, description = "Unauthorized.")
    })
    @PutMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    default ResponseEntity<Application> update(@PathVariable UUID id, @RequestBody ApplicationUpdateRequest request) {
        return this.getService().update(id, request.getApplication());
    }

    /// Deletes an application by its ID.
    ///
    /// @param id the application UUID
    /// @return empty response with status header
    @Operation(summary = "Delete an application", description = "Deletes the application identified by the given UUID.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = HttpStatusUtil.OK_STR, description = "Application deleted successfully."),
        @ApiResponse(responseCode = HttpStatusUtil.NOT_FOUND_STR, description = "Application not found."),
        @ApiResponse(responseCode = HttpStatusUtil.UNAUTHORIZED_STR, description = "Unauthorized.")
    })
    @DeleteMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    default ResponseEntity<Application> delete(@PathVariable UUID id) {
        return this.getService().delete(id, null);
    }
}

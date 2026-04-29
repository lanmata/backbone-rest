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
import com.umdc.backoffice.v1.application.service.ApplicationService;
import com.prx.commons.general.pojo.Application;
import com.prx.commons.util.HttpStatusUtil;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

/// Interface for the Application API.
/// Provides endpoints for creating applications.
@Tag(name="service", description="The service API")
public interface ApplicationApi {

    /// Gets the application service.
    ///
    /// @return the application service
    default ApplicationService getService() {
        return new ApplicationService() {};
    }

    /// Creates a new application.
    ///
    /// @param applicationCreateRequest the application creation request
    /// @return the response entity containing the created application
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
}

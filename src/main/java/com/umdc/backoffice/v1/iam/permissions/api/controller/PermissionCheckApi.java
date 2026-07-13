/*
 *  @(#)PermissionCheckApi.java
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
package com.umdc.backoffice.v1.iam.permissions.api.controller;

import com.umdc.backoffice.v1.iam.permissions.api.to.PermissionCheckRequest;
import com.umdc.backoffice.v1.iam.permissions.api.to.PermissionCheckResponse;
import com.umdc.backoffice.v1.iam.permissions.service.PermissionCheckService;
import com.umdc.commons.util.HttpStatusUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import jakarta.validation.Valid;

/**
 * REST API interface for IAM permission checks.
 * <p>
 * Requires a valid session-token JWT — this endpoint is <em>not</em> in {@code umdc.api.excludes}.
 * </p>
 */
@Tag(name = "iam-permissions", description = "IAM Permission Check API")
@RequestMapping("/api/v1/iam/permissions")
public interface PermissionCheckApi {

    /**
     * Returns the {@link PermissionCheckService} used by this API.
     *
     * @return the service instance
     */
    default PermissionCheckService getService() {
        return request -> ResponseEntity.status(HttpStatusUtil.NOT_IMPLEMENTED).build();
    }

    /**
     * Checks whether the session token in the request carries the requested permission.
     *
     * @param request the permission check request
     * @return a {@link ResponseEntity} wrapping {@link PermissionCheckResponse}
     */
    @Operation(summary = "Check permission", description = "Validates a session token and checks whether it carries the requested permission")
    @ApiResponses(value = {
            @ApiResponse(responseCode = HttpStatusUtil.OK_STR, description = "Permission check result returned"),
            @ApiResponse(responseCode = HttpStatusUtil.UNAUTHORIZED_STR, description = "Invalid or expired session token"),
            @ApiResponse(responseCode = HttpStatusUtil.BAD_REQUEST_STR, description = "Missing or malformed request payload")
    })
    @PostMapping(value = "/check", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    default ResponseEntity<PermissionCheckResponse> check(@Valid @RequestBody PermissionCheckRequest request) {
        return getService().check(request);
    }
}


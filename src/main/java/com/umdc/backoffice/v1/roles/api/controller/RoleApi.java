/*
 *  @(#)RoleApi.java
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

package com.umdc.backoffice.v1.roles.api.controller;

import com.umdc.backoffice.v1.roles.api.to.RoleRequest;
import com.umdc.backoffice.v1.roles.service.RoleService;
import com.umdc.commons.general.pojo.Role;
import com.umdc.commons.util.HttpStatusUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * RoleApi interface for role endpoints.
 */
public interface RoleApi {
    /**
     * Returns the RoleService instance.
     * @return RoleService instance
     */
    default RoleService getService() {
        return null;
    }

    /**
     * Find a role by id.
     * @param roleId Role identifier
     * @return ResponseEntity with found Role
     * @throws Exception if not found
     */
    @Operation(description = "Look for a role")
    @ApiResponses(value = {
            @ApiResponse(responseCode = HttpStatusUtil.OK_STR, description = ""),
            @ApiResponse(responseCode = HttpStatusUtil.NOT_FOUND_STR, description = "")
    })
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE, path = "/find/{roleId}")
    default ResponseEntity<Role> find(@PathVariable UUID roleId){
        return getService().find(roleId);
    }

    /**
     * List roles by status and role ids.
     * @param includeInactive Include/exclude inactive roles
     * @param roleIds List of role ids
     * @return ResponseEntity with list of Role
     */
    @Operation(description = "List Roles by status and role id.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = HttpStatusUtil.OK_STR, description = ""),
            @ApiResponse(responseCode = HttpStatusUtil.NOT_FOUND_STR, description = "")
    })
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE, path = "/{includeInactive}/{roleIds}")
    default ResponseEntity<List<Role>> list(@PathVariable boolean includeInactive, @PathVariable List<UUID> roleIds) {
        return getService().list(includeInactive, roleIds);
    }

    /**
     * List roles by status.
     * @param includeInactive Include/exclude inactive roles
     * @return ResponseEntity with list of Role
     */
    @Operation(description = "List Roles by status and role id.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = HttpStatusUtil.OK_STR, description = ""),
            @ApiResponse(responseCode = HttpStatusUtil.NOT_FOUND_STR, description = "")
    })
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE, path = "/{includeInactive}")
    default ResponseEntity<List<Role>> list(@PathVariable boolean includeInactive) {
        return getService().list(includeInactive, null);
    }

    /**
     * List all roles.
     * @return ResponseEntity with list of Role
     */
    @Operation(description = "List Roles by status and role id.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = HttpStatusUtil.OK_STR, description = ""),
            @ApiResponse(responseCode = HttpStatusUtil.NOT_FOUND_STR, description = "")
    })
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    default ResponseEntity<List<Role>> list() {
        return getService().list();
    }

    /**
     * Create a role.
     * @param roleCreateRequest RoleRequest object
     * @return ResponseEntity with created Role
     */
    @Operation(description = "Create a role")
    @ApiResponses(value = {
            @ApiResponse(responseCode = HttpStatusUtil.OK_STR, description = ""),
            @ApiResponse(responseCode = HttpStatusUtil.BAD_REQUEST_STR, description = "Role null.")
    })
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE, path = "/")
    default ResponseEntity<Role> create(@RequestBody RoleRequest roleCreateRequest){
        return getService().create(roleCreateRequest.getRole());
    }

    /**
     * Update a role.
     * @param roleId Role identifier
     * @param roleRequest RoleRequest object
     * @return ResponseEntity with updated Role
     */
    @Operation(description = "Update a role")
    @ApiResponses(value = {
            @ApiResponse(responseCode = HttpStatusUtil.OK_STR, description = ""),
            @ApiResponse(responseCode = HttpStatusUtil.NOT_FOUND_STR, description = "")
    })
    @PutMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE, path = "/{roleId}")
    default ResponseEntity<Role> update(@PathVariable UUID roleId, @RequestBody RoleRequest roleRequest){
        return getService().update(roleId, roleRequest.getRole());
    }

    /**
     * List roles by user id.
     * @param userId User identifier
     * @return ResponseEntity with list of Role
     */
    @Operation(description = "List Roles by Id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = HttpStatusUtil.OK_STR, description = ""),
            @ApiResponse(responseCode = HttpStatusUtil.NOT_FOUND_STR, description = "")
    })
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE, path = "/user/{userId}")
    default ResponseEntity<List<Role>> listByUser(@PathVariable UUID userId) {
        return getService().listByUser(userId);
    }
}


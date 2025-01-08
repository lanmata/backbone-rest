/*
 *  @(#)RoleController.java
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
package com.prx.backoffice.v1.roles.api.controller;

import com.prx.backoffice.util.MessageUtil;
import com.prx.backoffice.v1.roles.api.to.RoleCollectionResponse;
import com.prx.backoffice.v1.roles.api.to.RoleFindResponse;
import com.prx.backoffice.v1.roles.api.to.RoleRequest;
import com.prx.backoffice.v1.roles.service.RoleService;
import com.prx.commons.general.pojo.Role;
import com.prx.commons.general.to.Response;
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
 * RolController.
 *
 * @author <a href='mailto:luis.antonio.mata@gmail.com'>Luis Antonio Mata</a>
 * @version 1.0.0, 12-02-2021
 */
@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/v1/roles")
class RoleController {

    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    /**
     *
     * @param roleId {@link RoleRequest}
     * @return Objeto de tipo {@link RoleFindResponse}
     */
    @Operation          (description = "Look for a roles")
    @ApiResponses(value = {
            @ApiResponse(responseCode = MessageUtil.OK, description = MessageUtil.OK_VALUE),
            @ApiResponse(responseCode = MessageUtil.NOT_FOUND, description = MessageUtil.NOT_FOUND)
    })
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE, path = "/find/{roleId}")
    public ResponseEntity<Role> find(@Parameter(description = "Request to find a role", required = true)
                                     @PathVariable final UUID roleId){
        return roleService.find(roleId);
    }

    /**
     *
     * @param includeInactive {@link boolean}
     * @return {@link RoleCollectionResponse}
     */
    @Operation(description = "List Roles by status and role id.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = MessageUtil.OK, description = MessageUtil.OK_VALUE),
            @ApiResponse(responseCode = MessageUtil.NOT_FOUND, description = MessageUtil.NOT_FOUND)
    })
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE, path = "/{includeInactive}/{roleIds}")
    public ResponseEntity<List<Role>> list(@Parameter(description = "Include/exclude inactive roles.")
                                               @PathVariable boolean includeInactive,
                                           @Parameter(description = "Role list requested.")
                                           @PathVariable List<UUID> roleIds) {
        return roleService.list(includeInactive, roleIds);
    }

    /**
     *
     * @param includeInactive {@link boolean}
     * @return {@link RoleCollectionResponse}
     */
    @Operation(description = "List Roles by status and role id.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = MessageUtil.OK, description = MessageUtil.OK_VALUE),
            @ApiResponse(responseCode = MessageUtil.NOT_FOUND, description = MessageUtil.NOT_FOUND)
    })
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE, path = "/{includeInactive}")
    public ResponseEntity<List<Role>> list(@Parameter(description = "Include/exclude inactive roles.")
                                           @PathVariable boolean includeInactive) {
        return roleService.list(includeInactive, null);
    }

    /**
     *
     * @return {@link RoleCollectionResponse}
     */
    @Operation(description = "List Roles by status and role id.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = MessageUtil.OK, description = MessageUtil.OK_VALUE),
            @ApiResponse(responseCode = MessageUtil.NOT_FOUND, description = MessageUtil.NOT_FOUND)
    })
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Role>> list() {
        return roleService.list();
    }

    /**
     *
     * @param roleCreateRequest {@link RoleRequest}
     * @return {@link Response}
     */
    @Operation(description = "Create a role")
    @ApiResponses(value = {
            @ApiResponse(responseCode = MessageUtil.OK, description = MessageUtil.OK_VALUE),
            @ApiResponse(responseCode = MessageUtil.BAD_REQUEST, description = "Role null."),
            @ApiResponse(responseCode = MessageUtil.UNPROCESSABLE_ENTITY, description = "Role with content bad.")
    })
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE, path = "/")
    public ResponseEntity<Role> create(@Parameter(description = "Role properties", required = true)
                                       @RequestBody final RoleRequest roleCreateRequest){
        return roleService.create(roleCreateRequest.getRole());
    }

    /**
     *
     * @param roleRequest {@link RoleRequest}
     * @return {@link Response}
     */
    @Operation(description = "Update a role")
    @ApiResponses(value = {
            @ApiResponse(responseCode = MessageUtil.OK, description = MessageUtil.OK_VALUE),
            @ApiResponse(responseCode = MessageUtil.NOT_FOUND, description = MessageUtil.NOT_FOUND)
    })
    @PutMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE, path = "/{roleId}")
    public ResponseEntity<Role> update(@PathVariable(value = "roleId") UUID roleId, @RequestBody final RoleRequest roleRequest){
        return roleService.update(roleId, roleRequest.getRole());
    }

    /**
     * Obtiene una lista de rol en base al id de un usuario determinado.
     * @param userId {@link int}
     * @return Objeto de tipo {@link RoleCollectionResponse}
     */
    @Operation(description = "List Roles by Id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = MessageUtil.OK, description = MessageUtil.OK_VALUE),
            @ApiResponse(responseCode = MessageUtil.NOT_FOUND, description = MessageUtil.NOT_FOUND)
    })
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE, path = "/user/{userId}")
    public ResponseEntity<List<Role>> list(@PathVariable UUID userId) {
        return roleService.listByUser(userId);
    }

}

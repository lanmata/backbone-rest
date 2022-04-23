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
package com.prx.backoffice.v1.role.api.controller;

import com.prx.backoffice.util.MessageUtil;
import com.prx.backoffice.v1.role.api.to.RoleCollectionResponse;
import com.prx.backoffice.v1.role.api.to.RoleFindResponse;
import com.prx.backoffice.v1.role.api.to.RoleLinkRequest;
import com.prx.backoffice.v1.role.api.to.RoleRequest;
import com.prx.backoffice.v1.role.service.RoleService;
import com.prx.commons.pojo.Role;
import com.prx.commons.to.Response;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * RolController.
 *
 * @author <a href='mailto:luis.antonio.mata@gmail.com'>Luis Antonio Mata</a>
 * @version 1.0.0, 12-02-2021
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@RequestMapping("v1/role")
class RoleController {

    private final RoleService roleService;

    /**
     *
     * @param roleId {@link RoleRequest}
     * @return Objeto de tipo {@link RoleFindResponse}
     */
    @Operation          (description = "Look for a rol")
    @ApiResponses(value = {
            @ApiResponse(responseCode = MessageUtil.OK_VALUE, description = "Role founded")
    })
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE, path = "/find/{roleId}")
    public ResponseEntity<Role> find(@Parameter(description = "Request to find a role", required = true)
                                     @PathVariable final Long roleId){
        log.info("Inicia llamada al metodo /find/{roleId}");
        final var responseEntity = roleService.find(roleId);
        log.info("Termina llamada al metodo /find/{roleId}");
        return responseEntity;
    }

    /**
     *
     * @param roleCreateRequest {@link RoleRequest}
     * @return {@link Response}
     */
    @Operation(description = "Create a role")
    @ApiResponses(value = {
            @ApiResponse(responseCode = MessageUtil.OK_VALUE, description = "Role created.")
    })
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE, path = "/")
    public ResponseEntity<Role> create(@Parameter(description = "Role properties", required = true)
                                       @RequestBody final RoleRequest roleCreateRequest){
        log.info("Called the /create operation.");
        return roleService.create(roleCreateRequest.getRole());
    }

    /**
     *
     * @param roleRequest {@link RoleRequest}
     * @return {@link Response}
     */
    @Operation(description = "Update a role")
    @ApiResponses(value = {
            @ApiResponse(responseCode = MessageUtil.OK_VALUE, description = "Update a role")
    })
    @PutMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE, path = "/update/{roleId}")
    public ResponseEntity<Role> update(@PathVariable(value = "roleId") Long roleId, @RequestBody final RoleRequest roleRequest){
        log.info("{} /update/{idRole}", MessageUtil.LOG_START_MSG);
        return roleService.update(roleId, roleRequest.getRole());
    }

    /**
     *
     * @param roleLinkRequest {@link RoleRequest}
     * @return {@link Response}
     */
    @Operation(description = "Unlink a role with one or more features")
    @ApiResponses(value = {
            @ApiResponse(responseCode = MessageUtil.OK_VALUE, description = "Unlink completed")
    })
    @PutMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE, path = "/unlink/{idRole}")
    public ResponseEntity<Role> unlink(@PathVariable Long idRole, @RequestBody final RoleLinkRequest roleLinkRequest){
        log.info("{} /unlink/{idRole}", MessageUtil.LOG_START_MSG);
        return roleService.unlink(idRole, roleLinkRequest.getFeatureIdList());
    }

    /**
     *
     * @param roleLinkRequest {@link RoleRequest}
     * @return {@link Response}
     */
    @Operation(description = "Link a role with one or more features.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = MessageUtil.OK_VALUE, description = "Operación link realizada")
    })
    @PutMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE, path = "/link/{idRole}")
    public ResponseEntity<Role> link(@PathVariable Long idRole, @RequestBody final RoleLinkRequest roleLinkRequest){
        log.info("{} /link/{idRole}", MessageUtil.LOG_START_MSG);
        return roleService.link(idRole, roleLinkRequest.getFeatureIdList());
    }

    /**
     *
     * @param includeInactive {@link boolean}
     * @param roles {@link Integer}
     * @return {@link RoleCollectionResponse}
     */
    @Operation(description = "Busca los roles en base a un conjunto de id y estado de actividad")
    @ApiResponses(value = {
            @ApiResponse(responseCode = MessageUtil.OK_VALUE, description = "Operación list realizada")
    })
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE, path = "/list/{includeInactive}/{roles}")
    public ResponseEntity<List<Role>> list(@Parameter(description = "Incluye/excluye la obtención de roles inactivos")
                                           @PathVariable boolean includeInactive,
                                           @Parameter(description = "Id de roles para a ser buscados ")
                                           @PathVariable List<Long> roles){
        log.info("{} /list/{includeInactive}/{roles}", MessageUtil.LOG_START_MSG);
        final var messageActivity = roleService.list(includeInactive, roles);
        log.info("{} /list/{includeInactive}/{roles}", MessageUtil.LOG_END_MSG);
        return messageActivity;
    }

    /**
     *
     * @param includeInactive {@link boolean}
     * @return {@link RoleCollectionResponse}
     */
    @Operation(description = "Busca los roles en base al estado de actividad")
    @ApiResponses(value = {
            @ApiResponse(responseCode = MessageUtil.OK_VALUE, description = "Operación list realizada")
    })
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE, path = "/list/{includeInactive}")
    public ResponseEntity<List<Role>> list(@Parameter(description = "Incluye/excluye la obtención de roles inactivos")
                                           @PathVariable boolean includeInactive) {
        log.info("{} /list/{includeInactive}", MessageUtil.LOG_START_MSG);
        return roleService.list(includeInactive, null);
    }

    /**
     * Obtiene una lista de rol en base al id de un usuario determinado.
     * @param userId {@link int}
     * @return Objeto de tipo {@link RoleCollectionResponse}
     */
    @Operation(description = "Busca los roles en base al id de un usuario")
    @ApiResponses(value = {
            @ApiResponse(responseCode = MessageUtil.OK_VALUE, description = "Operación list realizada")
    })
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE, path = "/listByUser/{userId}")
    public ResponseEntity<List<Role>> list(@PathVariable long userId) {
        log.info("/listByUser/{userId}");
        return roleService.list(userId);
    }

//    private RoleCollectionResponse getRoleCollectioResponse(MessageActivity<List<Role>> messageActivity){
//        log.info("Inicia la conversion de MessageActivity a RoleCollectionResponse");
//        final var roleCollectionResponse = new RoleCollectionResponse();
//        roleCollectionResponse.setCode(messageActivity.getCode());
//        roleCollectionResponse.setMessage(messageActivity.getMessage());
//        roleCollectionResponse.setRoles(messageActivity.getObjectResponse());
//        roleCollectionResponse.setDateTime(LocalDateTime.now(ZoneId.systemDefault()));
//        log.info("Termina la conversion de MessageActivity a RoleCollectionResponse");
//        return roleCollectionResponse;
//    }

}

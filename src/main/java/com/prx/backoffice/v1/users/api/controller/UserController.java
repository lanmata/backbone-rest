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
package com.prx.backoffice.v1.users.api.controller;

import com.prx.backoffice.util.MessageUtil;
import com.prx.backoffice.v1.session.to.UserAliasTO;
import com.prx.backoffice.v1.users.api.to.UserTO;
import com.prx.backoffice.v1.users.service.UserService;
import com.prx.commons.util.ValidatorCommonsUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/users")
@CrossOrigin(origins = "*")
public class UserController {
    private static final String STR_ID_USER = "User Id";
    private static final Logger LOGGER = LoggerFactory.getLogger(UserController.class);

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping()
    public ResponseEntity<String> checkAliasAvailable(
            @Parameter(description = "User alias.", required = true) @Valid @RequestParam(value = "alias") String alias) {
        return userService.validateAlias(alias);
    }

    @Operation(description = "Busca los usuarios a través del identificador")
    @ApiResponses(value = {
            @ApiResponse(responseCode = MessageUtil.OK, description = "User found.")
    })
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE, path = "/{userId}")
    public ResponseEntity<UserTO> find(@Parameter(description = STR_ID_USER, required = true) @PathVariable(value = "userId") @NotNull String userId) {
        return userService.findUserById(userId);
    }

    @Operation(description = "Getting an user list")
    @ApiResponses(value = {
            @ApiResponse(responseCode = MessageUtil.OK, description = "User Found")
    })
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE, path = "/")
    public ResponseEntity<List<UserTO>> findAll() {
        return userService.findAll();
    }

    @Operation(description = "Crea un nuevo usuario")
    @ApiResponses(value = {
            @ApiResponse(responseCode = MessageUtil.OK, description = "Usuario creado con éxito.")
    })
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE, path = "/")
    public ResponseEntity<UserTO> create(@Parameter(description = "Objeto de tipo UserCreateRequest", required = true)
                                         @RequestBody UserTO userTO) {
        LOGGER.info("{} /create", MessageUtil.LOG_START_MSG);
        if (ValidatorCommonsUtil.esNulo(userTO)) {
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).build();
        } else if (ValidatorCommonsUtil.esNulo(userTO.getAlias())
                || ValidatorCommonsUtil.esNulo(userTO.getPassword())) {
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).build();
        }
        return userService.create(userTO);
    }

    @Operation(description = "Update a user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = MessageUtil.OK, description = "Updated user"),
            @ApiResponse(responseCode = MessageUtil.BAD_REQUEST, description = "User ID empty or null"),
            @ApiResponse(responseCode = MessageUtil.BAD_REQUEST, description = "The user requested doesn't have a person associated."),
            @ApiResponse(responseCode = MessageUtil.BAD_REQUEST, description = "Invalid user")
    })
    @PutMapping(produces = MediaType.APPLICATION_JSON_VALUE, path = "/{userId}")
    public ResponseEntity<UserTO> update(@PathVariable @NotNull String userId, @RequestBody @NotNull UserTO user) {
        LOGGER.info("{} /update/{userId}", MessageUtil.LOG_START_MSG);
        return userService.update(userId, user);
    }

    @Operation(description = "Busca un usuario por un alias")
    @ApiResponses(value = {
            @ApiResponse(responseCode = MessageUtil.OK, description = "Usuario encontrado.")
    })
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE, path = "/findByAlias/{alias}")
    public ResponseEntity<UserTO> findByAlias(@Parameter(description = "Alias de usuario", required = true)
                                              @PathVariable @NotNull String alias) {
        return userService.findUserByAlias(alias);
    }

    @Operation(description = "Busca un usuario por un alias")
    @ApiResponses(value = {
            @ApiResponse(responseCode = MessageUtil.OK, description = "Usuario encontrado.")
    })
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE, path = "/findAliasByAlias/{alias}")
    public ResponseEntity<UserAliasTO> findAliasByAlias(@Parameter(description = "Alias de usuario", required = true)
                                              @PathVariable @NotNull String alias) {
        return userService.findUserAliasByAlias(alias);
    }

    @Operation(description = "Desvincula un rol de usuario")
    @PutMapping(produces = MediaType.APPLICATION_JSON_VALUE, path = "/unlink/{userId}/{roleId}")
    public ResponseEntity<UserTO> unlink(@Parameter(description = "Id de usuario") @PathVariable @NotNull String userId,
                                         @Parameter(description = "Id de rol") @PathVariable @NotNull String roleId) {
        return userService.unlink(userId, roleId);
    }

    @Operation(description = "Vincula un rol de usuario")
    @PutMapping(produces = MediaType.APPLICATION_JSON_VALUE, path = "/link/{userId}/{roleId}")
    public ResponseEntity<UserTO> link(@Parameter(description = STR_ID_USER) @PathVariable @NotNull String userId,
                                       @Parameter(description = "Id de rol") @PathVariable @NotNull String roleId) {
        return userService.link(userId, roleId);
    }

}

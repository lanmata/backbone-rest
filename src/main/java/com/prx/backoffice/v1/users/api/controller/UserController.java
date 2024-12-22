/*
 *  @(#)UserController.java
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
package com.prx.backoffice.v1.users.api.controller;

import com.prx.backoffice.util.MessageUtil;
import com.prx.backoffice.v1.session.to.UserAliasTO;
import com.prx.backoffice.v1.users.api.to.UserCreateRequest;
import com.prx.backoffice.v1.users.api.to.UserCreateResponse;
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

/// REST controller for managing users.
/// Provides endpoints for user operations such as create, update, and find.
@RestController
@RequestMapping("/v1/users")
@CrossOrigin(origins = "*")
public class UserController {
    private static final String STR_ID_USER = "User Id";
    private static final Logger LOGGER = LoggerFactory.getLogger(UserController.class);

    private final UserService userService;

    /// Constructor for UserController.
    ///
    /// @param userService the user service
    public UserController(UserService userService) {
        this.userService = userService;
    }

    /// Checks if a user alias is available.
    ///
    /// @param alias the user alias
    /// @return the response entity with the validation result
    @GetMapping()
    public ResponseEntity<String> checkAliasAvailable(
            @Parameter(description = "User alias.", required = true) @Valid @RequestParam(value = "alias") String alias) {
        return userService.validateAlias(alias);
    }

    /// Finds a user by ID.
    ///
    /// @param userId the user ID
    /// @return the response entity containing the user
    @Operation(description = "Busca los usuarios a través del identificador")
    @ApiResponses(value = {
            @ApiResponse(responseCode = MessageUtil.OK, description = "User found.")
    })
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE, path = "/{userId}")
    public ResponseEntity<UserTO> find(@Parameter(description = STR_ID_USER, required = true) @PathVariable(value = "userId") @NotNull String userId) {
        return userService.findUserById(userId);
    }

    /// Gets a list of all users.
    ///
    /// @return the response entity containing the list of users
    @Operation(description = "Getting an user list")
    @ApiResponses(value = {
            @ApiResponse(responseCode = MessageUtil.OK, description = "User Found")
    })
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE, path = "/")
    public ResponseEntity<List<UserTO>> findAll() {
        return userService.findAll();
    }

    /// Creates a new user.
    ///
    /// @param userCreateRequest the user creation request
    /// @return the response entity containing the created user
    @Operation(description = "Crea un nuevo usuario")
    @ApiResponses(value = {
            @ApiResponse(responseCode = MessageUtil.OK, description = "Usuario creado con éxito.")
    })
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserCreateResponse> create(@Parameter(description = "Objeto de tipo UserCreateRequest", required = true)
                                         @RequestBody UserCreateRequest userCreateRequest) {
        LOGGER.info("{} /create", MessageUtil.LOG_START_MSG);
        if (ValidatorCommonsUtil.esNulo(userCreateRequest)) {
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).build();
        } else if (ValidatorCommonsUtil.esNulo(userCreateRequest.alias())
                || ValidatorCommonsUtil.esNulo(userCreateRequest.password())) {
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).build();
        }
        return userService.create(userCreateRequest);
    }

    /// Updates an existing user.
    ///
    /// @param userId the user ID
    /// @param user the user details to update
    /// @return the response entity containing the updated user
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

    /// Finds a user by alias.
    ///
    /// @param alias the user alias
    /// @return the response entity containing the user
    @Operation(description = "Busca un usuario por un alias")
    @ApiResponses(value = {
            @ApiResponse(responseCode = MessageUtil.OK, description = "Usuario encontrado.")
    })
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE, path = "/findByAlias/{alias}")
    public ResponseEntity<UserTO> findByAlias(@Parameter(description = "Alias de usuario", required = true)
                                              @PathVariable @NotNull String alias) {
        return userService.findUserByAlias(alias);
    }

    /// Finds a user alias by alias.
    ///
    /// @param alias the user alias
    /// @return the response entity containing the user alias
    @Operation(description = "Busca un usuario por un alias")
    @ApiResponses(value = {
            @ApiResponse(responseCode = MessageUtil.OK, description = "Usuario encontrado.")
    })
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE, path = "/findAliasByAlias/{alias}")
    public ResponseEntity<UserAliasTO> findAliasByAlias(@Parameter(description = "Alias de usuario", required = true)
                                              @PathVariable @NotNull String alias) {
        return userService.findUserAliasByAlias(alias);
    }

    /// Unlinks a role from a user.
    ///
    /// @param userId the user ID
    /// @param roleId the role ID
    /// @return the response entity containing the updated user
    @Operation(description = "Desvincula un rol de usuario")
    @PutMapping(produces = MediaType.APPLICATION_JSON_VALUE, path = "/unlink/{userId}/{roleId}")
    public ResponseEntity<UserTO> unlink(@Parameter(description = "Id de usuario") @PathVariable @NotNull String userId,
                                         @Parameter(description = "Id de rol") @PathVariable @NotNull String roleId) {
        return userService.unlink(userId, roleId);
    }

    /// Links a role to a user.
    ///
    /// @param userId the user ID
    /// @param roleId the role ID
    /// @return the response entity containing the updated user
    @Operation(description = "Vincula un rol de usuario")
    @PutMapping(produces = MediaType.APPLICATION_JSON_VALUE, path = "/link/{userId}/{roleId}")
    public ResponseEntity<UserTO> link(@Parameter(description = STR_ID_USER) @PathVariable @NotNull String userId,
                                       @Parameter(description = "Id de rol") @PathVariable @NotNull String roleId) {
        return userService.roleLink(userId, roleId);
    }

}

/*
 *  @(#)UserApi.java
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
import com.prx.backoffice.v1.users.api.to.PutUserUpdateRequest;
import com.prx.backoffice.v1.users.api.to.UserCreateRequest;
import com.prx.backoffice.v1.users.api.to.UserCreateResponse;
import com.prx.backoffice.v1.users.api.to.UserTO;
import com.prx.backoffice.v1.users.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "users", description = "The user API")
public interface UserApi {
    String STR_ID_USER = "User ID";

    default UserService getService() {
        return new UserService() {
        };
    }

    /// Checks if a user alias is available.
    ///
    /// @param alias the user alias
    /// @return the response entity with the validation result
    @Operation(description = "Checks if a user alias is available.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = MessageUtil.OK, description = "Alias is available"),
            @ApiResponse(responseCode = "404", description = "Alias not found")
    })
    @GetMapping(path = "/check/alias/{alias}/application/{applicationId}")
    default ResponseEntity<Void> checkAliasAvailable(@NotBlank @PathVariable String alias, @NotNull @PathVariable UUID applicationId) {
        return getService().validateAlias(alias, applicationId);
    }

    /// Checks if a user email is available.
    ///
    /// @param email the user email
    /// @return the response entity with the validation result

    @Operation(description = "Checks if a user email is available.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = MessageUtil.OK, description = "Email is available"),
            @ApiResponse(responseCode = "404", description = "Email not found")
    })
    @GetMapping(path = "/check/email/{email}/application/{applicationId}")
    default ResponseEntity<Void> checkEmailAvailable(@PathVariable @Email String email, @NotNull @PathVariable UUID applicationId) {
        return getService().validateEmail(email, applicationId);
    }

    /// Creates a new user.
    ///
    /// @param userCreateRequest the user create request
    /// @return the response entity with the user create response
    @Operation(description = "Create a new user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = MessageUtil.CREATED, description = "User created.")
    })
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    default ResponseEntity<UserCreateResponse> create(@Parameter(description = "UserCreateRequest object type", required = true)
                                                      @RequestBody @Valid UserCreateRequest userCreateRequest) {
        return getService().create(userCreateRequest);
    }

    /// Finds a user by its identifier.
    ///
    /// @param userId the user identifier
    /// @return the response entity with the user
    @Operation(description = "Find a user by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = MessageUtil.OK, description = "User found.")
    })
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE, path = "/user/{userId}")
    default ResponseEntity<UserTO> findUserById(@NotNull @Parameter(description = STR_ID_USER) @PathVariable UUID userId) {
        return getService().findUserById(userId);
    }

    /// Gets a list of all users.
    ///
    /// @return the response entity containing the list of users
    @Operation(description = "Getting an user list")
    @ApiResponses(value = {
            @ApiResponse(responseCode = MessageUtil.OK, description = "User Found")
    })
    @GetMapping(path = "/application/{applicationId}", produces = MediaType.APPLICATION_JSON_VALUE)
    default ResponseEntity<List<UserTO>> findAll(@PathVariable UUID applicationId) {
        return getService().findAll(applicationId);
    }

    /// Finds a user by its alias.
    ///
    /// @param alias the user alias
    /// @return the response entity with the user
    @Operation(description = "Find a user by alias")
    @ApiResponses(value = {
            @ApiResponse(responseCode = MessageUtil.OK, description = "User found")
    })
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE, path = "/userByAlias/{alias}/application/{applicationId}")
    default ResponseEntity<UserTO> findUserByAlias(@Parameter(description = "Alias user", required = true)
                                                   @PathVariable @NotBlank String alias, @PathVariable UUID applicationId) {
        return getService().findUserByAlias(alias, applicationId);
    }

    /// Finds a user alias by its alias.
    ///
    /// @param alias the user alias
    /// @return the response entity with the user alias
    @Operation(description = "Find a user alias by alias")
    @ApiResponses(value = {
            @ApiResponse(responseCode = MessageUtil.OK, description = "User alias found")
    })
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE, path = "/alias/{alias}/application/{applicationId}")
    default ResponseEntity<UserAliasTO> findUserAliasByAlias(@Parameter(description = "User alias", required = true)
                                                             @PathVariable @NotBlank String alias, @PathVariable UUID applicationId) {
        return getService().findUserAliasByAlias(alias, applicationId);
    }

    /// Updates a user.
    ///
    /// @param userId the user ID
    /// @param user   the user to update
    /// @return the response entity with the user
    @Operation(description = "Update a user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = MessageUtil.OK, description = "Updated user"),
            @ApiResponse(responseCode = "400", description = "User ID empty or null"),
            @ApiResponse(responseCode = "400", description = "The user requested doesn't have a person associated."),
            @ApiResponse(responseCode = "400", description = "Invalid user")
    })
    @PutMapping(produces = MediaType.APPLICATION_JSON_VALUE, path = "/{userId}/full-detail")
    default ResponseEntity<UserTO> update(@Parameter(description = STR_ID_USER) @PathVariable(name = "userId") UUID userId,
                                          @Parameter(description = "UserTO content") @RequestBody @Valid @NotNull UserTO user) {
        return getService().update(userId, user);
    }

    /// Unlinks a role from a user.
    ///
    /// @param userId the user ID
    /// @param roleId the role ID
    /// @return the response entity containing the updated user
    @Operation(description = "Unlinks a role from a user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = MessageUtil.OK, description = "Role unlinked from user")
    })
    @PutMapping(produces = MediaType.APPLICATION_JSON_VALUE, path = "/unlink/user/{userId}/role/{roleId}")
    default ResponseEntity<UserTO> unlink(@Parameter(description = STR_ID_USER) @PathVariable @NotBlank UUID userId,
                                          @Parameter(description = "Role ID") @PathVariable @NotBlank UUID roleId) {
        return getService().unlink(userId, roleId);
    }

    /// Links a role to a user.
    ///
    /// @param userId the user ID
    /// @param roleId the role ID
    /// @return the response entity containing the updated user
    @Operation(description = "Links a role to a user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = MessageUtil.OK, description = "Role linked to user")
    })
    @PutMapping(produces = MediaType.APPLICATION_JSON_VALUE, path = "/link/user/{userId}/role/{roleId}")
    default ResponseEntity<UserTO> link(@Parameter(description = STR_ID_USER) @PathVariable @NotBlank UUID userId,
                                        @Parameter(description = "Role ID") @PathVariable @NotBlank UUID roleId) {
        return getService().roleLink(userId, roleId);
    }

    /// Updates a user using PutUserUpdateRequest.
    ///
    /// @param userId the user ID
    /// @param request the PutUserUpdateRequest body
    /// @return the response entity with the update status
    @Operation(description = "putUserDetail(partial update) a user with PutUserUpdateRequest")
    @ApiResponses(value = {
            @ApiResponse(responseCode = MessageUtil.ACCEPTED, description = "User updated and accepted"),
            @ApiResponse(responseCode = MessageUtil.NOT_ACCEPTABLE, description = "User update rejected")
    })
    @PutMapping(consumes = MediaType.APPLICATION_JSON_VALUE, path = "/{userId}")
    default ResponseEntity<Void> putUserDetail(@Parameter(description = STR_ID_USER) @PathVariable @NotNull UUID userId,
                                               @Parameter(description = "PutUserUpdateRequest content") @Valid @NotNull @RequestBody PutUserUpdateRequest request) {
        // Call controller's conversion and update logic
        return ((UserController)this).putUserDetail(userId, request);
    }
}

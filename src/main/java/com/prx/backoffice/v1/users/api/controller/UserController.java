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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/// REST controller for managing users.
/// Provides endpoints for user operations such as create, update, and find.
@RestController
@RequestMapping("/api/v1/users")
@CrossOrigin(origins = "*")
public class UserController implements UserApi {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserController.class);

    private final UserService userService;

    /// Constructor for UserController.
    ///
    /// @param userService the user service
    public UserController(UserService userService) {
        this.userService = userService;
    }

    /// {@inheritDoc}
    @Override
    public ResponseEntity<Void> checkAliasAvailable(String alias, UUID applicationId) {
        return userService.validateAlias(alias, applicationId);
    }

    /// {@inheritDoc}
    @Override
    public ResponseEntity<Void> checkEmailAvailable(String email, UUID applicationId) {
        return userService.validateEmail(email, applicationId);
    }

    /// {@inheritDoc}
    @Override
    public ResponseEntity<UserTO> findUserById(UUID userId) {
        return userService.findUserById(userId);
    }

    /// {@inheritDoc}
    @Override
    public ResponseEntity<List<UserTO>> findAll(UUID applicationId) {
        return userService.findAll(applicationId);
    }

    /// {@inheritDoc}
    @Override
    public ResponseEntity<UserCreateResponse> create(UserCreateRequest userCreateRequest) {
        LOGGER.info("{} /create", MessageUtil.LOG_START_MSG);
        if (ValidatorCommonsUtil.esNulo(userCreateRequest)) {
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).build();
        } else if (ValidatorCommonsUtil.esNulo(userCreateRequest.alias())
                || ValidatorCommonsUtil.esNulo(userCreateRequest.password())) {
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).build();
        }
        return userService.create(userCreateRequest);
    }

    /// {@inheritDoc}
    @Override
    public ResponseEntity<UserTO> update(UUID userId, UserTO user) {
        LOGGER.info("{} /update/{userId}", MessageUtil.LOG_START_MSG);
        return userService.update(userId, user);
    }

    /// {@inheritDoc}
    @Override
    public ResponseEntity<UserTO> findUserByAlias(String alias, UUID applicationId) {
        return userService.findUserByAlias(alias, applicationId);
    }

    /// {@inheritDoc}
    @Override
    public ResponseEntity<UserAliasTO> findUserAliasByAlias(String alias, UUID applicationId) {
        return userService.findUserAliasByAlias(alias, applicationId);
    }

    /// {@inheritDoc}
    @Override
    public ResponseEntity<UserTO> unlink(UUID userId, UUID roleId) {
        return userService.unlink(userId, roleId);
    }

    ///  {@inheritDoc}
    @Override
    public ResponseEntity<UserTO> link(UUID userId, UUID roleId) {
        return userService.roleLink(userId, roleId);
    }

}

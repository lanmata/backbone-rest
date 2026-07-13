/*
 *  @(#)UserServiceTest.java
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

package com.umdc.backoffice.v1.users.service;

import com.umdc.backoffice.v1.users.api.to.UserTO;
import org.apache.commons.lang3.NotImplementedException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UserServiceTest {

    private final UserService userService = new UserService() {
    };

    @Test
    @DisplayName("Test finding user by ID")
    void findUserById() {
        UUID userId = UUID.randomUUID();
        assertThrows(NotImplementedException.class, () -> userService.findUserById(userId));
    }

    @Test
    @DisplayName("Test finding user by alias")
    void findUserByAlias() {
        UUID appId = UUID.randomUUID();
        assertThrows(NotImplementedException.class, () -> userService.findUserByAlias("pepe", appId));
    }

    @Test
    @DisplayName("Test accessing user")
    void access() {
        assertThrows(NotImplementedException.class, () -> userService.access("pepe", "abc123"));
    }

    @Test
    @DisplayName("Test finding all users")
    void findAll() {
        UUID appId = UUID.randomUUID();
        assertThrows(NotImplementedException.class, () -> userService.findAll(appId));
    }

    @Test
    @DisplayName("Test creating a user")
    void create() {
        var responseEntity = userService.create(new UserTO());
        assertEquals(HttpStatus.NOT_IMPLEMENTED, responseEntity.getStatusCode());
    }

    @Test
    @DisplayName("Test unlinking a user")
    void unlink() {
        UUID userId = UUID.randomUUID();
        UUID appId = UUID.randomUUID();
        assertThrows(NotImplementedException.class, () -> userService.unlink(userId, appId));
    }

    @Test
    @DisplayName("Test linking a user")
    void roleLink() {
        UUID userId = UUID.randomUUID();
        UUID roleId = UUID.randomUUID();
        assertThrows(NotImplementedException.class, () -> userService.roleLink(userId, roleId));
    }

    @ParameterizedTest(name = "validateAlias alias={0}")
    @NullSource
    @ValueSource(strings = {"pepe", "availableAlias", "unavailableAlias"})
    @DisplayName("Test alias validation with various alias values")
    void validateAlias(String alias) {
        UUID appId = UUID.randomUUID();
        assertThrows(NotImplementedException.class, () -> userService.validateAlias(alias, appId));
    }

    @Test
    @DisplayName("Validate alias with null applicationId")
    void validateAliasNullApplicationId() {
        assertThrows(NotImplementedException.class, () -> userService.validateAlias("alias", null));
    }

    @ParameterizedTest(name = "validateEmail email={0}")
    @NullSource
    @ValueSource(strings = {"available@example.com", "unavailable@example.com"})
    @DisplayName("Test email validation with various email values")
    void validateEmail(String email) {
        UUID appId = UUID.randomUUID();
        assertThrows(NotImplementedException.class, () -> userService.validateEmail(email, appId));
    }

    @Test
    @DisplayName("Validate email with null applicationId")
    void validateEmailNullApplicationId() {
        assertThrows(NotImplementedException.class, () -> userService.validateEmail("email@example.com", null));
    }
}

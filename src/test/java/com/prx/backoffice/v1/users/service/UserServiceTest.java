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

package com.prx.backoffice.v1.users.service;

import com.prx.backoffice.v1.users.api.to.UserTO;
import org.apache.commons.lang.NotImplementedException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UserServiceTest {

    private final UserService userService = new UserService() {
    };

    @Test
    @DisplayName("Test finding user by ID")
    void findUserById() {
        assertThrows(NotImplementedException.class, () -> userService.findUserById(UUID.randomUUID()));
    }

    @Test
    @DisplayName("Test finding user by alias")
    void findUserByAlias() {
        assertThrows(NotImplementedException.class, () -> userService.findUserByAlias("pepe", UUID.randomUUID()));
    }

    @Test
    @DisplayName("Test accessing user")
    void access() {
        assertThrows(NotImplementedException.class, () -> userService.access("pepe", "abc123"));
    }

    @Test
    @DisplayName("Test finding all users")
    void findAll() {
        assertThrows(NotImplementedException.class, () -> userService.findAll(UUID.randomUUID()));
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
        assertThrows(NotImplementedException.class, () -> userService.unlink(UUID.randomUUID(), UUID.randomUUID()));
    }

    @Test
    @DisplayName("Test linking a user")
    void roleLink() {
        assertThrows(NotImplementedException.class, () -> userService.roleLink(UUID.randomUUID(), UUID.randomUUID()));
    }

    @Test
    @DisplayName("Test alias validation")
    void validateAlias() {
        assertThrows(NotImplementedException.class, () -> userService.validateAlias("pepe", UUID.randomUUID()));
    }

    @Test
    @DisplayName("Validate alias is available")
    void validateAliasAvailable() {
        assertThrows(NotImplementedException.class, () -> userService.validateAlias("availableAlias", UUID.randomUUID()));
    }

    @Test
    @DisplayName("Validate alias is not available")
    void validateAliasNotAvailable() {
        assertThrows(NotImplementedException.class, () -> userService.validateAlias("unavailableAlias", UUID.randomUUID()));
    }

    @Test
    @DisplayName("Validate alias with null alias")
    void validateAliasNullAlias() {
        assertThrows(NotImplementedException.class, () -> userService.validateAlias(null, UUID.randomUUID()));
    }

    @Test
    @DisplayName("Validate alias with null applicationId")
    void validateAliasNullApplicationId() {
        assertThrows(NotImplementedException.class, () -> userService.validateAlias("alias", null));
    }

    @Test
    @DisplayName("Validate email is available")
    void validateEmailAvailable() {
        assertThrows(NotImplementedException.class, () -> userService.validateEmail("available@example.com", UUID.randomUUID()));
    }

    @Test
    @DisplayName("Validate email is not available")
    void validateEmailNotAvailable() {
        assertThrows(NotImplementedException.class, () -> userService.validateEmail("unavailable@example.com", UUID.randomUUID()));
    }

    @Test
    @DisplayName("Validate email with null email")
    void validateEmailNullEmail() {
        assertThrows(NotImplementedException.class, () -> userService.validateEmail(null, UUID.randomUUID()));
    }

    @Test
    @DisplayName("Validate email with null applicationId")
    void validateEmailNullApplicationId() {
        assertThrows(NotImplementedException.class, () -> userService.validateEmail("email@example.com", null));
    }
}

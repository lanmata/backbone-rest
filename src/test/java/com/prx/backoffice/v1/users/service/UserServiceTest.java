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

import static org.junit.jupiter.api.Assertions.assertThrows;

class UserServiceTest {

    private final UserService userService = new UserService() {
    };

    @Test
    @DisplayName("Test finding user by ID")
    void findUserById() {
        assertThrows(NotImplementedException.class, () -> userService.findUserById("abc123"));
    }

    @Test
    @DisplayName("Test finding user by alias")
    void findUserByAlias() {
        assertThrows(NotImplementedException.class, () -> userService.findUserByAlias("pepe"));
    }

    @Test
    @DisplayName("Test accessing user")
    void access() {
        assertThrows(NotImplementedException.class, () -> userService.access("pepe", "abc123"));
    }

    @Test
    @DisplayName("Test finding all users")
    void findAll() {
        assertThrows(NotImplementedException.class, userService::findAll);
    }

    @Test
    @DisplayName("Test creating a user")
    void create() {
        assertThrows(NotImplementedException.class, () -> userService.create(new UserTO()));
    }

    @Test
    @DisplayName("Test unlinking a user")
    void unlink() {
        assertThrows(NotImplementedException.class, () -> userService.unlink("abc123", "rol123"));
    }

    @Test
    @DisplayName("Test linking a user")
    void roleLink() {
        assertThrows(NotImplementedException.class, () -> userService.roleLink("abc123", "rol123"));
    }

    @Test
    @DisplayName("Test alias validation")
    void validateAlias() {
        assertThrows(NotImplementedException.class, () -> userService.validateAlias("pepe"));
    }
}

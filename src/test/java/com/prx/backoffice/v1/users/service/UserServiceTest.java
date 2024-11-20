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
    void link() {
        assertThrows(NotImplementedException.class, () -> userService.link("abc123", "rol123"));
    }

    @Test
    @DisplayName("Test alias validation")
    void validateAlias() {
        assertThrows(NotImplementedException.class, () -> userService.validateAlias("pepe"));
    }
}

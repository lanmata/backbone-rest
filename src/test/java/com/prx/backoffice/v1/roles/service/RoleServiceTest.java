/*
 *  @(#)RoleServiceTest.java
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

package com.prx.backoffice.v1.roles.service;

import com.prx.commons.general.pojo.Role;
import org.apache.commons.lang.NotImplementedException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class RoleServiceTest {

    private final RoleService roleService = new RoleService() {
    };

    @Test
    @DisplayName("Test finding a role")
    void find() {
        assertThrows(NotImplementedException.class, () -> roleService.find(UUID.randomUUID()));
    }

    @Test
    @DisplayName("Test creating a role")
    void create() {
        assertThrows(NotImplementedException.class, () -> roleService.create(new Role()));
    }

    @Test
    @DisplayName("Test updating a role")
    void update() {
        assertThrows(NotImplementedException.class, () -> roleService.update(UUID.randomUUID(), new Role()));
    }

    @Test
    @DisplayName("Test listing roles by user")
    void listByUser() {
        assertThrows(NotImplementedException.class, () -> roleService.listByUser(UUID.randomUUID()));
    }

    @Test
    @DisplayName("Test listing roles")
    void list() {
        assertThrows(NotImplementedException.class, roleService::list);
    }

    @Test
    @DisplayName("Test listing roles by user (duplicate)")
    void testList() {
        assertThrows(NotImplementedException.class, () -> roleService.listByUser(UUID.randomUUID()));
    }

    @Test
    @DisplayName("Test deleting a role")
    void delete() {
        assertThrows(NotImplementedException.class, () -> roleService.delete(UUID.randomUUID(), new Role()));
    }
}

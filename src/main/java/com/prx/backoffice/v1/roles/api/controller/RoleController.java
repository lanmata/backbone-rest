/*
 *  @(#)RoleController.java
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
package com.prx.backoffice.v1.roles.api.controller;

import com.prx.backoffice.v1.roles.api.to.RoleRequest;
import com.prx.backoffice.v1.roles.service.RoleService;
import com.prx.commons.general.pojo.Role;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * RolController.
 *
 * @author <a href='mailto:luis.antonio.mata@gmail.com'>Luis Antonio Mata</a>
 * @version 1.0.0, 12-02-2021
 */
@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/v1/roles")
class RoleController implements RoleApi {

    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @Override
    public RoleService getService() {
        return this.roleService;
    }

    @Override
    public ResponseEntity<Role> find(UUID roleId) {
        return roleService.find(roleId);
    }

    @Override
    public ResponseEntity<List<Role>> list(boolean includeInactive, List<UUID> roleIds) {
        return roleService.list(includeInactive, roleIds);
    }

    @Override
    public ResponseEntity<List<Role>> list(boolean includeInactive) {
        return roleService.list(includeInactive, null);
    }

    @Override
    public ResponseEntity<List<Role>> list() {
        return roleService.list();
    }

    @Override
    public ResponseEntity<Role> create(RoleRequest roleCreateRequest) {
        return roleService.create(roleCreateRequest.getRole());
    }

    @Override
    public ResponseEntity<Role> update(UUID roleId, RoleRequest roleRequest) {
        return roleService.update(roleId, roleRequest.getRole());
    }

    @Override
    public ResponseEntity<List<Role>> listByUser(UUID userId) {
        return roleService.listByUser(userId);
    }
}

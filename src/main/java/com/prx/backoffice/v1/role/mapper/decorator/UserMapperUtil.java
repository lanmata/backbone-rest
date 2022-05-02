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

package com.prx.backoffice.v1.role.mapper.decorator;

import com.prx.backoffice.v1.role.mapper.RoleMapper;
import com.prx.commons.pojo.Role;
import com.prx.persistence.general.domains.UserRoleEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * UserMapperUtil.
 *
 * @author Luis Antonio Mata
 * @version 1.0.0, 01-05-2022
 * @since 11
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserMapperUtil {
    private final RoleMapper roleMapper;

    public List<Role> toRole(Set<UserRoleEntity> userRoleEntities){
        final var roles = new ArrayList<Role>();
        userRoleEntities.forEach(userRoleEntity -> {
            var role = new Role();
            role.setId(userRoleEntity.getRole().getId());
            roles.add(role);
        });
        return roles;
    }

}

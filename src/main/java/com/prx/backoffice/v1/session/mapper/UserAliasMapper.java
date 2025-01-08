/*
 *  @(#)UserAliasMapper.java
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

package com.prx.backoffice.v1.session.mapper;

import com.prx.backoffice.v1.people.mapper.PersonMapper;
import com.prx.backoffice.v1.roles.mapper.RoleMapper;
import com.prx.backoffice.v1.session.to.UserAliasTO;
import com.prx.backoffice.v1.users.api.to.UserTO;
import com.prx.backoffice.v1.users.mapper.UserMapper;
import com.prx.commons.general.pojo.Role;
import com.prx.commons.services.config.mapper.MapperAppConfig;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Mapper(config = MapperAppConfig.class,
        uses = {UserMapper.class, PersonMapper.class, RoleMapper.class}
)
public interface UserAliasMapper {

    @Mapping(target = "userId", source = "id")
    @Mapping(target = "alias", source = "alias")
    @Mapping(target = "firstname", source = "person.firstName")
    @Mapping(target = "lastname", source = "person.lastName")
    @Mapping(target = "roles", source = "roles")
    UserAliasTO toTarget(UserTO userTO);

    default Set<UUID> map(Set<Role> roles) {
        if (roles == null) {
            return null;
        }
        return roles.stream()
                .filter(Objects::nonNull)
                .map(Role::getId)
                .collect(Collectors.toSet());
    }

    @AfterMapping
    default void map(Role role, @MappingTarget Set<UUID> uuid) {
        if(Objects.nonNull(role)) {
            uuid.add(role.getId());
        }
    }
}

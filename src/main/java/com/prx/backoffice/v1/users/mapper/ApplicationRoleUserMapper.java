/*
 *  @(#)ApplicationRoleUserMapper.java
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

package com.prx.backoffice.v1.users.mapper;

import com.prx.backoffice.config.jackson.MapperAppConfig;
import com.prx.backoffice.v1.features.mapper.FeatureMapper;
import com.prx.commons.pojo.Application;
import com.prx.commons.pojo.Role;
import com.prx.commons.pojo.User;
import com.prx.persistence.general.domains.ApplicationRoleUserEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/// Mapper interface for converting between ApplicationRoleUserEntity and various target objects.
/// Utilizes MapStruct for automatic mapping.
@Mapper(
        // Specifies the configuration class to use for this mapper.
        config = MapperAppConfig.class,
        uses = {UserMapper.class, FeatureMapper.class}
)
public interface ApplicationRoleUserMapper {

    /// Maps an ApplicationRoleUserEntity to a Role object.
    ///
    /// @param userRoleEntity the entity to map from
    /// @return the mapped Role object
    @Mapping(target="id", source = "role.id")
    @Mapping(target="name", source = "role.name")
    @Mapping(target="description", source = "role.description")
    @Mapping(target="active", source = "role.active")
    Role toRoleTarget(ApplicationRoleUserEntity userRoleEntity);

    /// Maps an ApplicationRoleUserEntity to a User object.
    ///
    /// @param userRoleEntity the entity to map from
    /// @return the mapped User object
    @Mapping(target="id", source = "user.id")
    @Mapping(target="alias", source = "user.alias")
    @Mapping(target="password", source = "user.password")
    @Mapping(target="active", source = "user.active")
    User toUserTarget(ApplicationRoleUserEntity userRoleEntity);

    /// Maps an ApplicationRoleUserEntity to an Application object.
    ///
    /// @param applicationRoleUserEntity the entity to map from
    /// @return the mapped Application object
    @Mapping(target="id", source = "application.id")
    @Mapping(target="name", source = "application.name")
    @Mapping(target="description", source = "application.description")
    @Mapping(target="active", source = "application.active")
    @Mapping(target="serviceTypeId", source = "application.serviceTypeId")
    Application toApplicationTarget(ApplicationRoleUserEntity applicationRoleUserEntity);
}

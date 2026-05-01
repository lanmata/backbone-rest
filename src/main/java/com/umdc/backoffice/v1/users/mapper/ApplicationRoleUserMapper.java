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

package com.umdc.backoffice.v1.users.mapper;

import com.umdc.backoffice.v1.features.mapper.FeatureMapper;
import com.umdc.backoffice.v1.users.api.to.UserTO;
import com.umdc.commons.constants.httpstatus.type.MessageType;
import com.umdc.commons.exception.StandardException;
import com.umdc.commons.general.pojo.Application;
import com.umdc.commons.general.pojo.Role;
import com.umdc.commons.general.pojo.User;
import com.umdc.commons.services.config.mapper.MapperAppConfig;
import com.umdc.persistence.general.domains.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/// Mapper interface for converting between ApplicationRoleUserEntity and various target objects.
/// Uses MapStruct for automatic mapping.
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

    /// Maps a UserTO to a list of ApplicationRoleUserEntity objects.
    ///
    /// @param userTO the UserTO to map from
    /// @return the set of ApplicationRoleUserEntity objects
    static Set<ApplicationRoleUserEntity> getApplicationRoleUser(UserTO userTO) {
        if (userTO == null || userTO.getRoles() == null) {
            return java.util.Collections.emptySet();
        }
        Set<ApplicationRoleUserEntity> entities = new HashSet<>();
        var applicationId = userTO.getApplications().stream().findFirst().orElseThrow(() -> new StandardException(MessageType.DEFAULT_MESSAGE)).getId();
        //PENDING - I have to get only the application and roles linked for the current user
        for (Role role : userTO.getRoles()) {
            final var entity = getApplicationRoleUserEntity(userTO, role, applicationId);
            final var applicationRoleUserId = new ApplicationRoleUserEntityId();
            applicationRoleUserId.setUserId(applicationId);
            applicationRoleUserId.setApplicationId(userTO.getId());
            applicationRoleUserId.setRoleId(role.getId());
            entity.setId(applicationRoleUserId);
            entities.add(entity);
        }
        return entities;
    }

    private static ApplicationRoleUserEntity getApplicationRoleUserEntity(UserTO userTO, Role role, UUID applicationId) {
        ApplicationRoleUserEntity entity = new ApplicationRoleUserEntity();
        ApplicationEntity applicationEntity =  new ApplicationEntity();
        applicationEntity.setId(applicationId);
        entity.setApplication(applicationEntity);
        entity.setActive(role.getActive());
        // Set Role
        RoleEntity roleEntity = new RoleEntity();
        roleEntity.setId(role.getId());
        roleEntity.setName(role.getName());
        roleEntity.setDescription(role.getDescription());
        roleEntity.setActive(role.getActive());
        entity.setRole(roleEntity);
        // Set User
        UserEntity userEntity = new UserEntity();
        userEntity.setId(userTO.getId());
        entity.setUser(userEntity);

        return entity;
    }
}

/*
 *  @(#)UserMapper.java
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

import com.umdc.backoffice.v1.people.mapper.PersonMapper;
import com.umdc.backoffice.v1.roles.mapper.RoleMapper;
import com.umdc.backoffice.v1.users.api.to.UserCreateRequest;
import com.umdc.backoffice.v1.users.api.to.UserCreateResponse;
import com.umdc.backoffice.v1.users.api.to.UserTO;
import com.umdc.commons.services.config.mapper.MapperAppConfig;
import com.umdc.persistence.general.domains.UserEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.UUID;

/// UserMapper interface for mapping between UserEntity and UserTO objects.
/// Utilizes MapStruct for automatic mapping.
/// Configured with MapperAppConfig and uses RoleMapper, PersonMapper, and UserRoleMapper.
///
/// @version 1.0.0, 20-10-2020
///
/// @author Luis Antonio Mata
@Mapper(
        // Specifies the configuration class to use for this mapper.
        config = MapperAppConfig.class,
        uses = {RoleMapper.class, PersonMapper.class, ApplicationRoleUserMapper.class}
)
public interface UserMapper {

    /// Maps a UserEntity object to a UserTO object.
    ///
    /// @param userEntity the UserEntity object to map from
    /// @return the mapped UserTO object
    @Mapping(target = "roles", source = "applicationRoleUser")
    @Mapping(target = "applications", source = "applicationRoleUser")
    @Mapping(target = "email", source = "email")
    @Mapping(target = "notificationEmail", source="notificationEmail")
    @Mapping(target = "notificationSms", source = "notificationSms")
    @Mapping(target = "privacyDataOutActive", source = "privacyDataOutActive")
    UserTO toTarget(UserEntity userEntity);

    /// Maps a UserTO object to a UserEntity object.
    /// Inherits the inverse configuration from the toTarget method.
    ///
    /// @param user the UserTO object to map from
    /// @return the mapped UserEntity object
    @Mapping(target = "applicationRoleUser", expression = "java(ApplicationRoleUserMapper.getApplicationRoleUser(user))")
    @Mapping(target = "id", source = "id")
    @Mapping(target = "alias", source = "alias")
    @Mapping(target = "displayName", source = "displayName")
    @Mapping(target = "notificationEmail", source= "notificationEmail")
    @Mapping(target = "notificationSms", source= "notificationSms")
    @Mapping(target = "privacyDataOutActive", source= "privacyDataOutActive")
    @Mapping(target = "person", source = "person")
    UserEntity toSource(UserTO user);


    UserEntity toSource(UserCreateRequest userCreateRequest);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "alias", source = "alias")
    @Mapping(target = "email", source = "email")
    @Mapping(target = "active", source = "active")
    @Mapping(target = "personId", source = "person.id")
    @Mapping(target = "applicationId", expression = "java(getApplicationId(userEntity))")
    @Mapping(target = "roleId", expression = "java(getRoleId(userEntity))")
    UserCreateResponse toUserCreateResponse(UserEntity userEntity);

    default UUID getRoleId(UserEntity userEntity) {
        return userEntity.getApplicationRoleUser().stream().map(applicationRoleUserEntity ->
                applicationRoleUserEntity.getRole().getId()).findFirst().orElse(null);

    }
    default UUID getApplicationId(UserEntity userEntity) {
        return userEntity.getApplicationRoleUser().stream().map(applicationRoleUserEntity ->
                applicationRoleUserEntity.getApplication().getId()).findFirst().orElse(null);
    }
}

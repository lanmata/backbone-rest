/*
 * @(#)UserMapper.java.
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
package com.prx.backoffice.v1.users.mapper;

import com.prx.backoffice.config.jackson.MapperAppConfig;
import com.prx.backoffice.v1.people.mapper.PersonMapper;
import com.prx.backoffice.v1.roles.mapper.RoleMapper;
import com.prx.backoffice.v1.users.api.to.UserTO;
import com.prx.persistence.general.domains.UserEntity;
import org.mapstruct.*;

/**
 * UserMapper interface for mapping between UserEntity and UserTO objects.
 * Utilizes MapStruct for automatic mapping.
 * Configured with MapperAppConfig and uses RoleMapper, PersonMapper, and UserRoleMapper.
 *
 * @version 1.0.0, 20-10-2020
 *
 * @author Luis Antonio Mata
 */
@Mapper(
        // Specifies the configuration class to use for this mapper.
        config = MapperAppConfig.class,
        uses = {RoleMapper.class, PersonMapper.class, UserRoleMapper.class}
)
public interface UserMapper {

    /**
     * Maps a UserEntity object to a UserTO object.
     *
     * @param userEntity the UserEntity object to map from
     * @return the mapped UserTO object
     */
    @Mapping(source = "userRole", target = "roles")
    UserTO toTarget(UserEntity userEntity);

    /**
     * Maps a UserTO object to a UserEntity object.
     * Inherits the inverse configuration from the toTarget method.
     *
     * @param user the UserTO object to map from
     * @return the mapped UserEntity object
     */
    @InheritInverseConfiguration
    UserEntity toSource(UserTO user);
}

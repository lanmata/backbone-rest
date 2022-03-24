
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

package com.prx.backoffice.v1.role.mapper;

import com.prx.backoffice.v1.feature.mapper.decorator.FeatureMapperUtil;
import com.prx.commons.pojo.Role;
import com.prx.persistence.general.domains.RoleEntity;
import com.prx.persistence.general.domains.UserRoleEntity;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * RolMapper.
 *
 * @author Luis Antonio Mata
 * @version 1.0.0, 12-02-2021
 */
@Mapper(componentModel = "spring", uses = {FeatureMapperUtil.class})
public interface RoleMapper {

    @Mapping(target = "features", source = "roleFeatures")
    Role toTarget(RoleEntity roleEntity);

    @InheritInverseConfiguration
    RoleEntity toSource(Role role);

    @Mapping(target = "id", source = "role.id")
    @Mapping(target = "name", source = "role.name")
    @Mapping(target = "description", source = "role.description")
    @Mapping(target = "active", source = "role.active")
    @Mapping(target = "features", source = "role.roleFeatures")
    Role userRoleToRole(UserRoleEntity userRoleEntity);
}

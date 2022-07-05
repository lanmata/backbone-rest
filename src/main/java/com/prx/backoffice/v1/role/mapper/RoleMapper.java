
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

import com.prx.commons.pojo.Feature;
import com.prx.commons.pojo.Role;
import com.prx.persistence.general.domains.RoleEntity;
import com.prx.persistence.general.domains.RoleFeatureEntity;
import org.mapstruct.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Objects;

/**
 * RolMapper.
 *
 * @author Luis Antonio Mata
 * @version 1.0.0, 12-02-2021
 */
@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
@MapperConfig(unmappedTargetPolicy = ReportingPolicy.ERROR, unmappedSourcePolicy = ReportingPolicy.ERROR)
public interface RoleMapper {

    @Mapping(target = "features", ignore = true)
    Role toTarget(RoleEntity roleEntity);

    @InheritInverseConfiguration
    RoleEntity toSource(Role role);

    @AfterMapping
    default void setRoleFeature(Role role, @MappingTarget RoleEntity roleEntity) {
        if(Objects.nonNull(role.getFeatures()) && !role.getFeatures().isEmpty()) {
            if(Objects.isNull(roleEntity.getRoleFeatures())) {
                roleEntity.setRoleFeatures(new HashSet<>());
            }
            role.getFeatures().forEach(feature -> {
                var roleFeature = new RoleFeatureEntity();
                roleFeature.setRole(role.getId());
                roleFeature.setFeature(feature.getId());
                roleEntity.getRoleFeatures().add(roleFeature);
            });
        }
    }

    @AfterMapping
    default void setFeature(RoleEntity roleEntity, @MappingTarget Role role) {
        if(Objects.nonNull(roleEntity.getRoleFeatures()) && !roleEntity.getRoleFeatures().isEmpty()) {
            if(Objects.isNull(role.getFeatures())) {
                role.setFeatures(new ArrayList<>());
            }
            roleEntity.getRoleFeatures().forEach(featureEntity -> {
                var feature = new Feature();
                feature.setId(featureEntity.getFeature());
                role.getFeatures().add(feature);
            });
        }
    }
}

/*
 *  @(#)RoleMapper.java
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

package com.umdc.backoffice.v1.roles.mapper;

import com.umdc.backoffice.v1.features.mapper.FeatureMapper;
import com.umdc.backoffice.v1.users.mapper.UserMapper;
import com.prx.commons.general.pojo.Feature;
import com.prx.commons.general.pojo.Role;
import com.prx.commons.services.config.mapper.MapperAppConfig;
import com.prx.persistence.general.domains.FeatureEntity;
import com.prx.persistence.general.domains.RoleEntity;
import com.prx.persistence.general.domains.RoleFeatureEntity;
import org.mapstruct.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Objects;

/**
 * RoleMapper interface for mapping between Role and RoleEntity objects.
 * Utilizes MapStruct for automatic mapping.
 * Configured with MapperAppConfig and uses UserMapper and FeatureMapper.
 *
 * @version 1.0.0, 12-02-2021
 *
 * @author Luis Antonio Mata
 */
@Mapper(
        // Specifies the configuration class to use for this mapper.
        config = MapperAppConfig.class,
        uses = {UserMapper.class, FeatureMapper.class}
)
@MapperConfig(
        // Specifies that the mapper should fail if there are any unmapped properties.
        unmappedSourcePolicy = ReportingPolicy.IGNORE,
        // Specifies that the mapper should fail if there are any unmapped properties.
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface RoleMapper {

    /**
     * Maps a RoleEntity object to a Role object.
     *
     * @param roleEntity the RoleEntity object to map from
     * @return the mapped Role object
     */
    @Mapping(target = "features", ignore = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "description", source = "description")
    @Mapping(target = "active", source = "active")
    Role toTarget(RoleEntity roleEntity);

    /**
     * Maps a Role object to a RoleEntity object.
     *
     * @param role the Role object to map from
     * @return the mapped RoleEntity object
     */
    @InheritInverseConfiguration
    RoleEntity toSource(Role role);

    /**
     * Sets the RoleFeatureEntity objects for a RoleEntity based on the features of a Role.
     *
     * @param role the Role object containing the features
     * @param roleEntity the RoleEntity object to set the RoleFeatureEntity objects for
     */
    @AfterMapping
    default void setRoleFeature(Role role, @MappingTarget RoleEntity roleEntity) {
        if (Objects.nonNull(role.getFeatures()) && !role.getFeatures().isEmpty()) {
            if (Objects.isNull(roleEntity.getRoleFeatures())) {
                roleEntity.setRoleFeatures(new HashSet<>());
            }
            role.getFeatures().forEach(feature -> {
                var roleFeature = new RoleFeatureEntity();
                final var featureEntity = new FeatureEntity();

                featureEntity.setId(feature.getId());
                featureEntity.setName(featureEntity.getName());
                featureEntity.setDescription(featureEntity.getDescription());
                featureEntity.setActive(feature.getActive());
                roleFeature.setRole(roleEntity);
                roleFeature.setFeature(featureEntity);
                roleEntity.getRoleFeatures().add(roleFeature);
            });
        }
    }

    /**
     * Sets the features for a Role based on the RoleFeatureEntity objects of a RoleEntity.
     *
     * @param roleEntity the RoleEntity object containing the RoleFeatureEntity objects
     * @param role the Role object to set the features for
     */
    @AfterMapping
    default void setFeature(RoleEntity roleEntity, @MappingTarget Role role) {
        if (Objects.nonNull(roleEntity.getRoleFeatures()) && !roleEntity.getRoleFeatures().isEmpty()) {
            if (Objects.isNull(role.getFeatures())) {
                role.setFeatures(new ArrayList<>());
            }
            roleEntity.getRoleFeatures().forEach(roleFeatureEntity -> {
                var feature = new Feature();
                feature.setId(roleFeatureEntity.getFeature().getId());
                feature.setName(roleFeatureEntity.getFeature().getName());
                feature.setDescription(roleFeatureEntity.getFeature().getDescription());
                feature.setActive(roleFeatureEntity.getFeature().getActive());
                role.getFeatures().add(feature);
            });
        }
    }
}

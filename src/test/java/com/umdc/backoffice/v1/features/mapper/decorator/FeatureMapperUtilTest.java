/*
 *  @(#)FeatureMapperUtilTest.java
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

package com.umdc.backoffice.v1.features.mapper.decorator;

import com.umdc.backoffice.v1.features.mapper.FeatureMapper;
import com.umdc.commons.general.pojo.Feature;
import com.umdc.commons.general.pojo.Role;
import com.umdc.persistence.general.domains.FeatureEntity;
import com.umdc.persistence.general.domains.RoleFeatureEntity;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class FeatureMapperUtilTest {

    @Mock
    private FeatureMapper featureMapper;

    private FeatureMapperUtil featureMapperUtil;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        featureMapperUtil = new FeatureMapperUtil(featureMapper);
    }

    @Test
    @DisplayName("toFeature extracts the nested Feature id from each RoleFeatureEntity")
    void toFeature_extractsFeatureIds() {
        var featureId = UUID.randomUUID();
        var featureEntity = new FeatureEntity();
        featureEntity.setId(featureId);
        var roleFeatureEntity = new RoleFeatureEntity();
        roleFeatureEntity.setFeature(featureEntity);

        List<Feature> result = featureMapperUtil.toFeature(Set.of(roleFeatureEntity));

        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals(featureId, result.get(0).getId());
    }

    @Test
    @DisplayName("toFeature returns an empty list for an empty set")
    void toFeature_emptySet_returnsEmptyList() {
        List<Feature> result = featureMapperUtil.toFeature(Set.of());

        Assertions.assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("toRoleFeatureEntity(List<Feature>) maps each feature into a new RoleFeatureEntity")
    void toRoleFeatureEntityFromList_mapsEachFeature() {
        var feature = new Feature();
        feature.setId(UUID.randomUUID());
        feature.setName("USER_READ");
        feature.setDescription("Read users");
        feature.setActive(true);

        Set<RoleFeatureEntity> result = featureMapperUtil.toRoleFeatureEntity(List.of(feature));

        Assertions.assertEquals(1, result.size());
        var mapped = result.iterator().next();
        Assertions.assertEquals(feature.getId(), mapped.getFeature().getId());
        Assertions.assertEquals(feature.getName(), mapped.getFeature().getName());
        Assertions.assertEquals(feature.getDescription(), mapped.getFeature().getDescription());
        Assertions.assertEquals(feature.getActive(), mapped.getFeature().getActive());
    }

    @Test
    @DisplayName("toRoleFeatureEntity(List<Feature>) returns an empty set when the list is null")
    void toRoleFeatureEntityFromList_nullList_returnsEmptySet() {
        Set<RoleFeatureEntity> result = featureMapperUtil.toRoleFeatureEntity(null);

        Assertions.assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("toRoleFeatureEntity(List<Feature>) returns an empty set when the list is empty")
    void toRoleFeatureEntityFromList_emptyList_returnsEmptySet() {
        Set<RoleFeatureEntity> result = featureMapperUtil.toRoleFeatureEntity(List.of());

        Assertions.assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("toRoleFeatureEntity(Role, List<Feature>) builds one active link per feature for the given role")
    void toRoleFeatureEntityFromRoleAndList_buildsLinksForEachFeature() {
        var role = new Role();
        role.setId(UUID.randomUUID());
        role.setActive(true);
        role.setDescription("Administrator");
        role.setName("ADMIN");

        var feature = new Feature();
        feature.setId(UUID.randomUUID());
        var featureEntity = new FeatureEntity();
        featureEntity.setId(feature.getId());
        when(featureMapper.toSource(feature)).thenReturn(featureEntity);

        List<RoleFeatureEntity> result = featureMapperUtil.toRoleFeatureEntity(role, List.of(feature));

        Assertions.assertEquals(1, result.size());
        var mapped = result.get(0);
        Assertions.assertEquals(role.getId(), mapped.getRole().getId());
        Assertions.assertEquals(role.getActive(), mapped.getRole().isActive());
        Assertions.assertEquals(role.getDescription(), mapped.getRole().getDescription());
        Assertions.assertEquals(role.getName(), mapped.getRole().getName());
        Assertions.assertEquals(featureEntity, mapped.getFeature());
        Assertions.assertTrue(mapped.getActive());
        verify(featureMapper).toSource(feature);
    }
}

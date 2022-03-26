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
package com.prx.backoffice.v1.role.service;

import com.prx.backoffice.MockLoaderBase;
import com.prx.backoffice.v1.feature.mapper.FeatureMapper;
import com.prx.backoffice.v1.feature.mapper.decorator.FeatureMapperUtil;
import com.prx.backoffice.v1.role.api.to.RoleLinkRequest;
import com.prx.backoffice.v1.role.mapper.RoleMapper;
import com.prx.commons.pojo.Feature;
import com.prx.commons.pojo.Role;
import com.prx.persistence.general.domains.FeatureEntity;
import com.prx.persistence.general.domains.RoleEntity;
import com.prx.persistence.general.domains.RoleFeatureEntity;
import com.prx.persistence.general.repositories.RoleRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;

/**
 * RoleServiceImplTest.
 *
 * @author &lt;a href='mailto:luis.antonio.mata@gmail.com'&gt;Luis Antonio Mata&lt;/a&gt;
 * @version 1.0.0, 17-12-2021
 * @since 11
 */
class RoleServiceImplTest extends MockLoaderBase {

    @InjectMocks
    RoleServiceImpl roleService;

    @Mock
    RoleMapper roleMapper;
    @Mock
    FeatureMapper featureMapper;
    @Mock
    RoleRepository roleRepository;
    @Mock
    FeatureMapperUtil featureMapperUtil;

    @BeforeEach
    void initService(){
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void find() {
        var roleEntity = new RoleEntity();
        var featureEntity = new FeatureEntity();
        var roleFeatureEntity = new RoleFeatureEntity();
        featureEntity.setId(1L);
        featureEntity.setActive(true);
        featureEntity.setName("Feature name");
        featureEntity.setDescription("Feature description");
        roleEntity.setId(1L);
        roleEntity.setActive(true);
        roleEntity.setName("Role name");
        roleEntity.setDescription("Role description");
        roleFeatureEntity.setActive(true);
        roleFeatureEntity.setRole(roleEntity.getId());
        roleFeatureEntity.setFeature(featureEntity.getId());
        var optionalRole = Optional.of(roleEntity);

        Mockito.when(roleRepository.findById(Mockito.anyLong())).thenReturn(optionalRole);
        final var responseEntity = roleService.find(1L);
        Assertions.assertNotNull(responseEntity);
    }

    @Test
    void list() {
    }

    @Test
    void create() {
        var roleEntity = new RoleEntity();
        var featureEntity = new FeatureEntity();
        var roleFeatureEntity = new RoleFeatureEntity();
        featureEntity.setId(1L);
        featureEntity.setActive(true);
        featureEntity.setName("Feature name");
        featureEntity.setDescription("Feature description");
        roleEntity.setId(1L);
        roleEntity.setActive(true);
        roleEntity.setName("Rol name");
        roleEntity.setDescription("Rol description");
        roleFeatureEntity.setActive(true);
        roleFeatureEntity.setRole(roleEntity.getId());
        roleFeatureEntity.setFeature(featureEntity.getId());

        Mockito.doReturn(featureEntity).when(featureMapper).toSource(ArgumentMatchers.any(Feature.class));
        Mockito.doReturn(roleEntity.getRoleFeatures()).when(featureMapperUtil).toRoleFeatureEntity(ArgumentMatchers.anyList());
        Mockito.doReturn(roleEntity).when(roleMapper).toSource(ArgumentMatchers.any(Role.class));
        Mockito.when(roleRepository.save(ArgumentMatchers.any(RoleEntity.class))).thenReturn(roleEntity);
        final var responseEntity = roleService.create(getRole());
        Assertions.assertNotNull(responseEntity);
    }

    @Test
    void link() {
    }

    @Test
    void update() {
    }

    @Test
    void delete() {
    }

    @Test
    void testList() {
    }

    @Test
    void unlink() {
    }

    @Test
    void testList1() {
    }

    private @NotNull Role getRole() {
        final var role = new Role();
        final var feature = new Feature();
        feature.setId(1L);
        feature.setActive(true);
        feature.setName("Feature name");
        feature.setDescription("Feature description");
        role.setId(1L);
        role.setActive(true);
        role.setName("Role name");
        role.setFeatures(new ArrayList<>());
        role.getFeatures().add(feature);
        role.setDescription("Role description");
        return role;
    }

    private @NotNull RoleLinkRequest getRoleLinkRequest() {
        var roleLinkRequest = new RoleLinkRequest();
        roleLinkRequest.setAppName("APP-TEST-001");
        roleLinkRequest.setAppToken("T000X");
        roleLinkRequest.setDateTime(LocalDateTime.now());
        return roleLinkRequest;
    }
}

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
package com.prx.backoffice.v1.roles.service;

import com.prx.backoffice.v1.features.mapper.FeatureMapper;
import com.prx.backoffice.v1.features.mapper.decorator.FeatureMapperUtil;
import com.prx.backoffice.v1.features.service.FeatureService;
import com.prx.backoffice.v1.roles.mapper.RoleMapper;
import com.prx.commons.pojo.Feature;
import com.prx.commons.pojo.Role;
import com.prx.persistence.general.domains.FeatureEntity;
import com.prx.persistence.general.domains.RoleEntity;
import com.prx.persistence.general.domains.RoleFeatureEntity;
import com.prx.persistence.general.domains.RoleFeaturePK;
import com.prx.persistence.general.repositories.RoleFeatureRepository;
import com.prx.persistence.general.repositories.RoleRepository;
import jakarta.validation.constraints.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * RoleServiceImplTest.
 *
 * @author &lt;a href='mailto:luis.antonio.mata@gmail.com'&gt;Luis Antonio Mata&lt;/a&gt;
 * @version 1.0.0, 17-12-2021
 * @since 11
 */
@ContextConfiguration(classes = {RoleServiceImpl.class})
@ExtendWith(SpringExtension.class)
class RoleServiceImplTest {

    @MockBean
    private FeatureService featureService;

    @MockBean
    private RoleFeatureRepository roleFeatureRepository;

    @Autowired
    private RoleServiceImpl roleServiceImpl;

    @MockBean
    private RoleMapper roleMapper;

    @MockBean
    private FeatureMapper featureMapper;

    @MockBean
    private RoleRepository roleRepository;

    @MockBean
    private FeatureMapperUtil featureMapperUtil;

    @Test
    void find() {
        final var roleId = UUID.randomUUID();
        var roleEntity = new RoleEntity();
        var featureEntity = new FeatureEntity();
        var roleFeatureEntity = new RoleFeatureEntity();
        featureEntity.setId(roleId);
        featureEntity.setActive(true);
        featureEntity.setName("Feature name");
        featureEntity.setDescription("Feature description");
        roleEntity.setId(roleId);
        roleEntity.setActive(true);
        roleEntity.setName("Role name");
        roleEntity.setDescription("Role description");
        roleFeatureEntity.setActive(true);
        roleFeatureEntity.setRole(roleEntity);
        roleFeatureEntity.setFeature(featureEntity);
        var optionalRole = Optional.of(roleEntity);

        Mockito.when(roleRepository.findById(Mockito.any(UUID.class))).thenReturn(optionalRole);
        final var responseEntity = roleServiceImpl.find(roleId.toString());
        Assertions.assertNotNull(responseEntity);
    }

    @Test
    void list() {
        final var roles = new ArrayList<RoleEntity>();
        final Optional<List<RoleEntity>> rolesOption = Optional.of(roles);
        Mockito.when(roleRepository.findById(Mockito.anyList())).thenReturn(rolesOption);
        final var response = roleServiceImpl.list("18e4914b-f1f0-4c33-8559-944cf36b4b99",
                "e255e868-80f2-4161-ab9d-25f47c913cf8", "b4256add-939d-45db-a491-0fb38ad37d60",
                "87b036e8-a332-4960-8d9f-fb88530ca2bd");
        Assertions.assertNotNull(response);
    }

    /**
     * Method under test: {@link RoleServiceImpl#list(Boolean, List)}
     */
    @Test
    void testList6() {
        ResponseEntity<List<Role>> actualListResult = roleServiceImpl.list(true, new ArrayList<>());
        assertNull(actualListResult.getBody());
        assertEquals(HttpStatus.NOT_FOUND, actualListResult.getStatusCode());
        assertTrue(actualListResult.getHeaders().isEmpty());
    }

    /**
     * Method under test: {@link RoleServiceImpl#list(String[])}
     */
    @Test
    void testList2() {
        ArrayList<RoleEntity> roleEntityList = new ArrayList<>();
        RoleEntity roleEntity = new RoleEntity();
        roleEntity.setActive(true);
        roleEntity.setDescription("The characteristics of someone or something");
        roleEntity.setId(UUID.fromString("cc8f6d52-500d-4021-99c2-e53baafdc30b"));
        roleEntity.setName("Name");
        roleEntity.setRoleFeatures(new HashSet<>());
        roleEntity.setUserRoleEntities(new HashSet<>());
        roleEntityList.add(roleEntity);
        when(roleRepository.findByUserId(any())).thenReturn(Optional.of(roleEntityList));
        ResponseEntity<List<Role>> actualListResult = roleServiceImpl.listByUser("cc8f6d52-500d-4021-99c2-e53baafdc30b");
        List<Role> body = actualListResult.getBody();
        assertTrue(Objects.nonNull(body));
        assertEquals(HttpStatus.OK, actualListResult.getStatusCode());
        assertTrue(actualListResult.getHeaders().isEmpty());
        verify(roleRepository).findByUserId(any());
    }

    /**
     * Method under test: {@link RoleServiceImpl#list(String[])}
     */
    @Test
    void testList3() {
        final var roleId = UUID.randomUUID();
        RoleEntity roleEntity = new RoleEntity();
        roleEntity.setActive(true);
        roleEntity.setDescription("The characteristics of someone or something");
        roleEntity.setId(roleId);
        roleEntity.setName("Name");
        roleEntity.setRoleFeatures(new HashSet<>());
        roleEntity.setUserRoleEntities(new HashSet<>());

        ArrayList<RoleEntity> roleEntityList = new ArrayList<>();
        roleEntityList.add(roleEntity);
        Optional<List<RoleEntity>> ofResult = Optional.of(roleEntityList);
        when(roleRepository.findByUserId(any())).thenReturn(ofResult);

        Role role = new Role();
        role.setActive(true);
        role.setDescription("The characteristics of someone or something");
        role.setFeatures(new ArrayList<>());
        role.setId(roleId.toString());
        role.setName("Name");
        when(roleMapper.toTarget(any())).thenReturn(role);
        ResponseEntity<List<Role>> actualListResult = roleServiceImpl.listByUser(roleId.toString());
        assertEquals(1, Objects.requireNonNull(actualListResult.getBody()).size());
        assertTrue(actualListResult.hasBody());
        assertTrue(actualListResult.getHeaders().isEmpty());
        assertEquals(HttpStatus.OK, actualListResult.getStatusCode());
        verify(roleRepository).findByUserId(any());
        verify(roleMapper).toTarget(any());
    }

    /**
     * Method under test: {@link RoleServiceImpl#list(String[])}
     */
    @Test
    void testList4() {
        final var roleId = UUID.randomUUID();
        RoleEntity roleEntity = new RoleEntity();
        roleEntity.setActive(true);
        roleEntity.setDescription("The characteristics of someone or something");
        roleEntity.setId(roleId);
        roleEntity.setName("Name");
        roleEntity.setRoleFeatures(new HashSet<>());
        roleEntity.setUserRoleEntities(new HashSet<>());

        RoleEntity roleEntity1 = new RoleEntity();
        roleEntity1.setActive(true);
        roleEntity1.setDescription("The characteristics of someone or something");
        roleEntity1.setId(roleId);
        roleEntity1.setName("Name");
        roleEntity1.setRoleFeatures(new HashSet<>());
        roleEntity1.setUserRoleEntities(new HashSet<>());

        ArrayList<RoleEntity> roleEntityList = new ArrayList<>();
        roleEntityList.add(roleEntity1);
        roleEntityList.add(roleEntity);
        Optional<List<RoleEntity>> ofResult = Optional.of(roleEntityList);
        when(roleRepository.findByUserId(any())).thenReturn(ofResult);

        Role role = new Role();
        role.setActive(true);
        role.setDescription("The characteristics of someone or something");
        role.setFeatures(new ArrayList<>());
        role.setId("123L");
        role.setName("Name");
        when(roleMapper.toTarget((RoleEntity) any())).thenReturn(role);
        ResponseEntity<List<Role>> actualListResult = roleServiceImpl.listByUser(roleId.toString());
        assertEquals(2, Objects.requireNonNull(actualListResult.getBody()).size());
        assertTrue(actualListResult.hasBody());
        assertTrue(actualListResult.getHeaders().isEmpty());
        assertEquals(HttpStatus.OK, actualListResult.getStatusCode());
        verify(roleRepository).findByUserId(any());
        verify(roleMapper, atLeast(1)).toTarget((RoleEntity) any());
    }

    /**
     * Method under test: {@link RoleServiceImpl#list(String[])}
     */
    @Test
    void testList5() {
        final var roleId = UUID.randomUUID();

        Role role = new Role();
        role.setActive(true);
        role.setDescription("The characteristics of someone or something");
        role.setFeatures(new ArrayList<>());
        role.setId(roleId.toString());
        role.setName("Name");

        RoleEntity roleEntity = new RoleEntity();
        roleEntity.setActive(true);
        roleEntity.setDescription("The characteristics of someone or something");
        roleEntity.setId(roleId);
        roleEntity.setName("Name");
        roleEntity.setRoleFeatures(new HashSet<>());
        roleEntity.setUserRoleEntities(new HashSet<>());

        RoleEntity roleEntity1 = new RoleEntity();
        roleEntity1.setActive(true);
        roleEntity1.setDescription("The characteristics of someone or something");
        roleEntity1.setId(roleId);
        roleEntity1.setName("Name");
        roleEntity1.setRoleFeatures(new HashSet<>());
        roleEntity1.setUserRoleEntities(new HashSet<>());

        Optional<List<RoleEntity>> ofResult = Optional.of(new ArrayList<>());
        when(roleRepository.findByUserId(any())).thenReturn(ofResult);
//        when(roleMapper.toTarget(any())).thenReturn(role);
        ResponseEntity<List<Role>> actualListResult = roleServiceImpl.listByUser(roleId.toString());
        assertTrue(Objects.requireNonNull(actualListResult.getBody()).isEmpty());
        assertEquals(HttpStatus.OK, actualListResult.getStatusCode());
        assertTrue(actualListResult.getHeaders().isEmpty());
        verify(roleRepository).findByUserId(any());
    }

    @Test
    void testCreate() {
        final var roleId = UUID.randomUUID();
        final var featureId = UUID.randomUUID();
        var roleEntity = new RoleEntity();
        var featureEntity = new FeatureEntity();
        var roleFeatureEntity = new RoleFeatureEntity();
        featureEntity.setId(roleId);
        featureEntity.setActive(true);
        featureEntity.setName("Feature name");
        featureEntity.setDescription("Feature description");
        roleEntity.setId(featureId);
        roleEntity.setActive(true);
        roleEntity.setName("Rol name");
        roleEntity.setDescription("Rol description");
        roleFeatureEntity.setActive(true);
        roleFeatureEntity.setRole(roleEntity);
        roleFeatureEntity.setFeature(featureEntity);

        Mockito.doReturn(featureEntity).when(featureMapper).toSource(ArgumentMatchers.any(Feature.class));
        Mockito.doReturn(roleEntity.getRoleFeatures()).when(featureMapperUtil).toRoleFeatureEntity(ArgumentMatchers.anyList());
        Mockito.doReturn(roleEntity).when(roleMapper).toSource(ArgumentMatchers.any(Role.class));
        Mockito.when(roleRepository.save(ArgumentMatchers.any(RoleEntity.class))).thenReturn(roleEntity);
        final var responseEntity = roleServiceImpl.create(getRole());
        Assertions.assertNotNull(responseEntity);
    }

    /**
     * Method under test: {@link RoleServiceImpl#update(String, Role)}
     */
    @Test
    void testUpdate() {
        final var roleId = UUID.randomUUID();
        Role role = new Role();
        role.setActive(true);
        role.setDescription("The characteristics of someone or something");
        role.setFeatures(new ArrayList<>());
        role.setId(roleId.toString());
        role.setName("Name");
        final var roleEntity = new RoleEntity();
        roleEntity.setActive(true);
        roleEntity.setDescription("The characteristics of someone or something");
        roleEntity.setRoleFeatures(null);
        roleEntity.setId(roleId);
        roleEntity.setName("Name");
        when(roleRepository.findById(Mockito.<UUID>any())).thenReturn(Optional.of(roleEntity));
        when(roleMapper.toSource(Mockito.<Role>any())).thenReturn(roleEntity);
        when(roleMapper.toTarget(Mockito.<RoleEntity>any())).thenReturn(role);
        when(roleRepository.save(Mockito.<RoleEntity>any())).thenReturn(roleEntity);
        ResponseEntity<Role> response = roleServiceImpl.update(roleId.toString(), role);
        assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
        assertTrue(Objects.nonNull(response.getBody()));
        assertEquals("Name", response.getBody().getName());
    }

    /**
     * Method under test: {@link RoleServiceImpl#update(String, Role)}
     */
    @Test
    void testUpdate2() {
        final var featureId = UUID.randomUUID();
        final var roleId = UUID.randomUUID();
        final Role role = new Role();
        final Feature feature = new Feature();
        final var roleEntity = new RoleEntity();
        final FeatureEntity featureEntity = new FeatureEntity();
        final RoleFeaturePK roleFeaturePK = new RoleFeaturePK();
        final RoleFeatureEntity roleFeatureEntity = new RoleFeatureEntity();

        role.setActive(true);
        role.setDescription("The characteristics of someone or something");
        role.setFeatures(new ArrayList<>());
        role.setId(roleId.toString());
        role.setName("Name");
        feature.setId(featureId.toString());
        feature.setName("deeply");
        feature.setDescription("Tobacco tub delivery milk increased.");
        feature.setActive(true);

        roleEntity.setActive(true);
        roleEntity.setDescription("The characteristics of someone or something");
        roleEntity.setRoleFeatures(null);
        roleEntity.setId(roleId);
        roleEntity.setName("Name");
        featureEntity.setId(featureId);
        featureEntity.setName("deeply");
        featureEntity.setDescription("Tobacco tub delivery milk increased.");
        featureEntity.setActive(true);
        roleFeaturePK.setRoleId(roleEntity.getId());
        roleFeaturePK.setFeatureId(featureEntity.getId());
        roleFeatureEntity.setRoleFeaturePK(roleFeaturePK);
        roleFeatureEntity.setActive(true);
        roleFeatureEntity.setRole(roleEntity);
        roleFeatureEntity.setFeature(featureEntity);
        roleEntity.setRoleFeatures(new HashSet<>());
        roleEntity.getRoleFeatures().add(roleFeatureEntity);

        when(roleRepository.findById(Mockito.<UUID>any())).thenReturn(Optional.of(roleEntity));
        when(roleMapper.toSource(Mockito.<Role>any())).thenReturn(roleEntity);
        when(roleMapper.toTarget(Mockito.<RoleEntity>any())).thenReturn(role);
        when(roleRepository.save(Mockito.<RoleEntity>any())).thenReturn(roleEntity);
        ResponseEntity<Role> response = roleServiceImpl.update(roleId.toString(), role);
        assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
        assertTrue(Objects.nonNull(response.getBody()));
        assertEquals("Name", response.getBody().getName());
    }

    /**
     * Method under test: {@link RoleServiceImpl#listByUser(String)}
     */
    @Test
    void testListByUser() {
        final var roleId = UUID.randomUUID();
        final var featureId = UUID.randomUUID();
        final var userId = UUID.randomUUID();
        Role role = new Role();
        Feature feature = new Feature();
        RoleEntity roleEntity = new RoleEntity();
        FeatureEntity featureEntity = new FeatureEntity();
        RoleFeatureEntity roleFeatureEntity = new RoleFeatureEntity();
        RoleFeaturePK roleFeaturePK = new RoleFeaturePK();

        role.setActive(true);
        role.setDescription("The characteristics of someone or something");
        role.setFeatures(new ArrayList<>());
        role.setId(roleId.toString());
        role.setName("Name");

        feature.setId(featureId.toString());
        feature.setName("deeply");
        feature.setDescription("Tobacco tub delivery milk increased.");
        feature.setActive(true);

        roleEntity.setActive(true);
        roleEntity.setDescription("The characteristics of someone or something");
        roleEntity.setId(roleId);
        roleEntity.setName("Name");

        featureEntity.setId(featureId);
        featureEntity.setName("deeply");
        featureEntity.setDescription("Tobacco tub delivery milk increased.");
        featureEntity.setActive(true);

        roleFeaturePK.setRoleId(roleEntity.getId());
        roleFeaturePK.setFeatureId(featureEntity.getId());

        roleFeatureEntity.setRoleFeaturePK(roleFeaturePK);
        roleFeatureEntity.setActive(true);
        roleFeatureEntity.setRole(roleEntity);
        roleFeatureEntity.setFeature(featureEntity);

        roleEntity.setRoleFeatures(new HashSet<>());
        roleEntity.getRoleFeatures().add(roleFeatureEntity);

        when(roleRepository.findByUserId(Mockito.<UUID>any())).thenReturn(Optional.of(List.of(roleEntity)));
        when(roleMapper.toTarget(Mockito.<RoleEntity>any())).thenReturn(role);
        final var response = roleServiceImpl.listByUser(userId.toString());
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(Objects.nonNull(response.getBody()));
        assertEquals("Name", response.getBody().get(0).getName());
    }

    /**
     * Method under test: {@link RoleServiceImpl#listByUser(String)}
     */
    @Test
    void testListByUser_not_found() {
        final var roleId = UUID.randomUUID();
        final var featureId = UUID.randomUUID();
        final var userId = UUID.randomUUID();
        Role role = new Role();
        Feature feature = new Feature();
        RoleEntity roleEntity = new RoleEntity();
        FeatureEntity featureEntity = new FeatureEntity();
        RoleFeatureEntity roleFeatureEntity = new RoleFeatureEntity();
        RoleFeaturePK roleFeaturePK = new RoleFeaturePK();

        role.setActive(true);
        role.setDescription("The characteristics of someone or something");
        role.setFeatures(new ArrayList<>());
        role.setId(roleId.toString());
        role.setName("Name");

        feature.setId(featureId.toString());
        feature.setName("deeply");
        feature.setDescription("Tobacco tub delivery milk increased.");
        feature.setActive(true);

        roleEntity.setActive(true);
        roleEntity.setDescription("The characteristics of someone or something");
        roleEntity.setId(roleId);
        roleEntity.setName("Name");

        featureEntity.setId(featureId);
        featureEntity.setName("deeply");
        featureEntity.setDescription("Tobacco tub delivery milk increased.");
        featureEntity.setActive(true);

        roleFeaturePK.setRoleId(roleEntity.getId());
        roleFeaturePK.setFeatureId(featureEntity.getId());

        roleFeatureEntity.setRoleFeaturePK(roleFeaturePK);
        roleFeatureEntity.setActive(true);
        roleFeatureEntity.setRole(roleEntity);
        roleFeatureEntity.setFeature(featureEntity);

        roleEntity.setRoleFeatures(new HashSet<>());
        roleEntity.getRoleFeatures().add(roleFeatureEntity);

        when(roleRepository.findByUserId(Mockito.<UUID>any())).thenReturn(Optional.empty());
        when(roleMapper.toTarget(Mockito.<RoleEntity>any())).thenReturn(role);
        final var response = roleServiceImpl.listByUser(userId.toString());
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertTrue(Objects.isNull(response.getBody()));
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
        feature.setId("1L");
        feature.setActive(true);
        feature.setName("Feature name");
        feature.setDescription("Feature description");
        role.setId("1L");
        role.setActive(true);
        role.setName("Role name");
        role.setFeatures(new ArrayList<>());
        role.getFeatures().add(feature);
        role.setDescription("Role description");
        return role;
    }


}

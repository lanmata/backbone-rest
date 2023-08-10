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
import com.prx.backoffice.v1.roles.api.to.RoleLinkRequest;
import com.prx.backoffice.v1.roles.mapper.RoleMapper;
import com.prx.commons.pojo.Feature;
import com.prx.commons.pojo.Role;
import com.prx.persistence.general.domains.FeatureEntity;
import com.prx.persistence.general.domains.RoleEntity;
import com.prx.persistence.general.domains.RoleFeatureEntity;
import com.prx.persistence.general.repositories.RoleFeatureRepository;
import com.prx.persistence.general.repositories.RoleRepository;
import jakarta.validation.constraints.NotNull;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;

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
        var roleEntity = new RoleEntity();
        var featureEntity = new FeatureEntity();
        var roleFeatureEntity = new RoleFeatureEntity();
        featureEntity.setId(UUID.fromString("1L"));
        featureEntity.setActive(true);
        featureEntity.setName("Feature name");
        featureEntity.setDescription("Feature description");
        roleEntity.setId(UUID.fromString("1L"));
        roleEntity.setActive(true);
        roleEntity.setName("Role name");
        roleEntity.setDescription("Role description");
        roleFeatureEntity.setActive(true);
        roleFeatureEntity.setRole(roleEntity);
        roleFeatureEntity.setFeature(featureEntity);
        var optionalRole = Optional.of(roleEntity);

        Mockito.when(roleRepository.findById(Mockito.any(UUID.class))).thenReturn(optionalRole);
        final var responseEntity = roleServiceImpl.find("50199e6a-155d-4067-9a40-2f62c87c2e55");
        Assertions.assertNotNull(responseEntity);
    }

    @Test
    void list() {
        final var roles = new ArrayList<RoleEntity>();
        final Optional<List<RoleEntity>> rolesOption = Optional.of(roles);
        Mockito.when(roleRepository.findAllById(Mockito.anyList())).thenReturn(rolesOption);
        final var response = roleServiceImpl.list("18e4914b-f1f0-4c33-8559-944cf36b4b99",
                "e255e868-80f2-4161-ab9d-25f47c913cf8", "b4256add-939d-45db-a491-0fb38ad37d60",
                "87b036e8-a332-4960-8d9f-fb88530ca2bd");
        Assertions.assertNotNull(response);
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
        when(roleRepository.findAllByUserId(any())).thenReturn(Optional.of(roleEntityList));
        ResponseEntity<List<Role>> actualListResult = roleServiceImpl.list("cc8f6d52-500d-4021-99c2-e53baafdc30b");
        List<Role> body = actualListResult.getBody();
        assertTrue(Objects.nonNull(body));
        assertEquals(HttpStatus.OK, actualListResult.getStatusCode());
        assertTrue(actualListResult.getHeaders().isEmpty());
        verify(roleRepository).findAllByUserId(any());
    }

    /**
     * Method under test: {@link RoleServiceImpl#list(String[])}
     */
    @Test
    void testList3() {
        RoleEntity roleEntity = new RoleEntity();
        roleEntity.setActive(true);
        roleEntity.setDescription("The characteristics of someone or something");
        roleEntity.setId(UUID.fromString("123L"));
        roleEntity.setName("Name");
        roleEntity.setRoleFeatures(new HashSet<>());
        roleEntity.setUserRoleEntities(new HashSet<>());

        ArrayList<RoleEntity> roleEntityList = new ArrayList<>();
        roleEntityList.add(roleEntity);
        Optional<List<RoleEntity>> ofResult = Optional.of(roleEntityList);
        when(roleRepository.findAllById(any())).thenReturn(ofResult);

        Role role = new Role();
        role.setActive(true);
        role.setDescription("The characteristics of someone or something");
        role.setFeatures(new ArrayList<>());
        role.setId("2ba32a02-5f0f-42cf-b56d-37a4f3e95720");
        role.setName("Name");
        when(roleMapper.toTarget(any())).thenReturn(role);
        ResponseEntity<List<Role>> actualListResult = roleServiceImpl.list("123L");
        assertEquals(1, Objects.requireNonNull(actualListResult.getBody()).size());
        assertTrue(actualListResult.hasBody());
        assertTrue(actualListResult.getHeaders().isEmpty());
        assertEquals(HttpStatus.OK, actualListResult.getStatusCode());
        verify(roleRepository).findAllById(any());
        verify(roleMapper).toTarget(any());
    }

    /**
     * Method under test: {@link RoleServiceImpl#list(String[])}
     */
    @Test
    void testList4() {
        RoleEntity roleEntity = new RoleEntity();
        roleEntity.setActive(true);
        roleEntity.setDescription("The characteristics of someone or something");
        roleEntity.setId(UUID.fromString("123L"));
        roleEntity.setName("Name");
        roleEntity.setRoleFeatures(new HashSet<>());
        roleEntity.setUserRoleEntities(new HashSet<>());

        RoleEntity roleEntity1 = new RoleEntity();
        roleEntity1.setActive(true);
        roleEntity1.setDescription("The characteristics of someone or something");
        roleEntity1.setId(UUID.fromString("123L"));
        roleEntity1.setName("Name");
        roleEntity1.setRoleFeatures(new HashSet<>());
        roleEntity1.setUserRoleEntities(new HashSet<>());

        ArrayList<RoleEntity> roleEntityList = new ArrayList<>();
        roleEntityList.add(roleEntity1);
        roleEntityList.add(roleEntity);
        Optional<List<RoleEntity>> ofResult = Optional.of(roleEntityList);
        when(roleRepository.findAllById(any())).thenReturn(ofResult);

        Role role = new Role();
        role.setActive(true);
        role.setDescription("The characteristics of someone or something");
        role.setFeatures(new ArrayList<>());
        role.setId("123L");
        role.setName("Name");
        when(roleMapper.toTarget((RoleEntity) any())).thenReturn(role);
        ResponseEntity<List<Role>> actualListResult = roleServiceImpl.list("123L");
        assertEquals(2, Objects.requireNonNull(actualListResult.getBody()).size());
        assertTrue(actualListResult.hasBody());
        assertTrue(actualListResult.getHeaders().isEmpty());
        assertEquals(HttpStatus.OK, actualListResult.getStatusCode());
        verify(roleRepository).findAllById(any());
        verify(roleMapper, atLeast(1)).toTarget((RoleEntity) any());
    }

    /**
     * Method under test: {@link RoleServiceImpl#list(String[])}
     */
    @Test
    void testList5() {
        when(roleRepository.findAllById(any())).thenReturn(Optional.empty());

        Role role = new Role();
        role.setActive(true);
        role.setDescription("The characteristics of someone or something");
        role.setFeatures(new ArrayList<>());
        role.setId("123L");
        role.setName("Name");
        when(roleMapper.toTarget(any())).thenReturn(role);
        ResponseEntity<List<Role>> actualListResult = roleServiceImpl.list("123L");
        assertNull(actualListResult.getBody());
        assertEquals(HttpStatus.NOT_FOUND, actualListResult.getStatusCode());
        assertTrue(actualListResult.getHeaders().isEmpty());
        verify(roleRepository).findAllById(any());
    }

    @Test
    void testCreate() {
        var roleEntity = new RoleEntity();
        var featureEntity = new FeatureEntity();
        var roleFeatureEntity = new RoleFeatureEntity();
        featureEntity.setId(UUID.fromString("1L"));
        featureEntity.setActive(true);
        featureEntity.setName("Feature name");
        featureEntity.setDescription("Feature description");
        roleEntity.setId(UUID.fromString("1L"));
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
    @Disabled("TODO: Complete this test")
    void testUpdate() {
        // TODO: Complete this test.
        //   Reason: R013 No inputs found that don't throw a trivial exception.
        //   Diffblue Cover tried to run the arrange/act section, but the method under
        //   test threw
        //   java.lang.IllegalArgumentException: Invalid UUID string: 42
        //       at java.base/java.util.UUID.fromString1(UUID.java:280)
        //       at java.base/java.util.UUID.fromString(UUID.java:258)
        //       at com.prx.backoffice.v1.roles.service.RoleServiceImpl.update(RoleServiceImpl.java:141)
        //   See https://diff.blue/R013 to resolve this issue.

        Role role = new Role();
        role.setActive(true);
        role.setDescription("The characteristics of someone or something");
        role.setFeatures(new ArrayList<>());
        role.setId("42");
        role.setName("Name");
        roleServiceImpl.update("42", role);
    }

    /**
     * Method under test: {@link RoleServiceImpl#update(String, Role)}
     */
    @Test
    @Disabled("TODO: Complete this test")
    void testUpdate2() {
        // TODO: Complete this test.
        //   Reason: R013 No inputs found that don't throw a trivial exception.
        //   Diffblue Cover tried to run the arrange/act section, but the method under
        //   test threw
        //   java.lang.IllegalArgumentException: Invalid UUID string: 42
        //       at java.base/java.util.UUID.fromString1(UUID.java:280)
        //       at java.base/java.util.UUID.fromString(UUID.java:258)
        //       at com.prx.backoffice.v1.roles.service.RoleServiceImpl.update(RoleServiceImpl.java:141)
        //   See https://diff.blue/R013 to resolve this issue.

        Role role = mock(Role.class);
        doNothing().when(role).setActive(Mockito.<Boolean>any());
        doNothing().when(role).setDescription(Mockito.<String>any());
        doNothing().when(role).setFeatures(Mockito.<List<Feature>>any());
        doNothing().when(role).setId(Mockito.<String>any());
        doNothing().when(role).setName(Mockito.<String>any());
        role.setActive(true);
        role.setDescription("The characteristics of someone or something");
        role.setFeatures(new ArrayList<>());
        role.setId("42");
        role.setName("Name");
        roleServiceImpl.update("42", role);
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

    private @NotNull RoleLinkRequest getRoleLinkRequest() {
        var roleLinkRequest = new RoleLinkRequest();
        roleLinkRequest.setAppName("APP-TEST-001");
        roleLinkRequest.setAppToken("T000X");
        roleLinkRequest.setDateTime(LocalDateTime.now());
        return roleLinkRequest;
    }

}

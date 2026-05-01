/*
 *  @(#)RoleServiceImplTest.java
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
package com.umdc.backoffice.v1.roles.service;

import com.umdc.backoffice.v1.features.mapper.FeatureMapper;
import com.umdc.backoffice.v1.features.mapper.decorator.FeatureMapperUtil;
import com.umdc.backoffice.v1.features.service.FeatureService;
import com.umdc.backoffice.v1.roles.mapper.RoleMapper;
import com.umdc.commons.general.pojo.Feature;
import com.umdc.commons.general.pojo.Role;
import com.umdc.persistence.general.domains.FeatureEntity;
import com.umdc.persistence.general.domains.RoleEntity;
import com.umdc.persistence.general.domains.RoleFeatureEntity;
import com.umdc.persistence.general.domains.RoleFeaturePK;
import com.umdc.persistence.general.repositories.RoleFeatureRepository;
import com.umdc.persistence.general.repositories.RoleRepository;
import jakarta.validation.constraints.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

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
class RoleServiceImplTest {

    @InjectMocks
    private RoleServiceImpl roleServiceImpl;

    @Mock
    private FeatureService featureService;

    @Mock
    private RoleFeatureRepository roleFeatureRepository;

    @Mock
    private RoleMapper roleMapper;

    @Mock
    private FeatureMapper featureMapper;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private FeatureMapperUtil featureMapperUtil;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("Test finding a role by ID")
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
        final var responseEntity = roleServiceImpl.find(roleId);
        Assertions.assertNotNull(responseEntity);
    }

    @Test
    @DisplayName("Test listing roles by IDs")
    void list() {
        final var roles = new ArrayList<RoleEntity>();
        final Optional<List<RoleEntity>> rolesOption = Optional.of(roles);
        Mockito.when(roleRepository.findById(Mockito.anyList())).thenReturn(rolesOption);
        final var response = roleServiceImpl.list(UUID.randomUUID(),
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());
        Assertions.assertNotNull(response);
    }

    /**
     * Method under test: {@link RoleServiceImpl#list(Boolean, List)}
     */
    @Test
    @DisplayName("Test listing roles with empty list")
    void testList6() {
        ResponseEntity<List<Role>> actualListResult = roleServiceImpl.list(true, new ArrayList<>());
        assertNull(actualListResult.getBody());
        assertEquals(HttpStatus.NOT_FOUND, actualListResult.getStatusCode());
        assertTrue(actualListResult.getHeaders().isEmpty());
    }

    /**
     * Method under test: {@link RoleServiceImpl#list(UUID[])}
     */
    @Test
    @DisplayName("Test listing roles by user ID")
    void testListByUser2() {
        var uuid = UUID.randomUUID();
        ArrayList<RoleEntity> roleEntityList = new ArrayList<>();
        RoleEntity roleEntity = new RoleEntity();
        roleEntity.setActive(true);
        roleEntity.setDescription("The characteristics of someone or something");
        roleEntity.setId(uuid);
        roleEntity.setName("Name");
        roleEntity.setRoleFeatures(new HashSet<>());
        roleEntity.setApplicationRoleUser(new HashSet<>());
        roleEntityList.add(roleEntity);
        when(roleRepository.findByUserId(any())).thenReturn(Optional.of(roleEntityList));
        ResponseEntity<List<Role>> actualListResult = roleServiceImpl.listByUser(uuid);
        List<Role> body = actualListResult.getBody();
        assertTrue(Objects.nonNull(body));
        assertEquals(HttpStatus.OK, actualListResult.getStatusCode());
        assertTrue(actualListResult.getHeaders().isEmpty());
        verify(roleRepository).findByUserId(any());
    }

    /**
     * Method under test: {@link RoleServiceImpl#list(UUID[])}
     */
    @Test
    @DisplayName("Test listing roles by user ID with multiple roles")
    void testListByUser3() {
        final var roleId = UUID.randomUUID();
        RoleEntity roleEntity = new RoleEntity();
        roleEntity.setActive(true);
        roleEntity.setDescription("The characteristics of someone or something");
        roleEntity.setId(roleId);
        roleEntity.setName("Name");
        roleEntity.setRoleFeatures(new HashSet<>());
        roleEntity.setApplicationRoleUser(new HashSet<>());

        ArrayList<RoleEntity> roleEntityList = new ArrayList<>();
        roleEntityList.add(roleEntity);
        Optional<List<RoleEntity>> ofResult = Optional.of(roleEntityList);
        when(roleRepository.findByUserId(any())).thenReturn(ofResult);

        Role role = new Role();
        role.setActive(true);
        role.setDescription("The characteristics of someone or something");
        role.setFeatures(new ArrayList<>());
        role.setId(roleId);
        role.setName("Name");
        when(roleMapper.toTarget(any(RoleEntity.class))).thenReturn(role);
        ResponseEntity<List<Role>> actualListResult = roleServiceImpl.listByUser(roleId);
        assertEquals(1, Objects.requireNonNull(actualListResult.getBody()).size());
        assertTrue(actualListResult.hasBody());
        assertTrue(actualListResult.getHeaders().isEmpty());
        assertEquals(HttpStatus.OK, actualListResult.getStatusCode());
        verify(roleRepository).findByUserId(any());
        verify(roleMapper).toTarget(any(RoleEntity.class));
    }

    /**
     * Method under test: {@link RoleServiceImpl#list(UUID[])}
     */
    @Test
    @DisplayName("Test listing roles by user ID with multiple roles and entities")
    void testListByUser4() {
        final var roleId = UUID.randomUUID();
        RoleEntity roleEntity = new RoleEntity();
        roleEntity.setActive(true);
        roleEntity.setDescription("The characteristics of someone or something");
        roleEntity.setId(roleId);
        roleEntity.setName("Name");
        roleEntity.setRoleFeatures(new HashSet<>());
        roleEntity.setApplicationRoleUser(new HashSet<>());

        RoleEntity roleEntity1 = new RoleEntity();
        roleEntity1.setActive(true);
        roleEntity1.setDescription("The characteristics of someone or something");
        roleEntity1.setId(roleId);
        roleEntity1.setName("Name");
        roleEntity1.setRoleFeatures(new HashSet<>());
        roleEntity1.setApplicationRoleUser(new HashSet<>());

        ArrayList<RoleEntity> roleEntityList = new ArrayList<>();
        roleEntityList.add(roleEntity1);
        roleEntityList.add(roleEntity);
        Optional<List<RoleEntity>> ofResult = Optional.of(roleEntityList);
        when(roleRepository.findByUserId(any())).thenReturn(ofResult);

        Role role = new Role();
        role.setActive(true);
        role.setDescription("The characteristics of someone or something");
        role.setFeatures(new ArrayList<>());
        role.setId(roleId);
        role.setName("Name");
        when(roleMapper.toTarget((RoleEntity) any())).thenReturn(role);
        ResponseEntity<List<Role>> actualListResult = roleServiceImpl.listByUser(roleId);
        assertEquals(2, Objects.requireNonNull(actualListResult.getBody()).size());
        assertTrue(actualListResult.hasBody());
        assertTrue(actualListResult.getHeaders().isEmpty());
        assertEquals(HttpStatus.OK, actualListResult.getStatusCode());
        verify(roleRepository).findByUserId(any());
        verify(roleMapper, atLeast(1)).toTarget((RoleEntity) any());
    }

    /**
     * Method under test: {@link RoleServiceImpl#list(UUID[])}
     */
    @Test
    @DisplayName("Test listing roles by user ID with empty result")
    void testListByUser5() {
        final var roleId = UUID.randomUUID();

        Role role = new Role();
        role.setActive(true);
        role.setDescription("The characteristics of someone or something");
        role.setFeatures(new ArrayList<>());
        role.setId(roleId);
        role.setName("Name");

        RoleEntity roleEntity = new RoleEntity();
        roleEntity.setActive(true);
        roleEntity.setDescription("The characteristics of someone or something");
        roleEntity.setId(roleId);
        roleEntity.setName("Name");
        roleEntity.setRoleFeatures(new HashSet<>());
        roleEntity.setApplicationRoleUser(new HashSet<>());

        RoleEntity roleEntity1 = new RoleEntity();
        roleEntity1.setActive(true);
        roleEntity1.setDescription("The characteristics of someone or something");
        roleEntity1.setId(roleId);
        roleEntity1.setName("Name");
        roleEntity1.setRoleFeatures(new HashSet<>());
        roleEntity1.setApplicationRoleUser(new HashSet<>());

        Optional<List<RoleEntity>> ofResult = Optional.of(new ArrayList<>());
        when(roleRepository.findByUserId(any())).thenReturn(ofResult);
        ResponseEntity<List<Role>> actualListResult = roleServiceImpl.listByUser(roleId);
        assertTrue(Objects.requireNonNull(actualListResult.getBody()).isEmpty());
        assertEquals(HttpStatus.OK, actualListResult.getStatusCode());
        assertTrue(actualListResult.getHeaders().isEmpty());
        verify(roleRepository).findByUserId(any());
    }

    @Test
    @DisplayName("Test creating a role")
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
     * Method under test: {@link RoleServiceImpl#update(UUID, Role)}
     */
    @Test
    @DisplayName("Test updating a role")
    void testUpdate() {
        final var roleId = UUID.randomUUID();
        Role role = new Role();
        role.setActive(true);
        role.setDescription("The characteristics of someone or something");
        role.setFeatures(new ArrayList<>());
        role.setId(roleId);
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
        ResponseEntity<Role> response = roleServiceImpl.update(roleId, role);
        assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
        assertTrue(Objects.nonNull(response.getBody()));
        assertEquals("Name", response.getBody().getName());
    }

    /**
     * Method under test: {@link RoleServiceImpl#update(UUID, Role)}
     */
    @Test
    @DisplayName("Test updating a role with features")
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
        role.setId(roleId);
        role.setName("Name");
        feature.setId(featureId);
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
        ResponseEntity<Role> response = roleServiceImpl.update(roleId, role);
        assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
        assertTrue(Objects.nonNull(response.getBody()));
        assertEquals("Name", response.getBody().getName());
    }

    /**
     * Method under test: {@link RoleServiceImpl#listByUser(UUID)}
     */
    @Test
    @DisplayName("Test listing roles by user ID with features")
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
        role.setId(roleId);
        role.setName("Name");

        feature.setId(featureId);
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
        final var response = roleServiceImpl.listByUser(userId);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(Objects.nonNull(response.getBody()));
        assertEquals("Name", response.getBody().getFirst().getName());
    }

    /**
     * Method under test: {@link RoleServiceImpl#listByUser(UUID)}
     */
    @Test
    @DisplayName("Test listing roles by user ID not found")
    void testListByUser_not_found() {
        when(roleRepository.findByUserId(Mockito.<UUID>any())).thenReturn(Optional.empty());
        final var response = roleServiceImpl.listByUser(UUID.randomUUID());
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertTrue(Objects.isNull(response.getBody()));
    }

    @Test
    @DisplayName("Test listing roles not found")
    void testList_not_found() {
        when(roleRepository.findAll()).thenReturn(null);
        final var response = roleServiceImpl.list();
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertTrue(Objects.isNull(response.getBody()));
    }

    /**
     * Method under test: {@link RoleServiceImpl#listByUser(UUID)}
     */
    @Test
    @DisplayName("Test listing roles")
    void testList() {
        final var roleId = UUID.randomUUID();
        final var featureId = UUID.randomUUID();
        Role role = new Role();
        Feature feature = new Feature();
        RoleEntity roleEntity = new RoleEntity();
        FeatureEntity featureEntity = new FeatureEntity();
        RoleFeatureEntity roleFeatureEntity = new RoleFeatureEntity();
        RoleFeaturePK roleFeaturePK = new RoleFeaturePK();

        role.setActive(true);
        role.setDescription("The characteristics of someone or something");
        role.setFeatures(new ArrayList<>());
        role.setId(roleId);
        role.setName("Name");

        feature.setId(featureId);
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

        when(roleRepository.findAll()).thenReturn(List.of(roleEntity));
        when(roleMapper.toTarget(Mockito.<RoleEntity>any())).thenReturn(role);
        final var response = roleServiceImpl.list();
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(Objects.nonNull(response.getBody()));
        assertEquals("Name", response.getBody().getFirst().getName());
    }

    private @NotNull Role getRole() {
        final var role = new Role();
        final var feature = new Feature();
        final var uuid = UUID.randomUUID();
        feature.setId(uuid);
        feature.setActive(true);
        feature.setName("Feature name");
        feature.setDescription("Feature description");
        role.setId(uuid);
        role.setActive(true);
        role.setName("Role name");
        role.setFeatures(new ArrayList<>());
        role.getFeatures().add(feature);
        role.setDescription("Role description");
        return role;
    }

    @Test
    @DisplayName("List roles with null IDs")
    void listRolesWithNullIds() {
        ResponseEntity<List<Role>> response = roleServiceImpl.list((UUID[]) null);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @DisplayName("List roles with non-existent IDs")
    void listRolesWithNonExistentIds() {
        when(roleRepository.findById(anyList())).thenReturn(Optional.empty());

        ResponseEntity<List<Role>> response = roleServiceImpl.list(UUID.randomUUID());

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    @DisplayName("Create role with null role")
    void createRoleWithNullRole() {
        ResponseEntity<Role> response = roleServiceImpl.create(null);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @DisplayName("Create role with valid role")
    void createRoleWithValidRole() {
        Role role = new Role();
        role.setName("Test Role");
        role.setDescription("Test Description");
        role.setActive(true);
        role.setFeatures(new ArrayList<>());

        RoleEntity roleEntity = new RoleEntity();
        roleEntity.setName("Test Role");
        roleEntity.setDescription("Test Description");
        roleEntity.setActive(true);

        when(roleMapper.toSource(role)).thenReturn(roleEntity);
        when(roleRepository.save(roleEntity)).thenReturn(roleEntity);
        when(roleMapper.toTarget(roleEntity)).thenReturn(role);

        ResponseEntity<Role> response = roleServiceImpl.create(role);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Test Role", response.getBody().getName());
    }

    @Test
    @DisplayName("Update role with non-existent role ID")
    void updateRoleWithNonExistentRoleId() {
        var roleId = UUID.randomUUID();
        when(roleRepository.findById(roleId)).thenReturn(Optional.empty());

        ResponseEntity<Role> response = roleServiceImpl.update(roleId, getRole());

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }


}

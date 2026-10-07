/*
 *  @(#)FeatureServiceImplTest.java
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

package com.umdc.backoffice.v1.features.service;

import com.umdc.backoffice.v1.features.mapper.FeatureMapper;
import com.umdc.backoffice.v1.roles.service.RoleGraphLookupService;
import com.umdc.backoffice.v1.rolefeatures.service.RoleFeatureLinkService;
import com.umdc.commons.general.pojo.Feature;
import com.umdc.persistence.general.domains.FeatureEntity;
import com.umdc.persistence.general.domains.RoleEntity;
import com.umdc.persistence.general.repositories.FeatureRepository;
import com.umdc.persistence.general.repositories.RoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
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
 * @author Luis Mata
 */
class FeatureServiceImplTest {
    @InjectMocks
    private FeatureServiceImpl featureServiceImpl;

    @Mock
    private FeatureMapper featureMapper;

    @Mock
    private FeatureRepository featureRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private RoleFeatureLinkService roleFeatureLinkService;

    @Mock
    private FeatureGraphLookupService featureGraphLookupService;

    @Mock
    private RoleGraphLookupService roleGraphLookupService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("Create feature when feature already exists")
    void createFeatureWhenFeatureAlreadyExists() {
        var feature = new Feature();
        var featureEntity = new FeatureEntity();

        feature.setName("Test Feature");
        featureEntity.setName("Test Feature");

        when(featureGraphLookupService.findByNameWithGraph(feature.getName())).thenReturn(Optional.of(featureEntity));

        ResponseEntity<Feature> response = featureServiceImpl.create(feature);

        assertEquals(HttpStatus.NOT_ACCEPTABLE, response.getStatusCode());
    }

    /**
     * Method under test: {@link FeatureServiceImpl#create(Feature)}
     */
    @Test
    @DisplayName("Test creating a feature - Existing feature")
    void testCreate() {
        FeatureEntity featureEntity = new FeatureEntity();
        featureEntity.setActive(true);
        featureEntity.setDescription("The characteristics of someone or something");
        featureEntity.setId(UUID.randomUUID());
        featureEntity.setName("Name");
        featureEntity.setRolFeatures(new HashSet<>());
        Optional<FeatureEntity> ofResult = Optional.of(featureEntity);
        when(featureGraphLookupService.findByNameWithGraph(Mockito.any())).thenReturn(ofResult);

        Feature feature = new Feature();
        feature.setActive(true);
        feature.setDescription("The characteristics of someone or something");
        feature.setName("Name");
        ResponseEntity<Feature> actualCreateResult = featureServiceImpl.create(feature);
        assertNull(actualCreateResult.getBody());
        assertEquals(HttpStatus.NOT_ACCEPTABLE, actualCreateResult.getStatusCode());
        assertTrue(actualCreateResult.getHeaders().isEmpty());
        verify(featureGraphLookupService).findByNameWithGraph(Mockito.any());
    }

    /**
     * Method under test: {@link FeatureServiceImpl#create(Feature)}
     */
    @Test
    @DisplayName("Test creating a feature - New feature")
    void testCreate2() {
        final var featureId = UUID.randomUUID();
        FeatureEntity featureEntity = new FeatureEntity();
        featureEntity.setActive(true);
        featureEntity.setDescription("The characteristics of someone or something");
        featureEntity.setId(featureId);
        featureEntity.setName("Name");
        featureEntity.setRolFeatures(new HashSet<>());
        when(featureRepository.save(Mockito.any())).thenReturn(featureEntity);
        when(featureGraphLookupService.findByNameWithGraph(Mockito.any())).thenReturn(Optional.empty());

        Feature feature = new Feature();
        feature.setActive(true);
        feature.setDescription("The characteristics of someone or something");
        feature.setName("Name");

        FeatureEntity featureEntity2 = new FeatureEntity();
        featureEntity2.setActive(true);
        featureEntity2.setDescription("The characteristics of someone or something");
        featureEntity2.setId(UUID.randomUUID());
        featureEntity2.setName("Name");
        featureEntity2.setRolFeatures(new HashSet<>());
        when(featureMapper.toTarget(Mockito.any())).thenReturn(feature);
        when(featureMapper.toSource(Mockito.any())).thenReturn(featureEntity2);

        Feature feature2 = new Feature();
        feature2.setActive(true);
        feature2.setDescription("The characteristics of someone or something");
        feature2.setName("Name");
        ResponseEntity<Feature> actualCreateResult = featureServiceImpl.create(feature2);
        assertTrue(actualCreateResult.hasBody());
        assertTrue(actualCreateResult.getHeaders().isEmpty());
        assertEquals(HttpStatus.CREATED, actualCreateResult.getStatusCode());
        verify(featureRepository).save(Mockito.any());
        verify(featureGraphLookupService).findByNameWithGraph(Mockito.any());
        verify(featureMapper).toTarget(Mockito.any());
        verify(featureMapper).toSource(Mockito.any());
    }

    /**
     * Method under test: {@link FeatureServiceImpl#create(Feature)}
     */
    @Test
    @DisplayName("Test creating a feature - Mocked feature")
    void testCreate3() {
        FeatureEntity featureEntity = new FeatureEntity();
        featureEntity.setActive(true);
        featureEntity.setDescription("The characteristics of someone or something");
        featureEntity.setName("Name");
        featureEntity.setRolFeatures(new HashSet<>());
        when(featureRepository.save(Mockito.any())).thenReturn(featureEntity);
        when(featureGraphLookupService.findByNameWithGraph(Mockito.any())).thenReturn(Optional.empty());

        Feature feature = new Feature();
        feature.setActive(true);
        feature.setDescription("The characteristics of someone or something");
        feature.setName("Name");

        FeatureEntity featureEntity2 = new FeatureEntity();
        featureEntity2.setActive(true);
        featureEntity2.setDescription("The characteristics of someone or something");
        featureEntity2.setName("Name");
        featureEntity2.setRolFeatures(new HashSet<>());
        when(featureMapper.toTarget(Mockito.any())).thenReturn(feature);
        when(featureMapper.toSource(Mockito.any())).thenReturn(featureEntity2);
        Feature feature2 = mock(Feature.class);
        when(feature2.getName()).thenReturn("Name");
        doNothing().when(feature2).setActive(Mockito.any());
        doNothing().when(feature2).setDescription(Mockito.any());
        doNothing().when(feature2).setId(Mockito.any());
        doNothing().when(feature2).setName(Mockito.any());
        feature2.setActive(true);
        feature2.setDescription("The characteristics of someone or something");
        feature2.setName("Name");
        ResponseEntity<Feature> actualCreateResult = featureServiceImpl.create(feature2);
        assertTrue(actualCreateResult.hasBody());
        assertTrue(actualCreateResult.getHeaders().isEmpty());
        assertEquals(HttpStatus.CREATED, actualCreateResult.getStatusCode());
        verify(featureRepository).save(Mockito.any());
        verify(featureGraphLookupService).findByNameWithGraph(Mockito.any());
        verify(featureMapper).toTarget(Mockito.any());
        verify(featureMapper).toSource(Mockito.any());
        verify(feature2).getName();
        verify(feature2).setActive(Mockito.any());
        verify(feature2).setDescription(Mockito.any());
        verify(feature2).setName(Mockito.any());
    }

    @Test
    @DisplayName("Update feature when feature does not exist")
    void updateFeatureWhenFeatureDoesNotExist() {
        var feature = new Feature();
        var featureEntity = new FeatureEntity();
        final var  featureId = UUID.randomUUID();

        feature.setName("Test Feature");
        featureEntity.setName("Test Feature");
        when(featureGraphLookupService.findByIdWithGraph(featureId)).thenReturn(Optional.empty());

        ResponseEntity<Feature> response = featureServiceImpl.update(featureId, feature);

        assertEquals(HttpStatus.NOT_ACCEPTABLE, response.getStatusCode());
    }

    /**
     * Method under test: {@link FeatureServiceImpl#update(UUID, Feature)}
     */
    @Test
    @DisplayName("Test updating a feature - Successful update")
    void testUpdate3() {
        FeatureEntity featureEntity = new FeatureEntity();
        final var featureId = UUID.randomUUID();
        featureEntity.setActive(true);
        featureEntity.setDescription("The characteristics of someone or something");
        featureEntity.setId(UUID.randomUUID());
        featureEntity.setName("Name");
        featureEntity.setRolFeatures(new HashSet<>());
        when(featureRepository.save(Mockito.any())).thenReturn(featureEntity);

        Feature feature = new Feature();
        feature.setActive(true);
        feature.setDescription("The characteristics of someone or something");
        feature.setId(featureId);
        feature.setName("Name");

        FeatureEntity featureEntity2 = new FeatureEntity();
        featureEntity2.setActive(true);
        featureEntity2.setDescription("The characteristics of someone or something");
        featureEntity2.setId(UUID.randomUUID());
        featureEntity2.setName("Name");
        featureEntity2.setRolFeatures(new HashSet<>());
        when(featureMapper.toTarget(Mockito.any())).thenReturn(feature);
        when(featureMapper.toSource(Mockito.any())).thenReturn(featureEntity2);
        when(featureGraphLookupService.findByIdWithGraph(Mockito.any())).thenReturn(Optional.of(featureEntity2));

        Feature feature2 = mock(Feature.class);
        doNothing().when(feature2).setActive(Mockito.any());
        doNothing().when(feature2).setDescription(Mockito.any());
        doNothing().when(feature2).setId(Mockito.any());
        doNothing().when(feature2).setName(Mockito.any());
        feature2.setActive(true);
        feature2.setDescription("The characteristics of someone or something");
        feature2.setId(featureId);
        feature2.setName("Name");
        ResponseEntity<Feature> actualUpdateResult = featureServiceImpl.update(featureId, feature2);
        assertTrue(actualUpdateResult.hasBody());
        assertTrue(actualUpdateResult.getHeaders().isEmpty());
        assertEquals(HttpStatus.ACCEPTED, actualUpdateResult.getStatusCode());
        verify(featureRepository).save(Mockito.any());
        verify(featureMapper).toTarget(Mockito.any());
        verify(featureMapper).toSource(Mockito.any());
        verify(feature2).setActive(Mockito.any());
        verify(feature2).setDescription(Mockito.any());
        verify(feature2, atLeast(1)).setId(Mockito.any());
        verify(feature2).setName(Mockito.any());
    }

    /**
     * Method under test: {@link FeatureServiceImpl#update(UUID, Feature)}
     */
    @Test
    @DisplayName("Test updating a feature - Feature not found")
    void testUpdate4() {
        var featureId = UUID.fromString("1551c702-154e-47f3-b515-87f6ee960acb");
        Feature feature = new Feature();
        feature.setActive(true);
        feature.setDescription("The characteristics of someone or something");
        feature.setId(featureId);
        feature.setName("Name");
        var result = featureServiceImpl.update(featureId, feature);
        verify(featureGraphLookupService).findByIdWithGraph(Mockito.any());
        assertTrue(result.hasBody());
        assertTrue(result.getHeaders().isEmpty());
    }

    /**
     * Method under test: {@link FeatureServiceImpl#update(UUID, Feature)}
     */
    @Test
    @DisplayName("Test updating a feature - Mocked feature")
    void testUpdate5() {
        final var featureId = UUID.fromString("fda40349-6850-46de-94fc-3ad07608b043");
        Feature feature = mock(Feature.class);
        doNothing().when(feature).setActive(Mockito.any());
        doNothing().when(feature).setDescription(Mockito.any());
        doNothing().when(feature).setId(Mockito.any());
        doNothing().when(feature).setName(Mockito.any());
        feature.setActive(true);
        feature.setDescription("The characteristics of someone or something");
        feature.setId(featureId);
        feature.setName("Name");
        var result = featureServiceImpl.update(featureId, feature);
        verify(featureGraphLookupService).findByIdWithGraph(Mockito.any());
        assertTrue(result.hasBody());
        assertTrue(result.getHeaders().isEmpty());
    }

    /**
     * Method under test: {@link FeatureServiceImpl#list(List, boolean)}
     */
    @Test
    @DisplayName("Test listing features - Empty list")
    void testList() {
        List<FeatureEntity> iterable = new ArrayList<>();
        when(featureGraphLookupService.findAllWithGraph()).thenReturn(iterable);
        ResponseEntity<List<Feature>> actualListResult = featureServiceImpl.list(new ArrayList<>(), true);
        assertNull(actualListResult.getBody());
        assertEquals(HttpStatus.NOT_FOUND, actualListResult.getStatusCode());
        assertTrue(actualListResult.getHeaders().isEmpty());
        verify(featureGraphLookupService).findAllWithGraph();
    }

    @Test
    @DisplayName("List features with null featureIds and empty result")
    void listFeaturesWithNullFeatureIdsAndEmptyResult() {
        when(featureGraphLookupService.findAllWithGraph()).thenReturn(Collections.emptyList());

        ResponseEntity<List<Feature>> response = featureServiceImpl.list(null, true);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    @DisplayName("List features with non-empty featureIds and empty result")
    void listFeaturesWithNonEmptyFeatureIdsAndEmptyResult() {
        List<String> featureIds = List.of(UUID.randomUUID().toString());
        when(featureGraphLookupService.findByIdsAndStatusWithGraph(anyList(), eq(true))).thenReturn(List.of());

        ResponseEntity<List<Feature>> response = featureServiceImpl.list(featureIds, true);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    /**
     * Method under test: {@link FeatureServiceImpl#list(List, boolean)}
     */
    @Test
    @DisplayName("Test listing features - Non-empty list")
    void testList2() {
        final var featureEntities = getFeatureEntities(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());
        final var featuresString = featureEntities.stream().map(featureEntity -> featureEntity.getId().toString()).toList();
        final var featureList = getFeatureList(featureEntities);

        when(featureGraphLookupService.findByIdsAndStatusWithGraph(Mockito.any(), Mockito.anyBoolean())).thenReturn(featureEntities);
        when(featureMapper.toTarget(Mockito.any())).thenReturn(featureList.get(0));
        when(featureRepository.findAllById(Mockito.any())).thenReturn(featureEntities);
        when(featureMapper.toSourceList(Mockito.any())).thenReturn(featureEntities);
        var result = featureServiceImpl.list(featuresString, true);
        assertNotNull(result);
    }

    /**
     * Method under test: {@link FeatureServiceImpl#find(UUID)}
     */
    @Test
    @DisplayName("Test finding a feature - Feature found")
    void testFind() {
        FeatureEntity featureEntity = new FeatureEntity();
        final var featureId = UUID.randomUUID();
        Feature feature = new Feature();
        feature.setActive(true);
        feature.setDescription("The characteristics of someone or something");
        feature.setId(featureId);
        feature.setName("Name");
        featureEntity.setActive(true);
        featureEntity.setDescription("The characteristics of someone or something");
        featureEntity.setId(UUID.randomUUID());
        featureEntity.setName("Name");
        featureEntity.setRolFeatures(new HashSet<>());
        when(featureGraphLookupService.findByIdWithGraph(Mockito.any())).thenReturn(Optional.of(featureEntity));
        when(featureMapper.toTarget(Mockito.any())).thenReturn(feature);
        when(featureMapper.toSource(Mockito.any())).thenReturn(featureEntity);

        ResponseEntity<Feature> response = featureServiceImpl.find(featureId);
        assertTrue(response.hasBody());
        assertTrue(response.getHeaders().isEmpty());
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    /**
     * Method under test: {@link FeatureServiceImpl#find(UUID)}
     */
    @Test
    @DisplayName("Test finding a feature - Feature not found")
    void testFind_not_found() {
        final var featureId = UUID.randomUUID();
        when(featureGraphLookupService.findByIdWithGraph(Mockito.any())).thenReturn(Optional.empty());

        ResponseEntity<Feature> response = featureServiceImpl.find(featureId);
        assertFalse(response.hasBody());
        assertTrue(response.getHeaders().isEmpty());
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    /**
     * Method under test: {@link FeatureServiceImpl#listByRole(UUID)}
     */
    @Test
    @DisplayName("Test listing features by role id")
    void testListByRole() {
        final var roleId = UUID.randomUUID();
        final var featureEntities = getFeatureEntities(UUID.randomUUID(), UUID.randomUUID());
        final var featureList = getFeatureList(featureEntities);

        when(featureGraphLookupService.findByRoleIdWithGraph(roleId)).thenReturn(featureEntities);
        when(featureMapper.toTarget(Mockito.any()))
                .thenReturn(featureList.get(0), featureList.get(1));

        ResponseEntity<List<Feature>> response = featureServiceImpl.listByRole(roleId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(Objects.nonNull(response.getBody()));
        assertEquals(2, response.getBody().size());
        verify(featureGraphLookupService).findByRoleIdWithGraph(roleId);
    }

    /**
     * Method under test: {@link FeatureServiceImpl#listByRole(UUID)}
     */
    @Test
    @DisplayName("Test listing features by role id not found")
    void testListByRole_not_found() {
        final var roleId = UUID.randomUUID();
        when(featureGraphLookupService.findByRoleIdWithGraph(roleId)).thenReturn(List.of());

        ResponseEntity<List<Feature>> response = featureServiceImpl.listByRole(roleId);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
    }

    /**
     * Method under test: {@link FeatureServiceImpl#listByRole(UUID)}
     */
    @Test
    @DisplayName("Test listing features by null role id")
    void testListByRole_nullId() {
        ResponseEntity<List<Feature>> response = featureServiceImpl.listByRole(null);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    /**
     * Method under test: {@link FeatureServiceImpl#create(Feature)}
     */
    @Test
    @DisplayName("Create feature with roleIds links it to the existing roles")
    void createFeatureWithRoleIdsLinksExistingRoles() {
        var roleId = UUID.randomUUID();
        var feature = new Feature();
        feature.setName("Name");
        feature.setRoleIds(List.of(roleId));

        var featureEntity = new FeatureEntity();
        featureEntity.setId(UUID.randomUUID());
        var roleEntity = new RoleEntity();
        roleEntity.setId(roleId);

        when(featureGraphLookupService.findByNameWithGraph("Name")).thenReturn(Optional.empty());
        when(featureMapper.toSource(feature)).thenReturn(featureEntity);
        when(featureRepository.save(featureEntity)).thenReturn(featureEntity);
        when(featureMapper.toTarget(featureEntity)).thenReturn(feature);
        when(roleGraphLookupService.findByIdWithGraph(roleId)).thenReturn(Optional.of(roleEntity));

        ResponseEntity<Feature> response = featureServiceImpl.create(feature);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        verify(roleFeatureLinkService).linkFeatureToRoles(featureEntity, List.of(roleEntity));
    }

    /**
     * Method under test: {@link FeatureServiceImpl#create(Feature)}
     */
    @Test
    @DisplayName("Create feature with a non-existent roleId returns not found")
    void createFeatureWithNonExistentRoleIdReturnsNotFound() {
        var roleId = UUID.randomUUID();
        var feature = new Feature();
        feature.setName("Name");
        feature.setRoleIds(List.of(roleId));

        when(featureGraphLookupService.findByNameWithGraph("Name")).thenReturn(Optional.empty());
        when(roleGraphLookupService.findByIdWithGraph(roleId)).thenReturn(Optional.empty());

        ResponseEntity<Feature> response = featureServiceImpl.create(feature);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(featureRepository, never()).save(any());
    }

    private static ArrayList<FeatureEntity> getFeatureEntities(UUID... featureIds) {
        final var features = new ArrayList<FeatureEntity>();
        for (var featureId : featureIds) {
            var feature = new FeatureEntity();
            feature.setId(featureId);
            feature.setName("Ftr-0001");
            feature.setDescription("Description of feature #1");
            feature.setActive(true);
            features.add(feature);
        }
        return features;
    }

    private static List<Feature> getFeatureList(List<FeatureEntity> featureEntityList) {
        return featureEntityList.stream().map(featureEntity -> {
            var feature = new Feature();
            feature.setActive(featureEntity.getActive());
            feature.setDescription(featureEntity.getDescription());
            feature.setName(featureEntity.getName());
            feature.setId(featureEntity.getId());
            return feature;
        }).toList();
    }
}

package com.prx.backoffice.v1.features.service;

import com.prx.backoffice.v1.features.mapper.FeatureMapper;
import com.prx.commons.pojo.Feature;
import com.prx.persistence.general.domains.FeatureEntity;
import com.prx.persistence.general.repositories.FeatureRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.*;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ContextConfiguration(classes = {FeatureServiceImpl.class})
@ExtendWith(SpringExtension.class)
class FeatureServiceImplTest {
    @MockBean
    private FeatureMapper featureMapper;

    @MockBean
    private FeatureRepository featureRepository;

    @Autowired
    private FeatureServiceImpl featureServiceImpl;

    /**
     * Method under test: {@link FeatureServiceImpl#create(Feature)}
     */
    @Test
    void testCreate() {
        FeatureEntity featureEntity = new FeatureEntity();
        featureEntity.setActive(true);
        featureEntity.setDescription("The characteristics of someone or something");
        featureEntity.setId(UUID.randomUUID());
        featureEntity.setName("Name");
        featureEntity.setRolFeatures(new HashSet<>());
        Optional<FeatureEntity> ofResult = Optional.of(featureEntity);
        when(featureRepository.findByName(Mockito.<String>any())).thenReturn(ofResult);

        Feature feature = new Feature();
        feature.setActive(true);
        feature.setDescription("The characteristics of someone or something");
        feature.setName("Name");
        ResponseEntity<Feature> actualCreateResult = featureServiceImpl.create(feature);
        assertNull(actualCreateResult.getBody());
        assertEquals(HttpStatus.NOT_ACCEPTABLE, actualCreateResult.getStatusCode());
        assertTrue(actualCreateResult.getHeaders().isEmpty());
        verify(featureRepository).findByName(Mockito.<String>any());
    }

    /**
     * Method under test: {@link FeatureServiceImpl#create(Feature)}
     */
    @Test
    void testCreate2() {
        final var featureId = UUID.randomUUID();
        FeatureEntity featureEntity = new FeatureEntity();
        featureEntity.setActive(true);
        featureEntity.setDescription("The characteristics of someone or something");
        featureEntity.setId(featureId);
        featureEntity.setName("Name");
        featureEntity.setRolFeatures(new HashSet<>());
        when(featureRepository.save(Mockito.<FeatureEntity>any())).thenReturn(featureEntity);
        when(featureRepository.findByName(Mockito.<String>any())).thenReturn(Optional.empty());

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
        when(featureMapper.toTarget(Mockito.<FeatureEntity>any())).thenReturn(feature);
        when(featureMapper.toSource(Mockito.<Feature>any())).thenReturn(featureEntity2);

        Feature feature2 = new Feature();
        feature2.setActive(true);
        feature2.setDescription("The characteristics of someone or something");
        feature2.setName("Name");
        ResponseEntity<Feature> actualCreateResult = featureServiceImpl.create(feature2);
        assertTrue(actualCreateResult.hasBody());
        assertTrue(actualCreateResult.getHeaders().isEmpty());
        assertEquals(HttpStatus.CREATED, actualCreateResult.getStatusCode());
        verify(featureRepository).save(Mockito.<FeatureEntity>any());
        verify(featureRepository).findByName(Mockito.<String>any());
        verify(featureMapper).toTarget(Mockito.<FeatureEntity>any());
        verify(featureMapper).toSource(Mockito.<Feature>any());
    }

    /**
     * Method under test: {@link FeatureServiceImpl#create(Feature)}
     */
    @Test
    void testCreate3() {
        FeatureEntity featureEntity = new FeatureEntity();
        featureEntity.setActive(true);
        featureEntity.setDescription("The characteristics of someone or something");
        featureEntity.setName("Name");
        featureEntity.setRolFeatures(new HashSet<>());
        when(featureRepository.save(Mockito.<FeatureEntity>any())).thenReturn(featureEntity);
        when(featureRepository.findByName(Mockito.<String>any())).thenReturn(Optional.empty());

        Feature feature = new Feature();
        feature.setActive(true);
        feature.setDescription("The characteristics of someone or something");
        feature.setName("Name");

        FeatureEntity featureEntity2 = new FeatureEntity();
        featureEntity2.setActive(true);
        featureEntity2.setDescription("The characteristics of someone or something");
        featureEntity2.setName("Name");
        featureEntity2.setRolFeatures(new HashSet<>());
        when(featureMapper.toTarget(Mockito.<FeatureEntity>any())).thenReturn(feature);
        when(featureMapper.toSource(Mockito.<Feature>any())).thenReturn(featureEntity2);
        Feature feature2 = mock(Feature.class);
        when(feature2.getName()).thenReturn("Name");
        doNothing().when(feature2).setActive(Mockito.<Boolean>any());
        doNothing().when(feature2).setDescription(Mockito.<String>any());
        doNothing().when(feature2).setId(Mockito.<String>any());
        doNothing().when(feature2).setName(Mockito.<String>any());
        feature2.setActive(true);
        feature2.setDescription("The characteristics of someone or something");
        feature2.setName("Name");
        ResponseEntity<Feature> actualCreateResult = featureServiceImpl.create(feature2);
        assertTrue(actualCreateResult.hasBody());
        assertTrue(actualCreateResult.getHeaders().isEmpty());
        assertEquals(HttpStatus.CREATED, actualCreateResult.getStatusCode());
        verify(featureRepository).save(Mockito.<FeatureEntity>any());
        verify(featureRepository).findByName(Mockito.<String>any());
        verify(featureMapper).toTarget(Mockito.<FeatureEntity>any());
        verify(featureMapper).toSource(Mockito.<Feature>any());
        verify(feature2).getName();
        verify(feature2).setActive(Mockito.<Boolean>any());
        verify(feature2).setDescription(Mockito.<String>any());
        verify(feature2).setName(Mockito.<String>any());
    }


    /**
     * Method under test: {@link FeatureServiceImpl#update(String, Feature)}
     */
    @Test
    void testUpdate3() {
        FeatureEntity featureEntity = new FeatureEntity();
        final var featureId = UUID.randomUUID();
        featureEntity.setActive(true);
        featureEntity.setDescription("The characteristics of someone or something");
        featureEntity.setId(UUID.randomUUID());
        featureEntity.setName("Name");
        featureEntity.setRolFeatures(new HashSet<>());
        when(featureRepository.save(Mockito.<FeatureEntity>any())).thenReturn(featureEntity);

        Feature feature = new Feature();
        feature.setActive(true);
        feature.setDescription("The characteristics of someone or something");
        feature.setId(featureId.toString());
        feature.setName("Name");

        FeatureEntity featureEntity2 = new FeatureEntity();
        featureEntity2.setActive(true);
        featureEntity2.setDescription("The characteristics of someone or something");
        featureEntity2.setId(UUID.randomUUID());
        featureEntity2.setName("Name");
        featureEntity2.setRolFeatures(new HashSet<>());
        when(featureMapper.toTarget(Mockito.<FeatureEntity>any())).thenReturn(feature);
        when(featureMapper.toSource(Mockito.<Feature>any())).thenReturn(featureEntity2);
        when(featureRepository.findById(Mockito.<UUID>any())).thenReturn(Optional.of(featureEntity2));

        Feature feature2 = mock(Feature.class);
        doNothing().when(feature2).setActive(Mockito.<Boolean>any());
        doNothing().when(feature2).setDescription(Mockito.<String>any());
        doNothing().when(feature2).setId(Mockito.<String>any());
        doNothing().when(feature2).setName(Mockito.<String>any());
        feature2.setActive(true);
        feature2.setDescription("The characteristics of someone or something");
        feature2.setId(featureId.toString());
        feature2.setName("Name");
        ResponseEntity<Feature> actualUpdateResult = featureServiceImpl.update(featureId.toString(), feature2);
        assertTrue(actualUpdateResult.hasBody());
        assertTrue(actualUpdateResult.getHeaders().isEmpty());
        assertEquals(HttpStatus.ACCEPTED, actualUpdateResult.getStatusCode());
        verify(featureRepository).save(Mockito.<FeatureEntity>any());
        verify(featureMapper).toTarget(Mockito.<FeatureEntity>any());
        verify(featureMapper).toSource(Mockito.<Feature>any());
        verify(feature2).setActive(Mockito.<Boolean>any());
        verify(feature2).setDescription(Mockito.<String>any());
        verify(feature2, atLeast(1)).setId(Mockito.<String>any());
        verify(feature2).setName(Mockito.<String>any());
    }

    /**
     * Method under test: {@link FeatureServiceImpl#update(String, Feature)}
     */
    @Test
    void testUpdate4() {
        var featureId = UUID.fromString("1551c702-154e-47f3-b515-87f6ee960acb");
        Feature feature = new Feature();
        feature.setActive(true);
        feature.setDescription("The characteristics of someone or something");
        feature.setId(featureId.toString());
        feature.setName("Name");
        var result = featureServiceImpl.update(featureId.toString(), feature);
        verify(featureRepository).findById(Mockito.<UUID>any());
        assertTrue(result.hasBody());
        assertTrue(result.getHeaders().isEmpty());
    }

    /**
     * Method under test: {@link FeatureServiceImpl#update(String, Feature)}
     */
    @Test
    void testUpdate5() {
        final var featureId = UUID.fromString("fda40349-6850-46de-94fc-3ad07608b043");
        Feature feature = mock(Feature.class);
        doNothing().when(feature).setActive(Mockito.<Boolean>any());
        doNothing().when(feature).setDescription(Mockito.<String>any());
        doNothing().when(feature).setId(Mockito.<String>any());
        doNothing().when(feature).setName(Mockito.<String>any());
        feature.setActive(true);
        feature.setDescription("The characteristics of someone or something");
        feature.setId(featureId.toString());
        feature.setName("Name");
        var result = featureServiceImpl.update(featureId.toString(), feature);
        verify(featureRepository).findById(Mockito.<UUID>any());
        assertTrue(result.hasBody());
        assertTrue(result.getHeaders().isEmpty());
    }

    /**
     * Method under test: {@link FeatureServiceImpl#list(List, boolean)}
     */
    @Test
    void testList() {
        List<FeatureEntity> iterable = new ArrayList<>();
        doNothing().when(iterable).forEach(Mockito.<Consumer<FeatureEntity>>any());
        when(featureRepository.findAllById(Mockito.<Iterable<UUID>>any())).thenReturn(iterable);
        ResponseEntity<List<Feature>> actualListResult = featureServiceImpl.list(new ArrayList<>(), true);
        assertNull(actualListResult.getBody());
        assertEquals(HttpStatus.NOT_FOUND, actualListResult.getStatusCode());
        assertTrue(actualListResult.getHeaders().isEmpty());
        verify(featureRepository).findByIdAndStatus(Mockito.<List<UUID>>any(), anyBoolean());
    }

    /**
     * Method under test: {@link FeatureServiceImpl#list(List, boolean)}
     */
    @Test
    void testList2() {
        final var featureEntities = getFeatureEntities(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());
        final var featuresString = featureEntities.stream().map(featureEntity -> featureEntity.getId().toString()).toList();
        when(featureRepository.findAllById(Mockito.<Iterable<UUID>>any())).thenReturn(featureEntities);
        when(featureMapper.toTargetList(Mockito.<List<FeatureEntity>>any())).thenReturn(getFeatureList(featureEntities));
        when(featureMapper.toSourceList(Mockito.<List<Feature>>any())).thenReturn(featureEntities);
        var result = featureServiceImpl.list(featuresString, true);
        assertNotNull(result);
    }

    /**
     * Method under test: {@link FeatureServiceImpl#find(String)}
     */
    @Test
    void testFind() {
        FeatureEntity featureEntity = new FeatureEntity();
        final var featureId = UUID.randomUUID();
        Feature feature = new Feature();
        feature.setActive(true);
        feature.setDescription("The characteristics of someone or something");
        feature.setId(featureId.toString());
        feature.setName("Name");
        featureEntity.setActive(true);
        featureEntity.setDescription("The characteristics of someone or something");
        featureEntity.setId(UUID.randomUUID());
        featureEntity.setName("Name");
        featureEntity.setRolFeatures(new HashSet<>());
        when(featureRepository.findById(Mockito.<UUID>any())).thenReturn(Optional.of(featureEntity));
        when(featureMapper.toTarget(Mockito.<FeatureEntity>any())).thenReturn(feature);
        when(featureMapper.toSource(Mockito.<Feature>any())).thenReturn(featureEntity);

        ResponseEntity<Feature> response = featureServiceImpl.find(featureId.toString());
        assertTrue(response.hasBody());
        assertTrue(response.getHeaders().isEmpty());
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    /**
     * Method under test: {@link FeatureServiceImpl#find(String)}
     */
    @Test
    void testFind_not_found() {
        final var featureId = UUID.randomUUID();
        when(featureRepository.findById(Mockito.<UUID>any())).thenReturn(Optional.empty());

        ResponseEntity<Feature> response = featureServiceImpl.find(featureId.toString());
        assertFalse(response.hasBody());
        assertTrue(response.getHeaders().isEmpty());
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
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
            feature.setId(featureEntity.getId().toString());
            return feature;
        }).toList();
    }
}


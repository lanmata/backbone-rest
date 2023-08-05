package com.prx.backoffice.v1.features.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.prx.backoffice.v1.features.mapper.FeatureMapper;
import com.prx.commons.pojo.Feature;
import com.prx.persistence.general.domains.FeatureEntity;
import com.prx.persistence.general.repositories.FeatureRepository;

import java.util.HashSet;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

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
        feature.setId("49bdc28b-b203-4c56-9c94-1919928ce596");
        feature.setName("Name");
        ResponseEntity<Feature> actualCreateResult = featureServiceImpl.create(feature);
        assertNull(actualCreateResult.getBody());
        assertEquals(208, actualCreateResult.getStatusCode().value());
        assertTrue(actualCreateResult.getHeaders().isEmpty());
        verify(featureRepository).findByName(Mockito.<String>any());
    }

    /**
     * Method under test: {@link FeatureServiceImpl#create(Feature)}
     */
    @Test
    void testCreate2() {
        FeatureEntity featureEntity = new FeatureEntity();
        featureEntity.setActive(true);
        featureEntity.setDescription("The characteristics of someone or something");
        featureEntity.setId(UUID.randomUUID());
        featureEntity.setName("Name");
        featureEntity.setRolFeatures(new HashSet<>());
        when(featureRepository.save(Mockito.<FeatureEntity>any())).thenReturn(featureEntity);
        when(featureRepository.findByName(Mockito.<String>any())).thenReturn(Optional.empty());

        Feature feature = new Feature();
        feature.setActive(true);
        feature.setDescription("The characteristics of someone or something");
        feature.setId("49bdc28b-b203-4c56-9c94-1919928ce596");
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
        feature2.setId("da9e6ee1-402b-4a2c-bb66-86bf3e936f5f");
        feature2.setName("Name");
        ResponseEntity<Feature> actualCreateResult = featureServiceImpl.create(feature2);
        assertTrue(actualCreateResult.hasBody());
        assertTrue(actualCreateResult.getHeaders().isEmpty());
        assertEquals(201, actualCreateResult.getStatusCode().value());
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
        featureEntity.setId(UUID.randomUUID());
        featureEntity.setName("Name");
        featureEntity.setRolFeatures(new HashSet<>());
        when(featureRepository.save(Mockito.<FeatureEntity>any())).thenReturn(featureEntity);
        when(featureRepository.findByName(Mockito.<String>any())).thenReturn(Optional.empty());

        Feature feature = new Feature();
        feature.setActive(true);
        feature.setDescription("The characteristics of someone or something");
        feature.setId("6ddbec44-dc9e-4351-919f-5cccf2964ce9");
        feature.setName("Name");

        FeatureEntity featureEntity2 = new FeatureEntity();
        featureEntity2.setActive(true);
        featureEntity2.setDescription("The characteristics of someone or something");
        featureEntity2.setId(UUID.randomUUID());
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
        feature2.setId("afd2734c-fed9-4beb-b5de-3ad8cd2bc7c1");
        feature2.setName("Name");
        ResponseEntity<Feature> actualCreateResult = featureServiceImpl.create(feature2);
        assertTrue(actualCreateResult.hasBody());
        assertTrue(actualCreateResult.getHeaders().isEmpty());
        assertEquals(201, actualCreateResult.getStatusCode().value());
        verify(featureRepository).save(Mockito.<FeatureEntity>any());
        verify(featureRepository).findByName(Mockito.<String>any());
        verify(featureMapper).toTarget(Mockito.<FeatureEntity>any());
        verify(featureMapper).toSource(Mockito.<Feature>any());
        verify(feature2).getName();
        verify(feature2).setActive(Mockito.<Boolean>any());
        verify(feature2).setDescription(Mockito.<String>any());
        verify(feature2).setId(Mockito.<String>any());
        verify(feature2).setName(Mockito.<String>any());
    }
}


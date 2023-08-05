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

package com.prx.backoffice.v1.features.api.controller;

import com.prx.backoffice.MockLoaderBase;
import com.prx.backoffice.v1.features.api.to.FeatureRequest;
import com.prx.backoffice.v1.features.mapper.FeatureMapperImpl;
import com.prx.backoffice.v1.features.service.FeatureServiceImpl;
import com.prx.commons.pojo.Feature;
import com.prx.persistence.general.domains.FeatureEntity;
import com.prx.persistence.general.repositories.FeatureRepository;
import io.restassured.module.mockmvc.specification.MockMvcRequestSpecification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Optional;
import java.util.UUID;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.apache.http.HttpHeaders.CONTENT_TYPE;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * FeatureControllerTest.
 *
 * @author &lt;a href='mailto:luis.antonio.mata@gmail.com'&gt;Luis Antonio Mata&lt;/a&gt;
 * @version 1.0.0, 24-03-2022
 * @since 11
 */
class FeatureControllerTest extends MockLoaderBase {

    @MockBean
    FeatureServiceImpl featureService;

    @Mock
    FeatureRepository featureRepository;

    private MockMvcRequestSpecification mockMvcRequestSpecification;

    private static final String PATH_LIST_BY_USER;
    private static final String PATH_UPDATE;
    private static final String PATH_CREATE;
    private static final String PATH_LIST;
    private static final String PATH_FIND;

    static {
        PATH_LIST_BY_USER = "/v1/feature/listByUser/";
        PATH_UPDATE = "/v1/feature/update/";
        PATH_CREATE = "/v1/feature/create";
        PATH_FIND = "/v1/feature/find/";
        PATH_LIST = "/v1/feature/list/";
    }

    @BeforeEach
    void setUp() {
        mockMvcRequestSpecification = given().header(CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
    }

    /**
     * Method under test: {@link FeatureController#create(FeatureRequest)}
     */
    @Test
    void testCreate() {
        FeatureEntity featureEntity = new FeatureEntity();
        featureEntity.setActive(true);
        featureEntity.setDescription("The characteristics of someone or something");
        featureEntity.setId(UUID.randomUUID());
        featureEntity.setName("Name");
        featureEntity.setRolFeatures(new HashSet<>());
        when(featureRepository.findByName(Mockito.<String>any())).thenReturn(Optional.of(featureEntity));
        FeatureController featureController = new FeatureController(
                new FeatureServiceImpl(featureRepository, new FeatureMapperImpl()));

        FeatureRequest featureRequest = getRequest();
        ResponseEntity<Feature> actualCreateResult = featureController.create(featureRequest);
        assertNull(actualCreateResult.getBody());
        assertEquals(208, actualCreateResult.getStatusCode().value());
        assertTrue(actualCreateResult.getHeaders().isEmpty());
        verify(featureRepository).findByName(Mockito.<String>any());
    }

    /**
     * Method under test: {@link FeatureController#create(FeatureRequest)}
     */
    @Test
    void testCreate2() {
        when(featureRepository.findByName(Mockito.<String>any())).thenReturn(Optional.empty());
        FeatureController featureController = new FeatureController(
                new FeatureServiceImpl(featureRepository, new FeatureMapperImpl()));

        FeatureRequest featureRequest = getFeatureRequest();
        var response = featureController.create(featureRequest);
        assertEquals(201, response.getStatusCode().value());
    }

    private static FeatureRequest getFeatureRequest() {
        Feature feature = new Feature();
        feature.setActive(true);
        feature.setDescription("The characteristics of someone or something");
        feature.setName("Name");

        FeatureRequest featureRequest = new FeatureRequest();
        featureRequest.setAppName("App Name");
        featureRequest.setAppToken("ABC123");
        featureRequest.setDateTime(LocalDate.of(1970, 1, 1).atStartOfDay());
        featureRequest.setFeature(feature);
        return featureRequest;
    }

    private static FeatureRequest getRequest() {
        Feature feature = new Feature();
        feature.setActive(true);
        feature.setDescription("The characteristics of someone or something");
        feature.setId(UUID.randomUUID().toString());
        feature.setName("Name");

        FeatureRequest featureRequest = new FeatureRequest();
        featureRequest.setAppName("App Name");
        featureRequest.setAppToken("ABC123");
        featureRequest.setDateTime(LocalDate.of(1970, 1, 1).atStartOfDay());
        featureRequest.setFeature(feature);
        return featureRequest;
    }

    @Test
    void find() {
    }

    @Test
    void testList() {
    }

    @Test
    void create() {
    }

    @Test
    void update() {
    }
}

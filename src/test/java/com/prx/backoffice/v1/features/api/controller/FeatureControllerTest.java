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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MvcResult;

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

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    FeatureServiceImpl featureService;

    @Mock
    FeatureRepository featureRepository;

    private MockMvcRequestSpecification mockMvcRequestSpecification;

    private static final String PATH;

    static {
        PATH = "/v1/features/";
    }

    @BeforeEach
    void setUp() {
        mockMvcRequestSpecification = given().header(CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
    }

    /**
     * Method under test: {@link FeatureController#create(FeatureRequest)}
     */
    @Test
    void testCreate() throws JsonProcessingException {
        final var featureId = UUID.fromString("22e5b1d8-e27c-4ee3-ac6e-f26275e450ff");
        final var featureRequest = getFeatureRequest();
        featureRequest.getFeature().setId(featureId.toString());
        final var response = ResponseEntity.status(HttpStatus.CREATED).body(featureRequest.getFeature());
        //when:
        Mockito.when(featureService.create(Mockito.any(Feature.class))).thenReturn(response);
        //then:
        given().contentType(MediaType.APPLICATION_JSON_VALUE).body(objectMapper.writeValueAsString(featureRequest))
                .accept(MediaType.APPLICATION_JSON_VALUE).when().post(PATH)
                .then().assertThat().statusCode(HttpStatus.CREATED.value()).expect(MvcResult::getResponse);
    }

    /**
     * Method under test: {@link FeatureController#create(FeatureRequest)}
     */
    @Test
    void testCreate2() throws JsonProcessingException {
        final var featureId = UUID.fromString("22e5b1d8-e27c-4ee3-ac6e-f26275e450ff");
        final var featureRequest = getFeatureRequest();
        featureRequest.getFeature().setId(featureId.toString());
        final var response = ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(featureRequest.getFeature());
        //when:
        Mockito.when(featureService.create(Mockito.any(Feature.class))).thenReturn(response);
        //then:
        given().contentType(MediaType.APPLICATION_JSON_VALUE).body(objectMapper.writeValueAsString(featureRequest))
                .accept(MediaType.APPLICATION_JSON_VALUE).when().post(PATH)
                .then().assertThat().statusCode(HttpStatus.NOT_ACCEPTABLE.value()).expect(MvcResult::getResponse);
    }

    /**
     * Method under test: {@link FeatureController#update(String, FeatureRequest)}
     */
    @Test
    void testUpdate() throws JsonProcessingException {
        final var featureId = UUID.fromString("22e5b1d8-e27c-4ee3-ac6e-f26275e450ff");
        final var featureRequest = getFeatureRequest();
        final var response = ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(new Feature());
        //when:
        Mockito.when(featureService.update(Mockito.anyString(),  Mockito.any(Feature.class))).thenReturn(response);
        //then:
        given().contentType(MediaType.APPLICATION_JSON_VALUE).body(objectMapper.writeValueAsString(featureRequest))
                .accept(MediaType.APPLICATION_JSON_VALUE).when().put(PATH.concat(featureId.toString()))
                .then().assertThat().statusCode(HttpStatus.NOT_ACCEPTABLE.value()).expect(MvcResult::getResponse);
    }

    /**
     * Method under test: {@link FeatureController#update(String, FeatureRequest)}
     */
    @Test
    void testUpdate1() throws JsonProcessingException {
        final var featureId = UUID.fromString("22e5b1d8-e27c-4ee3-ac6e-f26275e450ff");
        final var featureRequest = getFeatureRequest();
        final var response = ResponseEntity.status(HttpStatus.ACCEPTED).body(featureRequest.getFeature());
        //when:
        Mockito.when(featureService.update(Mockito.anyString(),  Mockito.any(Feature.class))).thenReturn(response);
        //then:
        given().contentType(MediaType.APPLICATION_JSON_VALUE).body(objectMapper.writeValueAsString(featureRequest))
                .accept(MediaType.APPLICATION_JSON_VALUE).when().put(PATH.concat(featureId.toString()))
                .then().assertThat().statusCode(HttpStatus.ACCEPTED.value()).expect(MvcResult::getResponse);
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

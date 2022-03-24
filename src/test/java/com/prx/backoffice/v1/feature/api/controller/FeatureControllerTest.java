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

package com.prx.backoffice.v1.feature.api.controller;

import com.prx.backoffice.MockLoaderBase;
import com.prx.backoffice.v1.feature.service.FeatureServiceImpl;
import io.restassured.module.mockmvc.specification.MockMvcRequestSpecification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.apache.http.HttpHeaders.CONTENT_TYPE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

/**
 * FeatureControllerTest.
 *
 * @author &lt;a href='mailto:luis.antonio.mata@gmail.com'&gt;Luis Antonio Mata&lt;/a&gt;
 * @version 1.0.0, 24-03-2022
 * @since
 */
class FeatureControllerTest extends MockLoaderBase {

    @MockBean
    FeatureServiceImpl featureService;

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

    @Test
    void find() {
    }

    @Test
    void list() {
        //when:
        var response = mockMvcRequestSpecification.get(PATH_LIST.concat("true").concat("/1,2,3"));
        //then:
        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
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

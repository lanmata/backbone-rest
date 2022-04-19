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

package com.prx.backoffice.v1.contacttype.api;

import com.prx.backoffice.MockLoaderBase;
import com.prx.backoffice.v1.contacttype.service.ContactTypeServiceImpl;
import io.restassured.module.mockmvc.specification.MockMvcRequestSpecification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.apache.http.HttpHeaders.CONTENT_TYPE;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * ContactTypeApiControllerTest.
 *
 * @author Luis Antonio Mata
 * @version 1.0.0, 16-04-2022
 * @since 11
 */
class ContactTypeControllerTest extends MockLoaderBase {

    private static final String PATH_LIST;

    @MockBean
    ContactTypeServiceImpl contactTypeService;

    private MockMvcRequestSpecification mockMvcRequestSpecification;

    static {
        PATH_LIST = "/v1/contact-type/list";
    }

    @BeforeEach
    void setUp() {
        mockMvcRequestSpecification = given().header(CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
    }

    @Test
    @DisplayName("Find a contact type list ")
    void list() {
        //when:
        var response = mockMvcRequestSpecification.get(PATH_LIST);
        // then:
        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
    }
}

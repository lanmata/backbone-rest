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

package com.prx.backoffice.v1.person.api.controller;

import com.prx.backoffice.MockLoaderBase;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.verifier.messaging.internal.ContractVerifierMessaging;
import org.springframework.cloud.contract.verifier.messaging.internal.ContractVerifierObjectMapper;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.context.WebApplicationContext;

/**
 * PersonControllerTest.
 *
 * @author Luis Antonio Mata
 * @version 1.0.1.20200904-01, 06-11-2020
 */
@SpringBootTest
@ActiveProfiles("local")
class PersonControllerTest extends MockLoaderBase {

    @Autowired
    WebApplicationContext applicationContext;

    @BeforeEach
    public void setup() {
        RestAssuredMockMvc.webAppContextSetup(applicationContext);
    }


    @Test
    void testCreate() {
//        MockMvcRequestSpecification request = given()
//                .header("Content-Type", "application/json");
//        ResponseOptions response = given().spec(request).get("")
    }

}

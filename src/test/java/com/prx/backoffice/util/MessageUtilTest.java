/*
 *
 *  * @(#)MessageUtilTest.java.
 *  *
 *  * Copyright (c) Luis Antonio Mata Mata. All rights reserved.
 *  *
 *  * All rights to this product are owned by Luis Antonio Mata Mata and may only
 *  * be used under the terms of its associated license document. You may NOT
 *  * copy, modify, sublicense, or distribute this source file or portions of
 *  * it unless previously authorized in writing by Luis Antonio Mata Mata.
 *  * In any event, this notice and the above copyright must always be included
 *  * verbatim with this file.
 *
 */
package com.prx.backoffice.util;

import com.prx.backoffice.PrxBackofficeRestApplication;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * MessageUtilTest.
 *
 * @author Luis Antonio Mata
 * @version 1.0.1.20200904-01, 26-10-2020
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = PrxBackofficeRestApplication.class,
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {""})
class MessageUtilTest {
    @Autowired
    MessageUtil messageUtil;

    @Test
    void testToString() {
        assertNotNull(messageUtil.toString());
    }

    @Test
    void testMessages(){
        assertNotNull(messageUtil.getSolicitudExitosa());
        assertNotNull(messageUtil.getUserAliasNuloVacio());
        assertNotNull(messageUtil.getSinDatos());
        assertNotNull(messageUtil.getUserClaveNoPermitida());
        assertNotNull(messageUtil.getUserClaveNulaVacia());
        assertNotNull(messageUtil.getUserCorreoNoExiste());
        assertNotNull(messageUtil.getUserCorreoNoValido());
        assertNotNull(messageUtil.getUserCorreoVacio());
        assertNotNull(messageUtil.getUserCreado());
        assertNotNull(messageUtil.getUserExiste());
        assertNotNull(messageUtil.getUserInvalido());
        assertNotNull(messageUtil.getUserSolicitudNulaVacia());
    }

}
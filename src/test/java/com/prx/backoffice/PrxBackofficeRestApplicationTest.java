/*
 *
 *  * @(#)PrxBackofficeRestApplicationTest.java.
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
package com.prx.backoffice;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

/**
 * PrxBackofficeRestApplicationTest.
 *
 * @author <a href="mailto:luis.antonio.mata@gmail.com">Luis Antonio Mata</a>
 * @version 1.0.1.20200904-01, 26-10-2020
 */
@SpringBootTest(classes = PrxBackofficeRestApplication.class)
@TestPropertySource(properties = "startup.beans.inspect=true")
public class PrxBackofficeRestApplicationTest {

    @Test
    public void testLoadContext() {
//        PrxBackofficeRestApplication.main(new String[] {});
        Assertions.assertTrue(true);
    }

}
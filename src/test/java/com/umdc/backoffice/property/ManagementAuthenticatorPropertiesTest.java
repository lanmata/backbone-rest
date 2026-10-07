/*
 *  @(#)ManagementAuthenticatorPropertiesTest.java
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

package com.umdc.backoffice.property;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ManagementAuthenticatorPropertiesTest {

    @Test
    @DisplayName("Test getters and setters of ManagementAuthenticatorProperties")
    void gettersAndSetters() {
        var props = new ManagementAuthenticatorProperties();
        var keystore = new StoreProperties();
        keystore.setLocation("classpath:mcam.jks");
        var truststore = new StoreProperties();
        truststore.setLocation("classpath:mcam-truststore.jks");

        props.setKeyAlias("mcam-rsa");
        props.setKeystore(keystore);
        props.setTruststore(truststore);
        props.setTokenTtlSeconds(3600L);
        props.setRotationGracePeriodSeconds(600L);
        props.setRateLimitRpm(100);

        Assertions.assertAll("Test Getters and Setters",
                () -> Assertions.assertEquals("mcam-rsa", props.getKeyAlias()),
                () -> Assertions.assertEquals(keystore, props.getKeystore()),
                () -> Assertions.assertEquals(truststore, props.getTruststore()),
                () -> Assertions.assertEquals(3600L, props.getTokenTtlSeconds()),
                () -> Assertions.assertEquals(600L, props.getRotationGracePeriodSeconds()),
                () -> Assertions.assertEquals(100, props.getRateLimitRpm())
        );
    }

    @Test
    @DisplayName("default constructor leaves every field at its zero value")
    void defaultConstructor() {
        var props = new ManagementAuthenticatorProperties();

        Assertions.assertAll("Test default state",
                () -> Assertions.assertNull(props.getKeyAlias()),
                () -> Assertions.assertNull(props.getKeystore()),
                () -> Assertions.assertNull(props.getTruststore()),
                () -> Assertions.assertEquals(0L, props.getTokenTtlSeconds()),
                () -> Assertions.assertEquals(0L, props.getRotationGracePeriodSeconds()),
                () -> Assertions.assertEquals(0, props.getRateLimitRpm())
        );
    }
}

/*
 *  @(#)FeatureRequestTest.java
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

package com.umdc.backoffice.v1.features.api.to;

import com.umdc.commons.general.pojo.Feature;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;

/**
 * FeatureRequestTest.
 *
 * @version 1.0.0, 26-03-2021
 */

class FeatureRequestTest {

    @Test
    @DisplayName("Test getters and setters of FeatureRequest")
    void gettersAndSetters() {
        final var featureRequest = new FeatureRequest();
        final var uuid = UUID.randomUUID();
        var feature = new Feature();
        feature.setId(uuid);
        feature.setName("Feature Name");
        feature.setDescription("Feature Description");
        feature.setActive(true);
        featureRequest.setAppName("TEST-APP");
        featureRequest.setAppToken("TEST-APP/00252336");
        featureRequest.setDateTime(LocalDateTime.now(ZoneId.systemDefault()));
        featureRequest.setFeature(feature);

        Assertions.assertAll("Test Getters and Setters",
                () -> Assertions.assertNotNull(featureRequest.getFeature()),
                () -> Assertions.assertNotNull(featureRequest.getAppToken()),
                () -> Assertions.assertNotNull(featureRequest.getAppName()),
                () -> Assertions.assertNotNull(featureRequest.getDateTime()),
                () -> Assertions.assertNotNull(featureRequest.toString()),
                () -> Assertions.assertNotEquals(1, featureRequest.hashCode()),
                () -> Assertions.assertNotEquals(new FeatureRequest(), featureRequest)
        );
    }

}

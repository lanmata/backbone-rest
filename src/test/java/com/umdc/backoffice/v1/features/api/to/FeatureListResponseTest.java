/*
 *  @(#)FeatureListResponseTest.java
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

import com.umdc.backoffice.constant.keys.FeatureMessageKey;
import com.prx.commons.general.pojo.Feature;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.UUID;

/**
 * FeatureListResponseTest.
 *
 * @version 1.0.0, 26-03-2021
 */

public class FeatureListResponseTest {

    @Test
    @DisplayName("Test getters and setters of FeatureListResponse")
    public void gettersAndSetters() {
        final var uuid = UUID.randomUUID();
        final var featureListResponse = new FeatureListResponse();
        var feature = new Feature();
        feature.setId(uuid);
        feature.setName("Feature");
        feature.setDescription("Feature Description");
        feature.setActive(true);
        featureListResponse.setList(new ArrayList<>());
        featureListResponse.getList().add(feature);
        featureListResponse.setCode(FeatureMessageKey.FEATURE_OK.getCode());
        featureListResponse.setMessage(FeatureMessageKey.FEATURE_OK.getStatus());
        featureListResponse.setDateTime(LocalDateTime.now(ZoneId.systemDefault()));

        Assertions.assertAll("Test Getters and Setters",
                () -> Assertions.assertNotNull(featureListResponse.getList()),
                () -> Assertions.assertNotNull(featureListResponse.getCode()),
                () -> Assertions.assertNotNull(featureListResponse.getDateTime()),
                () -> Assertions.assertNotNull(featureListResponse.getMessage()),
                () -> Assertions.assertNotNull(featureListResponse.toString()),
                () -> Assertions.assertNotEquals(1, featureListResponse.hashCode()),
                () -> Assertions.assertNotEquals(new FeatureListResponse(), featureListResponse)
        );
    }
}

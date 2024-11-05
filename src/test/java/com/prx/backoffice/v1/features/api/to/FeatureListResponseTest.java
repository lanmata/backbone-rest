package com.prx.backoffice.v1.features.api.to;

import com.prx.backoffice.enums.keys.FeatureMessageKey;
import com.prx.commons.pojo.Feature;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;

/**
 * FeatureListResponseTest.
 *
 * @version 1.0.0, 26-03-2021
 */

public class FeatureListResponseTest {

    @Test
    @DisplayName("Test getters and setters of FeatureListResponse")
    public void gettersAndSetters() {
        final var featureListResponse = new FeatureListResponse();
        var feature = new Feature();
        feature.setId("1L");
        feature.setName("Feature");
        feature.setDescription("Descripción de feature");
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

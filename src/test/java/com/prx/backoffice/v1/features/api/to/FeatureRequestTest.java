package com.prx.backoffice.v1.features.api.to;

import com.prx.commons.pojo.Feature;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * FeatureRequestTest.
 *
 * @version 1.0.0, 26-03-2021
 */

public class FeatureRequestTest {

    @Test
    @DisplayName("Test getters and setters of FeatureRequest")
    public void gettersAndSetters() {
        final var featureRequest = new FeatureRequest();
        var feature = new Feature();
        feature.setId("1L");
        feature.setName("Feature");
        feature.setDescription("Descripción de feature");
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

package com.prx.backoffice.v1.features.api.to;

import com.prx.backoffice.enums.keys.FeatureMessageKey;
import com.prx.commons.pojo.Feature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * FeatureResponseTest.
 *
 * @version 1.0.0, 14-02-2021
 */

public class FeatureResponseTest {

    @Test
    @DisplayName("Test getters and setters of FeatureResponse")
    public void gettersAndSetters() {
        final var featureResponse = new FeatureResponse();
        final var feature = new Feature();
        feature.setId(UUID.randomUUID().toString());
        feature.setActive(true);
        feature.setDescription("Descripcion de feature");
        feature.setName("Nombre de feature");
        featureResponse.setFeature(feature);
        featureResponse.setCode(FeatureMessageKey.FEATURE_OK.getCode());
        featureResponse.setMessage(FeatureMessageKey.FEATURE_OK.getStatus());
        featureResponse.setDateTime(LocalDateTime.now(ZoneId.systemDefault()));

        assertAll(() -> assertNotNull(featureResponse.getFeature()),
                () -> assertNotNull(featureResponse.getCode()),
                () -> assertNotNull(featureResponse.getDateTime()),
                () -> assertNotNull(featureResponse.getMessage()),
                () -> assertNotNull(featureResponse.toString())
        );
    }
}

package com.prx.backoffice.v1.features.api.to;

import com.prx.backoffice.v1.roles.api.to.RoleLinkRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

/**
 * RolLinkRequestTest.
 *
 * @version 1.0.0, 18-02-2021
 */

class RoleLinkRequestTest {

    @Test
    @DisplayName("Test getters and setters of RoleLinkRequest")
    void testGettersAndSetters() {
        final var roleLinkRequest = new RoleLinkRequest();
        roleLinkRequest.setFeatureIdList(new ArrayList<>());
        roleLinkRequest.getFeatureIdList().add("1L");
        roleLinkRequest.getFeatureIdList().add("2L");
        roleLinkRequest.getFeatureIdList().add("3L");

        assertAll(
                () -> assertNotNull(roleLinkRequest),
                () -> assertNotNull(roleLinkRequest.toString()),
                () -> assertNotNull(roleLinkRequest.getFeatureIdList())
        );

        String[] b = new String[roleLinkRequest.getFeatureIdList().size()];
        b = roleLinkRequest.getFeatureIdList().toArray(b);
        assertArrayEquals(new String[] {"1L", "2L", "3L"}, b);
    }
}

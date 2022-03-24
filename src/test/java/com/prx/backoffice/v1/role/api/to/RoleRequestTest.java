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

package com.prx.backoffice.v1.role.api.to;

import com.prx.commons.pojo.Feature;
import com.prx.commons.pojo.Role;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * RolRequestTest.
 *
 * @author <a href='mailto:luis.antonio.mata@gmail.com'>Luis Antonio Mata</a>
 * @version 1.0.0, 18-02-2021
 */
public class RoleRequestTest {

    @Test
    public void testGettersAndSetters(){
        final var rolRequest = new RoleRequest();
        final var rol = new Role();
        final var feature = new Feature();
        feature.setId(1L);
        feature.setActive(true);
        feature.setName("Nombre de feature");
        feature.setDescription("Descripcion de feature");
        rol.setId(1L);
        rol.setActive(true);
        rol.setName("Nombre de rol");
        rol.setFeatures(new ArrayList<>());
        rol.getFeatures().add(feature);
        rol.setDescription("Descrptcion de rol");
        rolRequest.setRole(rol);
        rolRequest.setAppName("APP-TEST");
        rolRequest.setAppToken("SDFGHJKLKJHGFrty865dfds");
        rolRequest.setDateTime(LocalDateTime.now(ZoneId.systemDefault()));

        assertAll(
                () -> assertNotNull(rolRequest),
                () -> assertNotNull(rolRequest.getRole()),
                () -> assertNotNull(rolRequest.getRole().getId()),
                () -> assertNotNull(rolRequest.getRole().getName()),
                () -> assertNotNull(rolRequest.getRole().toString()),
                () -> assertNotNull(rolRequest.getRole().getActive()),
                () -> assertNotNull(rolRequest.getRole().getFeatures()),
                () -> assertNotNull(rolRequest.getRole().getDescription()),
                () -> assertNotNull(rolRequest.toString()),
                () -> assertNotNull(rolRequest.getAppName()),
                () -> assertNotNull(rolRequest.getAppToken()),
                () -> assertNotNull(rolRequest.getDateTime())
        );
        rolRequest.getRole().getFeatures().forEach(feature1 -> {
            assertNotNull(feature1.getId());
            assertNotNull(feature1.getName());
            assertNotNull(feature1.toString());
            assertNotNull(feature1.getActive());
            assertNotNull(feature1.getDescription());
        });
    }

}

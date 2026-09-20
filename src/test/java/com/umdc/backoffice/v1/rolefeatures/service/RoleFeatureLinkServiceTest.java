/*
 *  @(#)RoleFeatureLinkServiceTest.java
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
package com.umdc.backoffice.v1.rolefeatures.service;

import com.umdc.persistence.general.domains.FeatureEntity;
import com.umdc.persistence.general.domains.RoleEntity;
import com.umdc.persistence.general.domains.RoleFeatureEntity;
import com.umdc.persistence.general.repositories.RoleFeatureRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * @author Luis Mata
 */
class RoleFeatureLinkServiceTest {

    @InjectMocks
    private RoleFeatureLinkService roleFeatureLinkService;

    @Mock
    private RoleFeatureRepository roleFeatureRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    private RoleEntity newRole() {
        final var role = new RoleEntity();
        role.setId(UUID.randomUUID());
        role.setName("Role name");
        return role;
    }

    private FeatureEntity newFeature() {
        final var feature = new FeatureEntity();
        feature.setId(UUID.randomUUID());
        feature.setName("Feature name");
        return feature;
    }

    @Test
    @DisplayName("replaceRoleFeatures links every resolved feature to the role")
    void replaceRoleFeaturesLinksResolvedFeatures() {
        final var role = newRole();
        final var featureOne = newFeature();
        final var featureTwo = newFeature();

        final var links = roleFeatureLinkService.replaceRoleFeatures(role, List.of(featureOne, featureTwo));

        assertEquals(2, links.size());
        assertEquals(links, role.getRoleFeatures());
        links.forEach(link -> {
            assertEquals(role, link.getRole());
            assertTrue(link.getActive());
            assertEquals(role.getId(), link.getRoleFeaturePK().getRoleId());
        });
        verify(roleFeatureRepository).saveAll(anyCollection());
        verify(roleFeatureRepository, never()).deleteAll(anyCollection());
    }

    @Test
    @DisplayName("replaceRoleFeatures deletes the previous links before creating the new ones")
    void replaceRoleFeaturesDeletesExistingLinksFirst() {
        final var role = newRole();
        final var existingLink = new RoleFeatureEntity();
        role.setRoleFeatures(new HashSet<>(Set.of(existingLink)));
        final var newFeature = newFeature();

        roleFeatureLinkService.replaceRoleFeatures(role, List.of(newFeature));

        verify(roleFeatureRepository).deleteAll(Set.of(existingLink));
        verify(roleFeatureRepository).saveAll(anyCollection());
    }

    @Test
    @DisplayName("replaceRoleFeatures with no features clears the role's links without saving")
    void replaceRoleFeaturesWithNoFeaturesClearsLinks() {
        final var role = newRole();
        role.setRoleFeatures(new HashSet<>(Set.of(new RoleFeatureEntity())));

        final var links = roleFeatureLinkService.replaceRoleFeatures(role, List.of());

        assertTrue(links.isEmpty());
        assertNull(role.getRoleFeatures());
        verify(roleFeatureRepository).deleteAll(anyCollection());
        verify(roleFeatureRepository, never()).saveAll(anyCollection());
    }

    @Test
    @DisplayName("replaceRoleFeatures with a null feature list is a no-op on a role with no links")
    void replaceRoleFeaturesWithNullFeaturesAndNoExistingLinks() {
        final var role = newRole();

        final var links = roleFeatureLinkService.replaceRoleFeatures(role, null);

        assertTrue(links.isEmpty());
        verify(roleFeatureRepository, never()).deleteAll(anyCollection());
        verify(roleFeatureRepository, never()).saveAll(anyCollection());
    }

    @Test
    @DisplayName("linkFeatureToRoles links the feature to every given role")
    @SuppressWarnings("unchecked")
    void linkFeatureToRolesLinksEveryRole() {
        final var feature = newFeature();
        final var roleOne = newRole();
        final var roleTwo = newRole();
        final var captor = ArgumentCaptor.forClass(Set.class);

        roleFeatureLinkService.linkFeatureToRoles(feature, List.of(roleOne, roleTwo));

        verify(roleFeatureRepository).saveAll(captor.capture());
        final Set<RoleFeatureEntity> saved = captor.getValue();
        assertEquals(2, saved.size());
        saved.forEach(link -> {
            assertEquals(feature, link.getFeature());
            assertTrue(link.getActive());
            assertEquals(feature.getId(), link.getRoleFeaturePK().getFeatureId());
        });
    }

    @Test
    @DisplayName("linkFeatureToRoles with an empty role collection does not touch the repository")
    void linkFeatureToRolesWithEmptyRolesIsNoOp() {
        final var feature = newFeature();

        roleFeatureLinkService.linkFeatureToRoles(feature, List.of());

        verify(roleFeatureRepository, never()).saveAll(any());
    }

    @Test
    @DisplayName("linkFeatureToRoles with a null role collection does not touch the repository")
    void linkFeatureToRolesWithNullRolesIsNoOp() {
        final var feature = newFeature();

        roleFeatureLinkService.linkFeatureToRoles(feature, null);

        verify(roleFeatureRepository, never()).saveAll(any());
    }
}

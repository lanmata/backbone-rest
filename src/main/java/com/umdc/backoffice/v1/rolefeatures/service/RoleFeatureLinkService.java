/*
 *  @(#)RoleFeatureLinkService.java
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
import com.umdc.persistence.general.domains.RoleFeaturePK;
import com.umdc.persistence.general.repositories.RoleFeatureRepository;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Owns every write to the Role-Feature join table ({@link RoleFeatureEntity}).
 * Shared by the roles and features domains so both sides of the many-to-many
 * relationship go through a single, consistent code path instead of two
 * diverging implementations.
 *
 * @author Luis Antonio Mata
 */
@Service
public class RoleFeatureLinkService {

    private final RoleFeatureRepository roleFeatureRepository;

    public RoleFeatureLinkService(RoleFeatureRepository roleFeatureRepository) {
        this.roleFeatureRepository = roleFeatureRepository;
    }

    /**
     * Replaces the full set of features linked to a role: removes any
     * existing links and creates one link per resolved feature.
     *
     * @param role     the role owning the links (must already have a persisted id)
     * @param features the resolved features to link (existing or newly created)
     * @return the persisted links
     */
    public Set<RoleFeatureEntity> replaceRoleFeatures(RoleEntity role, Collection<FeatureEntity> features) {
        if (Objects.nonNull(role.getRoleFeatures()) && !role.getRoleFeatures().isEmpty()) {
            roleFeatureRepository.deleteAll(role.getRoleFeatures());
        }
        role.setRoleFeatures(null);
        if (Objects.isNull(features) || features.isEmpty()) {
            return Set.of();
        }
        final var links = new HashSet<RoleFeatureEntity>();
        features.forEach(feature -> links.add(buildLink(role, feature)));
        roleFeatureRepository.saveAll(links);
        role.setRoleFeatures(links);
        return links;
    }

    /**
     * Links a feature to one or more existing roles, without touching any
     * links those roles may already have.
     *
     * @param feature the feature owning the links (must already have a persisted id)
     * @param roles   the existing roles to link the feature to
     */
    public void linkFeatureToRoles(FeatureEntity feature, Collection<RoleEntity> roles) {
        if (Objects.isNull(roles) || roles.isEmpty()) {
            return;
        }
        final var links = new HashSet<RoleFeatureEntity>();
        roles.forEach(role -> links.add(buildLink(role, feature)));
        roleFeatureRepository.saveAll(links);
    }

    private RoleFeatureEntity buildLink(RoleEntity role, FeatureEntity feature) {
        final var pk = new RoleFeaturePK();
        pk.setRoleId(role.getId());
        pk.setFeatureId(feature.getId());
        final var link = new RoleFeatureEntity();
        link.setRoleFeaturePK(pk);
        link.setRole(role);
        link.setFeature(feature);
        link.setActive(true);
        return link;
    }
}

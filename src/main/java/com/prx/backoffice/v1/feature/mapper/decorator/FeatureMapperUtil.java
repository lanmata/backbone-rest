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

package com.prx.backoffice.v1.feature.mapper.decorator;

import com.prx.backoffice.v1.feature.mapper.FeatureMapper;
import com.prx.commons.pojo.Feature;
import com.prx.commons.pojo.Role;
import com.prx.commons.util.ValidatorCommonsUtil;
import com.prx.persistence.general.domains.RoleEntity;
import com.prx.persistence.general.domains.RoleFeatureEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * FeatureMapperUtil.
 *
 * @author <a href='mailto:luis.antonio.mata@gmail.com'>Luis Antonio Mata</a>
 * @version 1.0.0, 12-02-2021
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FeatureMapperUtil {
	private final FeatureMapper featureMapper;

	/**
	 * Obtiene los {@link Feature} contenidos en un set de tipo {@link RoleFeatureEntity}.
	 *
	 * @param rolFeatureEntities {@link Set}
	 * @return Objeto de tipo {@link List}
	 */
	public List<Feature> toFeature(Set<RoleFeatureEntity> rolFeatureEntities) {
		List<Feature> features = new ArrayList<>();

		rolFeatureEntities.forEach(rolFeatureEntity -> {
			var feature = new Feature();
			feature.setId(rolFeatureEntity.getFeature());
			features.add(feature);
		});
		return features;
	}

	/**
	 * Obtiene los {@link RoleFeatureEntity} contenidos en una lista de tipo {@link Feature}.
	 *
	 * @param features {@link List}
	 * @return Objeto de tipo {@link Set}
	 */
	public Set<RoleFeatureEntity> toRoleFeatureEntity(List<Feature> features) {
		Set<RoleFeatureEntity> rolFeatureEntities = new HashSet<>();

		if (!ValidatorCommonsUtil.esNulo(features) && !features.isEmpty()) {
			features.forEach(feature -> {
				final var roleFeatureEntity = new RoleFeatureEntity();
				roleFeatureEntity.setFeature(feature.getId());
				rolFeatureEntities.add(roleFeatureEntity);
			});

		}

		return rolFeatureEntities;
	}

	/**
	 * Obtiene los {@link RoleFeatureEntity} contenidos en una lista de tipo {@link Feature} y un rol especifico.
	 *
	 * @param rol {@link Role}
	 * @param features {@link List}
	 * @return Objeto de tipo {@link List}
	 */
	public List<RoleFeatureEntity> toRoleFeatureEntity(Role rol, List<Feature> features) {
		final var roleFeatureEntities = new ArrayList<RoleFeatureEntity>();
		final var roleEntity = new RoleEntity();
		roleEntity.setId(rol.getId());
		roleEntity.setActive(rol.getActive());
		roleEntity.setDescription(rol.getDescription());
		roleEntity.setName(rol.getName());

		features.forEach(feature -> {
			final var rolFeatureEntity = new RoleFeatureEntity();
			rolFeatureEntity.setRole(roleEntity.getId());
			rolFeatureEntity.setFeature(feature.getId());
			rolFeatureEntity.setActive(true);
			roleFeatureEntities.add(rolFeatureEntity);
		});

		return roleFeatureEntities;
	}
}

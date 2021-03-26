/*
 * @(#)FeatureMapperUtil.java.
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

package com.prx.backoffice.mapper.decorator;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.prx.backoffice.mapper.FeatureMapper;
import com.prx.backoffice.mapper.RolMapper;
import com.prx.commons.pojo.Feature;
import com.prx.commons.pojo.Rol;
import com.prx.commons.util.ValidatorCommonsUtil;
import com.prx.persistence.general.domains.RolFeatureEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

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
	private final RolMapper rolMapper;

	/**
	 * Obtiene los {@link Feature} contenidos en un set de tipo {@link RolFeatureEntity}.
	 *
	 * @param rolFeatureEntities {@link Set}
	 * @return Objeto de tipo {@link List}
	 */
	public List<Feature> toFeature(Set<RolFeatureEntity> rolFeatureEntities) {
		List<Feature> features = new ArrayList<>();

		rolFeatureEntities.forEach(rolFeatureEntity -> {
			features.add(featureMapper.toTarget(rolFeatureEntity.getFeature()));
		});

		return features;
	}

	/**
	 * Obtiene los {@link RolFeatureEntity} contenidos en una lista de tipo {@link Feature}.
	 *
	 * @param features {@link List}
	 * @return Objeto de tipo {@link Set}
	 */
	public Set<RolFeatureEntity> toRolFeatureEntity(List<Feature> features) {
		Set<RolFeatureEntity> rolFeatureEntities = new HashSet<>();

		if (!ValidatorCommonsUtil.esNulo(features) && !features.isEmpty()) {
			features.forEach(feature -> {
				final var rolFeatureEntity = new RolFeatureEntity();
				rolFeatureEntity.setFeature(featureMapper.toSource(feature));
				rolFeatureEntities.add(rolFeatureEntity);
			});

		}

		return rolFeatureEntities;
	}

	/**
	 * Obtiene los {@link RolFeatureEntity} contenidos en una lista de tipo {@link Feature} y un rol especifico.
	 *
	 * @param rol {@link Rol}
	 * @param features {@link List}
	 * @return Objeto de tipo {@link List}
	 */
	public List<RolFeatureEntity> toRolFeatureEntity(Rol rol, List<Feature> features) {
		final var rolFeatureEntities = new ArrayList<RolFeatureEntity>();
		final var rolEntity = rolMapper.toSource(rol);

		features.forEach(feature -> {
			final var rolFeatureEntity = new RolFeatureEntity();
			rolFeatureEntity.setRol(rolEntity);
			rolFeatureEntity.setFeature(featureMapper.toSource(feature));
			rolFeatureEntity.setActive(true);
			rolFeatureEntities.add(rolFeatureEntity);
		});

		return rolFeatureEntities;
	}
}

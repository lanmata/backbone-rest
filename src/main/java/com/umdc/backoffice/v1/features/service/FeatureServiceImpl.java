/*
 *  @(#)FeatureServiceImpl.java
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
package com.umdc.backoffice.v1.features.service;

import com.umdc.backoffice.constant.keys.RoleMessageKey;
import com.umdc.backoffice.util.MessageUtil;
import com.umdc.backoffice.v1.features.mapper.FeatureMapper;
import com.umdc.backoffice.v1.rolefeatures.service.RoleFeatureLinkService;
import com.umdc.commons.exception.StandardException;
import com.umdc.commons.general.pojo.Feature;
import com.umdc.persistence.general.domains.FeatureEntity;
import com.umdc.persistence.general.domains.RoleEntity;
import com.umdc.persistence.general.repositories.FeatureRepository;
import com.umdc.persistence.general.repositories.RoleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.*;

import static com.umdc.commons.util.ValidatorCommonsUtil.esNulo;

/**
 * FeatureServiceImpl.
 *
 * @author <a href='mailto:luis.antonio.mata@gmail.com'>Luis Antonio Mata</a>
 * @version 1.0.0, 14-02-2021
 */
@Service
public class FeatureServiceImpl implements FeatureService {

	private static final Logger LOGGER = LoggerFactory.getLogger(FeatureServiceImpl.class);

	private final FeatureRepository featureRepository;

	private final FeatureMapper featureMapper;

	private final RoleRepository roleRepository;

	private final RoleFeatureLinkService roleFeatureLinkService;

	public FeatureServiceImpl(FeatureRepository featureRepository, FeatureMapper featureMapper,
							   RoleRepository roleRepository, RoleFeatureLinkService roleFeatureLinkService) {
		this.featureRepository = featureRepository;
		this.featureMapper = featureMapper;
		this.roleRepository = roleRepository;
		this.roleFeatureLinkService = roleFeatureLinkService;
	}

	/** {@inheritDoc} */
	@Override
	public ResponseEntity<Feature> create(Feature feature) {
		LOGGER.info("Starting feature creation");
		ResponseEntity<Feature> responseEntity;
		LOGGER.info("Feature name duplication validation.");
		final var optFeature = featureRepository.findByName(feature.getName());
		if (optFeature.isPresent()) {
			responseEntity = new ResponseEntity<>(HttpStatus.NOT_ACCEPTABLE);
		} else {
			final List<RoleEntity> resolvedRoles;
			try {
				resolvedRoles = resolveRoles(feature.getRoleIds());
			} catch (StandardException ex) {
				LOGGER.warn("Error resolving roles for feature creation: {}", ex.getStatus().getStatus());
				return ResponseEntity.status(ex.getCode()).build();
			}
			final var featureEntityResult = featureRepository.save(featureMapper.toSource(feature));
			roleFeatureLinkService.linkFeatureToRoles(featureEntityResult, resolvedRoles);
			responseEntity = new ResponseEntity<>(featureMapper.toTarget(featureEntityResult), HttpStatus.CREATED);
		}
		LOGGER.info(responseEntity.getStatusCode() + MessageUtil.LOG_PATH_SEPARATOR + feature);
		return responseEntity;
	}

	/**
	 * Resolves the existing roles a new feature should be linked to.
	 *
	 * @param roleIds the role ids coming from the request payload
	 * @return the resolved {@link RoleEntity} list
	 * @throws StandardException if a role id does not correspond to an existing role
	 */
	private List<RoleEntity> resolveRoles(List<UUID> roleIds) {
		if (Objects.isNull(roleIds) || roleIds.isEmpty()) {
			return List.of();
		}
		final List<RoleEntity> resolved = new ArrayList<>();
		for (UUID roleId : roleIds) {
			resolved.add(roleRepository.findById(roleId)
					.orElseThrow(() -> new StandardException(RoleMessageKey.ROL_NOT_FOUND)));
		}
		return resolved;
	}

	/** {@inheritDoc} */
	@Override
	public ResponseEntity<Feature> update(UUID featureId, Feature feature) {
		ResponseEntity<Feature> responseEntity;
		final var optFeature = featureRepository.findById(featureId);
		if (optFeature.isPresent()) {
			feature.setId(featureId);
			responseEntity = ResponseEntity.accepted().body(featureMapper.toTarget(
					featureRepository.save(featureMapper.toSource(feature))));
		} else {
			responseEntity = ResponseEntity.of(ProblemDetail.forStatusAndDetail(HttpStatus.NOT_ACCEPTABLE,"Feature not registered")).build();
		}
		LOGGER.info(responseEntity.getStatusCode() + MessageUtil.LOG_PATH_SEPARATOR + feature.toString());
		return responseEntity;
	}

	/** {@inheritDoc} */
	@Override
	public ResponseEntity<List<Feature>> list(List<String> featureIds, boolean includeInactive) {
		final var featureListResult = new ArrayList<Feature>();
		List<UUID> uuidList = new ArrayList<>();
		Optional<Iterable<FeatureEntity>> featureEntityListResult;
		if(Objects.isNull(featureIds) || featureIds.isEmpty()) {
			featureEntityListResult = Optional.of(featureRepository.findAll());
		} else {
			featureIds.forEach(s -> uuidList.add(UUID.fromString(s)));
			featureEntityListResult = featureRepository.findByIdAndStatus(uuidList.stream().toList(), includeInactive);
		}

        featureEntityListResult.ifPresent(featureEntities -> featureEntities.forEach(featureEntity -> {
            if (includeInactive || Boolean.TRUE.equals(featureEntity.getActive())) {
                featureListResult.add(featureMapper.toTarget(featureEntity));
            }
        }));

		if(featureListResult.isEmpty()) {
			return ResponseEntity.notFound().build();
		} else {
			return ResponseEntity.ok(sort(featureListResult));
		}
	}

	/** {@inheritDoc} */
	@Override
	public ResponseEntity<List<Feature>> listByRole(UUID roleId) {
		LOGGER.info("STARTED - Find features by role id {}", roleId);
		if (Objects.isNull(roleId)) {
			return ResponseEntity.badRequest().build();
		}
		final var optFeatures = featureRepository.findByRoleId(roleId);
		if (optFeatures.isEmpty() || optFeatures.get().isEmpty()) {
			return ResponseEntity.notFound().build();
		}
		final var features = optFeatures.get().stream().map(featureMapper::toTarget).toList();
		return ResponseEntity.ok(sort(features));
	}

	/** {@inheritDoc} */
	@Override
	public ResponseEntity<Feature> find(UUID featureId) {
		ResponseEntity<Feature> responseEntity;
		final var featureEntity = featureRepository.findById(featureId).orElse(new FeatureEntity());
		if (esNulo(featureEntity.getId())) {
			responseEntity = ResponseEntity.notFound().build();
		} else {
			responseEntity = new ResponseEntity<>(featureMapper.toTarget(featureEntity), HttpStatus.OK);
		}
		LOGGER.info(responseEntity.getStatusCode() + MessageUtil.LOG_PATH_SEPARATOR + featureId);
		return responseEntity;
	}

	private List<Feature> sort(List<Feature> features) {
		return features.stream().sorted(Comparator.comparing(Feature::getId)).toList();
	}
}
